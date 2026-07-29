package io.github.eariver.wayfarer.core.config;

import io.github.eariver.wayfarer.common.secret.SecretValue;

import java.time.Duration;
import java.util.List;

public record CoreConfig(
    int configVersion,
    String serverId,
    Duration shutdownTimeout,
    ExecutorSettings executor,
    AuditSettings audit,
    HealthSettings health,
    MariaDbSettings mariadb,
    RedisSettings redis,
    MigrationSettings migration,
    WaymarkSettings waymark
) implements AutoCloseable {
    @Override
    public void close() {
        mariadb.close();
        redis.close();
    }

    public record ExecutorSettings(
        int threads,
        String threadNamePrefix,
        int queueCapacity
    ) {
        public ExecutorSettings(int threads, String threadNamePrefix) {
            this(threads, threadNamePrefix, 256);
        }
    }

    public record AuditSettings(boolean enabled) {}

    public record HealthSettings(boolean playerDetails) {}

    public record MariaDbSettings(
        boolean enabled,
        String jdbcUrlReference,
        String usernameReference,
        String passwordReference,
        int maximumPoolSize,
        int minimumIdle,
        Duration connectionTimeout,
        SecretValue jdbcUrl,
        SecretValue username,
        SecretValue password
    ) implements AutoCloseable {
        @Override
        public void close() {
            closeSecret(jdbcUrl);
            closeSecret(username);
            closeSecret(password);
        }
    }

    public record RedisSettings(
        boolean enabled,
        String uriReference,
        Duration connectTimeout,
        Duration operationTimeout,
        Duration cacheMaximumTtl,
        Duration lockMaximumLease,
        String keyPrefix,
        SecretValue uri
    ) implements AutoCloseable {
        public RedisSettings(
            boolean enabled,
            String uriReference,
            Duration connectTimeout,
            SecretValue uri
        ) {
            this(
                enabled,
                uriReference,
                connectTimeout,
                Duration.ofSeconds(3),
                Duration.ofHours(1),
                Duration.ofSeconds(30),
                "wayfarer",
                uri
            );
        }

        @Override
        public void close() {
            closeSecret(uri);
        }
    }

    public record MigrationSettings(boolean enabled, List<String> locations) {
        public MigrationSettings {
            locations = List.copyOf(locations);
        }
    }

    public record WaymarkSettings(
        boolean enabled,
        String providerMode,
        String expectedProvider,
        Duration operationTimeout
    ) {
        public WaymarkSettings(
            boolean enabled,
            String expectedProvider,
            Duration operationTimeout
        ) {
            this(enabled, "vault", expectedProvider, operationTimeout);
        }
    }

    private static void closeSecret(SecretValue value) {
        if (value != null) {
            value.close();
        }
    }
}
