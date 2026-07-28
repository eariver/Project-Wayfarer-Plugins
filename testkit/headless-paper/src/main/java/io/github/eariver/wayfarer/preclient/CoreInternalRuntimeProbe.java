package io.github.eariver.wayfarer.preclient;

import io.github.eariver.wayfarer.api.WayfarerServices;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class CoreInternalRuntimeProbe {
    private CoreInternalRuntimeProbe() {}

    static void verifyMainThreadGuards(JavaPlugin probe) {
        Object runtime = coreRuntime(probe);
        verifyRedisMainThreadGuard(field(runtime, "redis"));
        verifyJdbcMainThreadGuard(field(runtime, "mariaDbPool"));
        probe.getLogger().info(
            "WAYFARER_PRECLIENT_PROBE: MAIN_THREAD_GUARDS PASS jdbc=true redis=true"
        );
    }

    static CompletionStage<Void> verifyRedisPrimitives(
        JavaPlugin probe,
        WayfarerServices services
    ) {
        return services.tasks().database(() -> {
            Object redis = field(coreRuntime(probe), "redis");
            verifyCache(redis);
            verifyLock(redis);
            verifyMessage(redis);
            probe.getLogger().info(
                "WAYFARER_PRECLIENT_PROBE: REDIS_PRIMITIVES PASS "
                    + "cache=true lock=true message=true"
            );
            return null;
        });
    }

    static CompletionStage<Void> verifyPlayerIdentity(
        JavaPlugin probe,
        UUID playerUuid
    ) {
        Object runtime = coreRuntime(probe);
        Object identity = field(runtime, "identity");
        Class<?> observationType = loadClass(
            runtime,
            "io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation"
        );
        Object observation = construct(
            constructor(
                observationType,
                UUID.class,
                String.class,
                String.class,
                Instant.class
            ),
            playerUuid,
            "PreclientProbe",
            "wayfarer-preclient",
            Instant.now()
        );
        CompletionStage<?> persisted = stage(invoke(
            declaredMethod(identity.getClass(), "observe", observationType),
            identity,
            observation
        ));
        return persisted.thenAccept(ignored -> probe.getLogger().info(
            "WAYFARER_PRECLIENT_PROBE: PLAYER_IDENTITY PASS uuid=" + playerUuid
        ));
    }

    private static void verifyRedisMainThreadGuard(Object redis) {
        Method cacheGet = declaredMethod(
            redis.getClass(),
            "cacheGet",
            String.class,
            String.class,
            int.class
        );
        CompletionStage<?> stage = stage(invoke(
            cacheGet,
            redis,
            "preclient",
            "main-thread",
            1
        ));
        expectFailure(stage, "Redis I/O is forbidden on the main thread");
    }

    private static void verifyJdbcMainThreadGuard(Object pool) {
        Object database = invoke(
            declaredMethod(pool.getClass(), "internalDatabaseForTesting"),
            pool
        );
        Class<?> operationType = nestedClass(database.getClass(), "SqlOperation");
        Object operation = Proxy.newProxyInstance(
            database.getClass().getClassLoader(),
            new Class<?>[]{operationType},
            (ignored, method, arguments) -> {
                if ("apply".equals(method.getName())) {
                    return 1;
                }
                return defaultObjectMethod(method, arguments);
            }
        );
        try {
            invoke(
                declaredMethod(database.getClass(), "executeRead", operationType),
                database,
                operation
            );
            throw new IllegalStateException("JDBC main-thread guard was not enforced");
        } catch (ProbeInvocationException failure) {
            requireMessage(failure.getCause(), "JDBC access is forbidden on the main thread");
        }
    }

    private static void verifyCache(Object redis) {
        Class<?> cacheValueType = loadClass(
            redis,
            "io.github.eariver.wayfarer.core.redis.RedisCacheValue"
        );
        Constructor<?> constructor = constructor(cacheValueType, int.class, String.class);
        Object value = construct(constructor, 1, "{\"balance\":100000}");
        await(stage(invoke(
            declaredMethod(
                redis.getClass(),
                "cachePut",
                String.class,
                String.class,
                cacheValueType,
                Duration.class
            ),
            redis,
            "preclient",
            "balance",
            value,
            Duration.ofSeconds(30)
        )));
        Optional<?> cached = cast(
            Optional.class,
            await(stage(invoke(
                declaredMethod(
                    redis.getClass(),
                    "cacheGet",
                    String.class,
                    String.class,
                    int.class
                ),
                redis,
                "preclient",
                "balance",
                1
            )))
        );
        require(cached.isPresent(), "Redis cache value");
        Object payload = invoke(declaredMethod(cacheValueType, "payload"), cached.orElseThrow());
        require("{\"balance\":100000}".equals(payload), "Redis cache payload");
    }

    private static void verifyLock(Object redis) {
        Method acquire = methodNamed(redis.getClass(), "tryAcquireLock");
        Optional<?> first = cast(
            Optional.class,
            await(stage(invoke(
                acquire,
                redis,
                "preclient",
                "exclusive",
                Duration.ofSeconds(5)
            )))
        );
        require(first.isPresent(), "Redis lock acquisition");
        Optional<?> second = cast(
            Optional.class,
            await(stage(invoke(
                acquire,
                redis,
                "preclient",
                "exclusive",
                Duration.ofSeconds(5)
            )))
        );
        require(second.isEmpty(), "Redis lock exclusion");
        Object lease = first.orElseThrow();
        Boolean released = cast(
            Boolean.class,
            await(stage(invoke(
                declaredMethod(redis.getClass(), "release", lease.getClass()),
                redis,
                lease
            )))
        );
        require(released, "Redis lock release");
    }

    private static void verifyMessage(Object redis) {
        CountDownLatch received = new CountDownLatch(1);
        Consumer<Object> handler = envelope -> {
            Object type = invoke(declaredMethod(envelope.getClass(), "messageType"), envelope);
            Object payload = invoke(declaredMethod(envelope.getClass(), "payload"), envelope);
            if ("PRECLIENT_PROBE".equals(type) && "{\"probe\":true}".equals(payload)) {
                received.countDown();
            }
        };
        invoke(
            declaredMethod(redis.getClass(), "subscribe", Consumer.class),
            redis,
            handler
        );

        String channel = cast(
            String.class,
            invoke(declaredMethod(redis.getClass(), "channel"), redis)
        );
        String message = encodeEnvelope();
        Object commands = field(redis, "commands");
        CompletionStage<?> published = stage(invoke(
            publicMethodNamed(commands.getClass(), "publish", 2),
            commands,
            channel,
            message
        ));
        await(published);
        try {
            require(received.await(3, TimeUnit.SECONDS), "Redis Pub/Sub delivery");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Redis Pub/Sub probe was interrupted");
        }
    }

    private static String encodeEnvelope() {
        return String.join(
            ".",
            "1",
            UUID.randomUUID().toString(),
            encode("external-preclient"),
            encode("PRECLIENT_PROBE"),
            Long.toString(Instant.now().toEpochMilli()),
            encode("{\"probe\":true}")
        );
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static Object coreRuntime(JavaPlugin probe) {
        Plugin core = probe.getServer().getPluginManager().getPlugin("Wayfarer_Core");
        require(core != null && core.isEnabled(), "Wayfarer_Core plugin");
        return field(core, "runtime");
    }

    private static Object field(Object target, String name) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Runtime probe field access failed");
        }
    }

    private static Method declaredMethod(
        Class<?> type,
        String name,
        Class<?>... parameterTypes
    ) {
        try {
            Method method = type.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Runtime probe method lookup failed");
        }
    }

    private static Method methodNamed(Class<?> type, String name) {
        for (Method method : type.getDeclaredMethods()) {
            if (name.equals(method.getName())) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("Runtime probe method lookup failed");
    }

    private static Method publicMethodNamed(Class<?> type, String name, int parameters) {
        for (Method method : type.getMethods()) {
            if (name.equals(method.getName())
                && method.getParameterCount() == parameters) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("Runtime probe public method lookup failed");
    }

    private static Class<?> nestedClass(Class<?> type, String simpleName) {
        for (Class<?> nested : type.getDeclaredClasses()) {
            if (simpleName.equals(nested.getSimpleName())) {
                return nested;
            }
        }
        throw new IllegalStateException("Runtime probe nested type lookup failed");
    }

    private static Class<?> loadClass(Object authority, String name) {
        try {
            return Class.forName(name, true, authority.getClass().getClassLoader());
        } catch (ClassNotFoundException failure) {
            throw new IllegalStateException("Runtime probe class lookup failed");
        }
    }

    private static Constructor<?> constructor(
        Class<?> type,
        Class<?>... parameterTypes
    ) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Runtime probe constructor lookup failed");
        }
    }

    private static Object construct(Constructor<?> constructor, Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Runtime probe construction failed");
        }
    }

    private static Object invoke(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException failure) {
            throw new ProbeInvocationException(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Runtime probe invocation failed");
        }
    }

    private static Object defaultObjectMethod(Method method, Object[] arguments) {
        return switch (method.getName()) {
            case "toString" -> "PreclientSqlOperation";
            case "hashCode" -> System.identityHashCode(method);
            case "equals" -> arguments != null
                && arguments.length == 1
                && method.equals(arguments[0]);
            default -> throw new IllegalStateException("Unexpected proxy method");
        };
    }

    private static CompletionStage<?> stage(Object value) {
        return cast(CompletionStage.class, value);
    }

    private static Object await(CompletionStage<?> stage) {
        return stage.toCompletableFuture().join();
    }

    private static void expectFailure(CompletionStage<?> stage, String expectedMessage) {
        try {
            await(stage);
            throw new IllegalStateException("Expected runtime guard failure");
        } catch (RuntimeException failure) {
            requireMessage(rootCause(failure), expectedMessage);
        }
    }

    private static void requireMessage(Throwable failure, String expectedMessage) {
        require(
            failure != null && expectedMessage.equals(failure.getMessage()),
            "sanitized runtime guard message"
        );
    }

    private static Throwable rootCause(Throwable failure) {
        Throwable current = failure;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static <T> T cast(Class<T> type, Object value) {
        return type.cast(value);
    }

    private static void require(boolean condition, String checkpoint) {
        if (!condition) {
            throw new IllegalStateException("Probe checkpoint failed: " + checkpoint);
        }
    }

    private static final class ProbeInvocationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        ProbeInvocationException(Throwable cause) {
            super(cause);
        }
    }
}
