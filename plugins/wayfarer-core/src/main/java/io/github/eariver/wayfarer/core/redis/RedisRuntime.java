package io.github.eariver.wayfarer.core.redis;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.persistence.ThreadContext;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionStateListener;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RedisRuntime implements AutoCloseable {
    private static final String RELEASE_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then "
            + "return redis.call('del', KEYS[1]) else return 0 end";
    private static final int CACHE_PAYLOAD_BYTES = 16 * 1024;
    private static final int RECENT_MESSAGE_LIMIT = 4_096;

    private final CoreConfig.RedisSettings settings;
    private final String serverId;
    private final HealthRegistry health;
    private final ThreadContext threadContext;
    private final ManagedExecutor executor;
    private final Consumer<String> warningSink;
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final RedisAsyncCommands<String, String> commands;
    private final RedisEnvelopeCodec envelopeCodec = new RedisEnvelopeCodec();
    private final AtomicInteger commandReconnects = new AtomicInteger();
    private final List<Consumer<RedisEnvelope>> messageHandlers = new CopyOnWriteArrayList<>();
    private final Map<UUID, Boolean> recentMessages = Collections.synchronizedMap(
        new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, Boolean> eldest) {
                return size() > RECENT_MESSAGE_LIMIT;
            }
        }
    );
    private final Object lifecycleMonitor = new Object();
    private boolean accepting = true;
    private boolean commandConnected = true;
    private boolean pubSubConnected = true;
    private int inFlight;
    private RedisCloseStatus closeStatus;

    private RedisRuntime(
        CoreConfig.RedisSettings settings,
        String serverId,
        HealthRegistry health,
        ThreadContext threadContext,
        ManagedExecutor executor,
        Consumer<String> warningSink,
        RedisClient client,
        StatefulRedisConnection<String, String> connection,
        StatefulRedisPubSubConnection<String, String> pubSubConnection
    ) {
        this.settings = settings;
        this.serverId = serverId;
        this.health = health;
        this.threadContext = threadContext;
        this.executor = executor;
        this.warningSink = warningSink;
        this.client = client;
        this.connection = connection;
        this.pubSubConnection = pubSubConnection;
        this.commands = connection.async();
    }

    public static RedisRuntime connect(
        CoreConfig.RedisSettings settings,
        String serverId,
        HealthRegistry health,
        ThreadContext threadContext,
        ManagedExecutor executor,
        Consumer<String> warningSink
    ) {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(health, "health");
        Objects.requireNonNull(threadContext, "threadContext");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(warningSink, "warningSink");
        if (!settings.enabled() || settings.uri() == null) {
            throw new RedisRuntimeException("Redis is disabled");
        }
        if (threadContext.isMainThread()) {
            throw new RedisRuntimeException("Redis connection is forbidden on the main thread");
        }

        RedisClient client = null;
        StatefulRedisConnection<String, String> connection = null;
        StatefulRedisPubSubConnection<String, String> pubSub = null;
        try {
            RedisURI uri = settings.uri().use(
                characters -> RedisURI.create(new String(characters))
            );
            uri.setTimeout(settings.connectTimeout());
            client = RedisClient.create(uri);
            client.setOptions(ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build());
            connection = client.connect();
            connection.setTimeout(settings.operationTimeout());
            pubSub = client.connectPubSub();
            pubSub.setTimeout(settings.operationTimeout());
            RedisRuntime runtime = new RedisRuntime(
                settings,
                serverId,
                health,
                threadContext,
                executor,
                warningSink,
                client,
                connection,
                pubSub
            );
            runtime.installListeners();
            runtime.await(runtime.commands.ping());
            runtime.await(pubSub.async().subscribe(runtime.channel()));
            health.update(HealthRegistry.REDIS, WayfarerHealth.Status.UP, "Redis connected");
            return runtime;
        } catch (RuntimeException failure) {
            closeQuietly(pubSub);
            closeQuietly(connection);
            if (client != null) {
                try {
                    client.shutdown(Duration.ZERO, settings.connectTimeout());
                } catch (RuntimeException ignored) {
                    // The sanitized startup failure remains authoritative.
                }
            }
            health.update(
                HealthRegistry.REDIS,
                WayfarerHealth.Status.DOWN,
                "Redis initialization failed"
            );
            warn(warningSink, "Wayfarer Redis initialization failed");
            throw new RedisRuntimeException("Redis initialization failed");
        }
    }

    CompletionStage<Void> cachePut(
        String namespace,
        String key,
        RedisCacheValue value,
        Duration ttl
    ) {
        Objects.requireNonNull(value, "value");
        validateTtl(ttl, settings.cacheMaximumTtl(), "cache TTL");
        String redisKey = cacheKey(namespace, key);
        String encoded = value.schemaVersion() + "." + Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.payload().getBytes(StandardCharsets.UTF_8));
        return execute(() -> commands.set(redisKey, encoded, SetArgs.Builder.px(ttl)))
            .thenApply(ignored -> null);
    }

    CompletionStage<Optional<RedisCacheValue>> cacheGet(
        String namespace,
        String key,
        int expectedSchemaVersion
    ) {
        if (expectedSchemaVersion < 1) {
            throw new RedisRuntimeException("cache schema version is invalid");
        }
        String redisKey = cacheKey(namespace, key);
        return execute(() -> commands.get(redisKey)).thenApply(encoded -> {
            if (encoded == null) {
                return Optional.empty();
            }
            RedisCacheValue value = decodeCacheValue(encoded);
            return value.schemaVersion() == expectedSchemaVersion
                ? Optional.of(value)
                : Optional.empty();
        });
    }

    CompletionStage<Optional<RedisCacheValue>> cacheAside(
        String namespace,
        String key,
        int expectedSchemaVersion,
        Duration ttl,
        Supplier<CompletionStage<Optional<RedisCacheValue>>> authoritativeLoader
    ) {
        Objects.requireNonNull(authoritativeLoader, "authoritativeLoader");
        validateTtl(ttl, settings.cacheMaximumTtl(), "cache TTL");
        return cacheGet(namespace, key, expectedSchemaVersion)
            .handle((cached, failure) ->
                failure == null ? cached : Optional.<RedisCacheValue>empty()
            )
            .thenCompose(cached -> {
                if (cached.isPresent()) {
                    return CompletableFuture.<Optional<RedisCacheValue>>completedFuture(cached);
                }
                CompletionStage<Optional<RedisCacheValue>> authoritative =
                    Objects.requireNonNull(authoritativeLoader.get(), "authoritative result");
                return authoritative.thenApply(value -> {
                    value.ifPresent(cacheValue -> cachePut(
                        namespace,
                        key,
                        cacheValue,
                        ttl
                    ).whenComplete((ignored, failure) -> {
                        if (failure != null) {
                            warn("Wayfarer Redis cache refresh failed");
                        }
                    }));
                    return value;
                });
            });
    }

    CompletionStage<Optional<RedisLease>> tryAcquireLock(
        String namespace,
        String key,
        Duration lease
    ) {
        validateTtl(lease, settings.lockMaximumLease(), "lock lease");
        String redisKey = lockKey(namespace, key);
        UUID ownerToken = UUID.randomUUID();
        return execute(() -> commands.set(
            redisKey,
            ownerToken.toString(),
            SetArgs.Builder.nx().px(lease)
        )).thenApply(reply -> "OK".equals(reply)
            ? Optional.of(new RedisLease(redisKey, ownerToken, Instant.now().plus(lease)))
            : Optional.empty());
    }

    CompletionStage<Boolean> release(RedisLease lease) {
        Objects.requireNonNull(lease, "lease");
        if (lease.ownerToken() == null || lease.key() == null) {
            throw new RedisRuntimeException("Redis lease is invalid");
        }
        return execute(() -> commands.eval(
            RELEASE_SCRIPT,
            ScriptOutputType.INTEGER,
            new String[]{lease.key()},
            lease.ownerToken().toString()
        )).thenApply(deleted -> ((Number) deleted).longValue() == 1L);
    }

    CompletionStage<RedisIdempotencyResult> markIdempotencyHint(
        String idempotencyKey,
        Duration ttl
    ) {
        validateTtl(ttl, settings.cacheMaximumTtl(), "idempotency hint TTL");
        String redisKey = settings.keyPrefix()
            + ":idempotency:v1:"
            + RedisDataValidator.keyPart(idempotencyKey, "idempotency key");
        return execute(() -> commands.set(
            redisKey,
            "1",
            SetArgs.Builder.nx().px(ttl)
        )).thenApply(reply -> "OK".equals(reply)
            ? RedisIdempotencyResult.NEW_HINT
            : RedisIdempotencyResult.DUPLICATE_HINT);
    }

    CompletionStage<Long> publish(RedisEnvelope envelope) {
        if (envelope == null || !serverId.equals(envelope.originServerId())) {
            throw new RedisRuntimeException("Redis message origin is not configured authority");
        }
        String encoded = envelopeCodec.encode(envelope);
        return execute(() -> commands.publish(channel(), encoded));
    }

    void subscribe(Consumer<RedisEnvelope> handler) {
        messageHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    boolean isAccepting() {
        synchronized (lifecycleMonitor) {
            return accepting;
        }
    }

    int inFlight() {
        synchronized (lifecycleMonitor) {
            return inFlight;
        }
    }

    int commandReconnects() {
        return commandReconnects.get();
    }

    @Override
    public void close() {
        shutdown(settings.operationTimeout());
    }

    RedisCloseStatus shutdown(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        synchronized (lifecycleMonitor) {
            if (closeStatus != null) {
                return closeStatus;
            }
            accepting = false;
        }
        RedisCloseStatus drainStatus = awaitDrain(timeout);
        RedisCloseStatus resourceStatus = closeResources(timeout);
        synchronized (lifecycleMonitor) {
            closeStatus = drainStatus == RedisCloseStatus.CLEAN
                ? resourceStatus
                : drainStatus;
        }
        if (closeStatus == RedisCloseStatus.CLEAN) {
            health.update(
                HealthRegistry.REDIS,
                WayfarerHealth.Status.DISABLED,
                "Redis connections closed after accepted work drained"
            );
        } else {
            health.update(
                HealthRegistry.REDIS,
                WayfarerHealth.Status.DOWN,
                "Redis shutdown was incomplete"
            );
        }
        return closeStatus;
    }

    private void installListeners() {
        connection.addListener(stateListener(true));
        pubSubConnection.addListener(stateListener(false));
        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                receive(channel, message);
            }
        });
    }

    private RedisConnectionStateListener stateListener(boolean commandConnection) {
        return new RedisConnectionStateListener() {
            @Override
            public void onRedisConnected(
                RedisChannelHandler<?, ?> ignored,
                SocketAddress socketAddress
            ) {
                connectionState(commandConnection, true);
            }

            @Override
            public void onRedisDisconnected(RedisChannelHandler<?, ?> ignored) {
                connectionState(commandConnection, false);
            }

            @Override
            public void onRedisExceptionCaught(
                RedisChannelHandler<?, ?> ignored,
                Throwable cause
            ) {
                markDown("Redis connection failure");
            }
        };
    }

    private void connectionState(boolean command, boolean connected) {
        synchronized (lifecycleMonitor) {
            if (command) {
                if (connected && !commandConnected) {
                    commandReconnects.incrementAndGet();
                }
                commandConnected = connected;
            } else {
                pubSubConnected = connected;
            }
            if (!accepting) {
                return;
            }
            if (commandConnected && pubSubConnected) {
                health.update(
                    HealthRegistry.REDIS,
                    WayfarerHealth.Status.UP,
                    "Redis reconnected"
                );
            } else {
                health.update(
                    HealthRegistry.REDIS,
                    WayfarerHealth.Status.DOWN,
                    "Redis connection unavailable"
                );
            }
        }
    }

    private void receive(String incomingChannel, String encoded) {
        if (!channel().equals(incomingChannel) || !isAccepting()) {
            return;
        }
        RedisEnvelope envelope;
        try {
            envelope = envelopeCodec.decode(encoded);
        } catch (RedisRuntimeException failure) {
            warn("Wayfarer Redis rejected an invalid message");
            return;
        }
        if (serverId.equals(envelope.originServerId())
            || recentMessages.putIfAbsent(envelope.messageId(), Boolean.TRUE) != null) {
            return;
        }
        executor.submit(() -> {
            for (Consumer<RedisEnvelope> handler : messageHandlers) {
                handler.accept(envelope);
            }
            return null;
        });
    }

    private <T> CompletionStage<T> execute(
        Supplier<? extends CompletionStage<T>> operation
    ) {
        if (threadContext.isMainThread()) {
            return CompletableFuture.failedFuture(
                new RedisRuntimeException("Redis I/O is forbidden on the main thread")
            );
        }
        synchronized (lifecycleMonitor) {
            if (!accepting) {
                return CompletableFuture.failedFuture(
                    new RejectedExecutionException("Redis runtime is stopping")
                );
            }
            inFlight++;
        }
        CompletableFuture<T> result = new CompletableFuture<>();
        CompletionStage<T> stage;
        try {
            stage = Objects.requireNonNull(operation.get(), "Redis operation");
        } catch (RuntimeException failure) {
            operationCompleted(failure);
            return CompletableFuture.failedFuture(
                new RedisRuntimeException("Redis operation failed")
            );
        }
        stage.whenComplete((value, failure) -> {
                operationCompleted(failure);
                if (failure == null) {
                    result.complete(value);
                } else {
                    result.completeExceptionally(
                        new RedisRuntimeException("Redis operation failed")
                    );
                }
            });
        return result;
    }

    private void operationCompleted(Throwable failure) {
        synchronized (lifecycleMonitor) {
            inFlight--;
            lifecycleMonitor.notifyAll();
            if (failure == null && accepting && commandConnected && pubSubConnected) {
                health.update(
                    HealthRegistry.REDIS,
                    WayfarerHealth.Status.UP,
                    "Redis available"
                );
            } else if (failure != null && accepting) {
                markDown("Redis operation failed");
            }
        }
    }

    private RedisCloseStatus awaitDrain(Duration timeout) {
        long remainingNanos = timeout.toNanos();
        long deadline = System.nanoTime() + remainingNanos;
        synchronized (lifecycleMonitor) {
            while (inFlight > 0 && remainingNanos > 0) {
                try {
                    long millis = remainingNanos / 1_000_000L;
                    int nanos = (int) (remainingNanos % 1_000_000L);
                    lifecycleMonitor.wait(millis, nanos);
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    warn("Wayfarer Redis shutdown drain was interrupted");
                    return RedisCloseStatus.INTERRUPTED;
                }
                remainingNanos = deadline - System.nanoTime();
            }
            if (inFlight > 0) {
                warn("Wayfarer Redis shutdown drain timed out");
                return RedisCloseStatus.TIMED_OUT;
            }
            return RedisCloseStatus.CLEAN;
        }
    }

    private RedisCloseStatus closeResources(Duration timeout) {
        try {
            try {
                await(pubSubConnection.async().unsubscribe(channel()));
            } catch (RuntimeException failure) {
                warn("Wayfarer Redis Pub/Sub unsubscribe failed");
            }
            pubSubConnection.closeAsync()
                .orTimeout(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .join();
            connection.closeAsync()
                .orTimeout(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .join();
            client.shutdown(Duration.ZERO, timeout);
            return RedisCloseStatus.CLEAN;
        } catch (RuntimeException failure) {
            warn("Wayfarer Redis resource close failed");
            return RedisCloseStatus.FAILED;
        }
    }

    private <T> T await(CompletionStage<T> stage) {
        return stage.toCompletableFuture()
            .orTimeout(
                settings.operationTimeout().toMillis(),
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .join();
    }

    private String cacheKey(String namespace, String key) {
        return settings.keyPrefix()
            + ":cache:"
            + RedisDataValidator.keyPart(namespace, "cache namespace")
            + ":"
            + RedisDataValidator.keyPart(key, "cache key");
    }

    private String lockKey(String namespace, String key) {
        return settings.keyPrefix()
            + ":lock:"
            + RedisDataValidator.keyPart(namespace, "lock namespace")
            + ":"
            + RedisDataValidator.keyPart(key, "lock key");
    }

    private String channel() {
        return settings.keyPrefix() + ":events:v1";
    }

    private static RedisCacheValue decodeCacheValue(String encoded) {
        try {
            int separator = encoded.indexOf('.');
            if (separator < 1) {
                throw new IllegalArgumentException();
            }
            int schemaVersion = Integer.parseInt(encoded.substring(0, separator));
            String payload = new String(
                Base64.getUrlDecoder().decode(encoded.substring(separator + 1)),
                StandardCharsets.UTF_8
            );
            RedisDataValidator.safePayload(payload, CACHE_PAYLOAD_BYTES, "cache payload");
            return new RedisCacheValue(schemaVersion, payload);
        } catch (IllegalArgumentException failure) {
            throw new RedisRuntimeException("Redis cache envelope is invalid");
        }
    }

    private static void validateTtl(Duration value, Duration maximum, String field) {
        Objects.requireNonNull(value, field);
        if (value.isZero() || value.isNegative() || value.compareTo(maximum) > 0) {
            throw new RedisRuntimeException(field + " is outside the configured bound");
        }
    }

    private void markDown(String detail) {
        health.update(HealthRegistry.REDIS, WayfarerHealth.Status.DOWN, detail);
    }

    private void warn(String warning) {
        warn(warningSink, warning);
    }

    private static void warn(Consumer<String> sink, String warning) {
        try {
            sink.accept(warning);
        } catch (RuntimeException ignored) {
            // Health and completion state remain authoritative.
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // Startup failure cleanup continues.
            }
        }
    }
}
