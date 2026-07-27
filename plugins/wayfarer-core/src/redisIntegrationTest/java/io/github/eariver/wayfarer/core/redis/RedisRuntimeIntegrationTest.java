package io.github.eariver.wayfarer.core.redis;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import io.github.eariver.wayfarer.core.CoreRuntime;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.persistence.ThreadContext;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class RedisRuntimeIntegrationTest {
    private static final String PASSWORD = "wayfarer-test-" + UUID.randomUUID();

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
        DockerImageName.parse("redis:8-alpine")
    ).withExposedPorts(6379).withEnv("REDISCLI_AUTH", PASSWORD).withCommand(
        "redis-server",
        "--requirepass",
        PASSWORD,
        "--appendonly",
        "yes"
    );

    @Test
    void cacheFallsBackToAuthorityAndLockReleaseIsOwnerChecked() {
        try (Fixture fixture = fixture("main-1", () -> false)) {
            RedisCacheValue value = new RedisCacheValue(1, "{\"balance\":12}");
            fixture.runtime.cachePut("balances", "player-1", value, Duration.ofSeconds(30))
                .toCompletableFuture()
                .join();
            assertEquals(
                Optional.of(value),
                fixture.runtime.cacheGet("balances", "player-1", 1)
                    .toCompletableFuture()
                    .join()
            );
            assertEquals(
                Optional.empty(),
                fixture.runtime.cacheGet("balances", "missing", 1)
                    .toCompletableFuture()
                    .join()
            );

            AtomicInteger loads = new AtomicInteger();
            RedisCacheValue authoritative = new RedisCacheValue(1, "{\"balance\":21}");
            assertEquals(
                Optional.of(authoritative),
                fixture.runtime.cacheAside(
                    "balances",
                    "authority",
                    1,
                    Duration.ofSeconds(30),
                    () -> {
                        loads.incrementAndGet();
                        return java.util.concurrent.CompletableFuture.completedFuture(
                            Optional.of(authoritative)
                        );
                    }
                ).toCompletableFuture().join()
            );
            assertEquals(1, loads.get());

            RedisLease lease = fixture.runtime.tryAcquireLock(
                "transaction",
                "player-1",
                Duration.ofSeconds(5)
            ).toCompletableFuture().join().orElseThrow();
            assertEquals(
                Optional.empty(),
                fixture.runtime.tryAcquireLock(
                    "transaction",
                    "player-1",
                    Duration.ofSeconds(5)
                ).toCompletableFuture().join()
            );
            assertFalse(fixture.runtime.release(new RedisLease(
                lease.key(),
                UUID.randomUUID(),
                lease.expiresAt()
            )).toCompletableFuture().join());
            assertTrue(fixture.runtime.release(lease).toCompletableFuture().join());

            assertEquals(
                RedisIdempotencyResult.NEW_HINT,
                fixture.runtime.markIdempotencyHint("operation-1", Duration.ofSeconds(30))
                    .toCompletableFuture()
                    .join()
            );
            assertEquals(
                RedisIdempotencyResult.DUPLICATE_HINT,
                fixture.runtime.markIdempotencyHint("operation-1", Duration.ofSeconds(30))
                    .toCompletableFuture()
                    .join()
            );
        }
    }

    @Test
    void pubSubUsesConfiguredOriginIgnoresSelfAndDeduplicatesMessageId() throws Exception {
        try (Fixture main = fixture("main-1", () -> false);
             Fixture frontier = fixture("frontier-1", () -> false)) {
            CountDownLatch received = new CountDownLatch(1);
            List<RedisEnvelope> external = new ArrayList<>();
            AtomicInteger self = new AtomicInteger();
            main.runtime.subscribe(ignored -> self.incrementAndGet());
            frontier.runtime.subscribe(message -> {
                external.add(message);
                received.countDown();
            });
            RedisEnvelope envelope = new RedisEnvelope(
                1,
                UUID.randomUUID(),
                "main-1",
                "CACHE_INVALIDATED",
                "{\"namespace\":\"balances\",\"revision\":5}",
                Instant.now().truncatedTo(ChronoUnit.MILLIS)
            );

            main.runtime.publish(envelope).toCompletableFuture().join();
            main.runtime.publish(envelope).toCompletableFuture().join();

            assertTrue(received.await(3, TimeUnit.SECONDS));
            assertEquals(List.of(envelope), external);
            assertEquals(0, self.get());
        }
    }

    @Test
    void outageMarksDownAndSuccessfulReconnectMarksUp() throws Exception {
        try (Fixture fixture = fixture("main-1", () -> false)) {
            REDIS.getDockerClient().pauseContainerCmd(REDIS.getContainerId()).exec();
            try {
                assertThrows(
                    CompletionException.class,
                    () -> fixture.runtime.cacheGet("health", "probe", 1)
                        .toCompletableFuture()
                        .join()
                );
                assertEquals(WayfarerHealth.Status.DOWN, fixture.redisHealth());
            } finally {
                REDIS.getDockerClient().unpauseContainerCmd(REDIS.getContainerId()).exec();
            }

            awaitRedisSuccess(fixture.runtime);
            assertEquals(WayfarerHealth.Status.UP, fixture.redisHealth());

            int reconnects = fixture.runtime.commandReconnects();
            var kill = REDIS.execInContainer(
                "redis-cli",
                "CLIENT",
                "KILL",
                "TYPE",
                "normal",
                "SKIPME",
                "yes"
            );
            assertEquals(0, kill.getExitCode());
            awaitReconnect(fixture.runtime, reconnects);
            awaitRedisSuccess(fixture.runtime);
            assertEquals(WayfarerHealth.Status.UP, fixture.redisHealth());
        }
    }

    @Test
    void mainThreadIoFailsBeforeRedisAndCleanCloseDisablesHealth() {
        AtomicBoolean mainThread = new AtomicBoolean();
        Fixture fixture = fixture("main-1", mainThread::get);
        try {
            mainThread.set(true);
            assertThrows(
                CompletionException.class,
                () -> fixture.runtime.cacheGet("health", "probe", 1)
                    .toCompletableFuture()
                    .join()
            );
            assertEquals(WayfarerHealth.Status.UP, fixture.redisHealth());
            mainThread.set(false);
            assertEquals(
                RedisCloseStatus.CLEAN,
                fixture.runtime.shutdown(Duration.ofSeconds(3))
            );
            assertEquals(WayfarerHealth.Status.DISABLED, fixture.redisHealth());
            assertThrows(
                CompletionException.class,
                () -> fixture.runtime.cacheGet("health", "probe", 1)
                    .toCompletableFuture()
                    .join()
            );
        } finally {
            mainThread.set(false);
            fixture.close();
        }
    }

    @Test
    void coreLifecyclePublishesAfterRedisAndUnpublishesBeforeRedisClose() {
        CoreConfig.RedisSettings redisSettings = redisSettings();
        CoreConfig config = new CoreConfig(
            1,
            "main-1",
            Duration.ofSeconds(3),
            new CoreConfig.ExecutorSettings(2, "Wayfarer-Core-Redis", 32),
            new CoreConfig.AuditSettings(false),
            new CoreConfig.HealthSettings(false),
            new CoreConfig.MariaDbSettings(
                false,
                "TEST_DB_URL",
                "TEST_DB_USERNAME",
                "TEST_DB_PASSWORD",
                2,
                0,
                Duration.ofSeconds(1),
                null,
                null,
                null
            ),
            redisSettings,
            new CoreConfig.MigrationSettings(false, List.of("classpath:db/migration/core")),
            new CoreConfig.WaymarkSettings(false, "RedisEconomy", Duration.ofSeconds(1))
        );
        AtomicReference<CoreRuntime> runtimeReference = new AtomicReference<>();
        AtomicReference<WayfarerHealth.Status> redisAtUnpublish = new AtomicReference<>();
        AtomicBoolean published = new AtomicBoolean();
        ServicePublisher publisher = new ServicePublisher() {
            @Override
            public void publish(WayfarerServices services, WayfarerHealth health) {
                published.set(true);
            }

            @Override
            public void unpublish() {
                redisAtUnpublish.set(
                    runtimeReference.get().health().snapshot().components()
                        .get(HealthRegistry.REDIS)
                        .status()
                );
                published.set(false);
            }
        };
        CoreRuntime runtime = new CoreRuntime(
            config,
            publisher,
            Runnable::run,
            Clock.systemUTC(),
            ignored -> {},
            () -> false
        );
        runtimeReference.set(runtime);

        runtime.enable();
        assertTrue(published.get());
        assertEquals(
            WayfarerHealth.Status.UP,
            runtime.health().snapshot().components().get(HealthRegistry.REDIS).status()
        );

        runtime.disable();
        assertFalse(published.get());
        assertEquals(WayfarerHealth.Status.UP, redisAtUnpublish.get());
        assertEquals(
            WayfarerHealth.Status.DISABLED,
            runtime.health().snapshot().components().get(HealthRegistry.REDIS).status()
        );
        assertEquals(WayfarerLifecycleState.DISABLED, runtime.state());
    }

    @Test
    void shutdownRejectsNewWorkAndSettlesAcceptedRedisCommand() throws Exception {
        try (Fixture fixture = fixture("main-1", () -> false)) {
            REDIS.getDockerClient().pauseContainerCmd(REDIS.getContainerId()).exec();
            AtomicReference<RedisCloseStatus> closeStatus = new AtomicReference<>();
            try {
                var accepted = fixture.runtime.cacheGet("shutdown", "accepted", 1)
                    .toCompletableFuture();
                awaitInFlight(fixture.runtime);
                Thread shutdown = new Thread(
                    () -> closeStatus.set(fixture.runtime.shutdown(Duration.ofSeconds(3))),
                    "Wayfarer-Redis-Shutdown-Test"
                );
                shutdown.start();
                awaitNotAccepting(fixture.runtime);
                assertTrue(shutdown.isAlive());
                assertThrows(
                    CompletionException.class,
                    () -> fixture.runtime.cacheGet("shutdown", "late", 1)
                        .toCompletableFuture()
                        .join()
                );

                REDIS.getDockerClient().unpauseContainerCmd(REDIS.getContainerId()).exec();
                assertEquals(Optional.empty(), accepted.join());
                shutdown.join(3_000);
                assertFalse(shutdown.isAlive());
                assertEquals(RedisCloseStatus.CLEAN, closeStatus.get());
            } finally {
                if (REDIS.getDockerClient().inspectContainerCmd(REDIS.getContainerId())
                    .exec().getState().getPaused()) {
                    REDIS.getDockerClient().unpauseContainerCmd(REDIS.getContainerId()).exec();
                }
            }
        }
    }

    private static Fixture fixture(String serverId, ThreadContext threadContext) {
        CoreConfig.RedisSettings settings = redisSettings();
        HealthRegistry health = new HealthRegistry(
            Clock.systemUTC(),
            () -> WayfarerLifecycleState.ENABLED
        );
        ManagedExecutor executor = new ManagedExecutor(
            2,
            "Wayfarer-Redis-Integration",
            32,
            Duration.ofSeconds(3),
            ignored -> {},
            ignored -> {}
        );
        RedisRuntime runtime = RedisRuntime.connect(
            settings,
            serverId,
            health,
            threadContext,
            executor,
            ignored -> {}
        );
        return new Fixture(settings, health, executor, runtime);
    }

    private static CoreConfig.RedisSettings redisSettings() {
        String uri = "redis://:" + PASSWORD + "@" + REDIS.getHost()
            + ":" + REDIS.getMappedPort(6379) + "/0";
        return new CoreConfig.RedisSettings(
            true,
            "TEST_REDIS_URI",
            Duration.ofSeconds(3),
            Duration.ofSeconds(2),
            Duration.ofMinutes(5),
            Duration.ofSeconds(10),
            "wayfarer-test",
            SecretValue.of(uri)
        );
    }

    private static void awaitInFlight(RedisRuntime runtime) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (runtime.inFlight() == 0 && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertEquals(1, runtime.inFlight());
    }

    private static void awaitNotAccepting(RedisRuntime runtime) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (runtime.isAccepting() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertFalse(runtime.isAccepting());
    }

    private static void awaitReconnect(RedisRuntime runtime, int previousCount)
        throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (runtime.commandReconnects() <= previousCount && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }
        assertTrue(runtime.commandReconnects() > previousCount);
    }

    private static void awaitRedisSuccess(RedisRuntime runtime) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            try {
                runtime.cacheGet("health", "probe", 1).toCompletableFuture().join();
                return;
            } catch (CompletionException ignored) {
                Thread.sleep(25);
            }
        }
        throw new AssertionError("Redis did not accept a command before the deadline");
    }

    private record Fixture(
        CoreConfig.RedisSettings settings,
        HealthRegistry health,
        ManagedExecutor executor,
        RedisRuntime runtime
    ) implements AutoCloseable {
        WayfarerHealth.Status redisHealth() {
            return health.snapshot().components().get(HealthRegistry.REDIS).status();
        }

        @Override
        public void close() {
            runtime.close();
            executor.close();
            settings.close();
        }
    }
}
