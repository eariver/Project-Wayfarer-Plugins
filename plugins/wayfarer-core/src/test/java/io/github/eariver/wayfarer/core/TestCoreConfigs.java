package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.core.config.CoreConfig;

import java.time.Duration;
import java.util.List;

final class TestCoreConfigs {
    private TestCoreConfigs() {}

    static CoreConfig valid() {
        return withThreadPrefix("Wayfarer-Test");
    }

    static CoreConfig withThreadPrefix(String prefix) {
        return withShutdownTimeout(prefix, Duration.ofSeconds(1));
    }

    static CoreConfig withShutdownTimeout(Duration timeout) {
        return withShutdownTimeout("Wayfarer-Test", timeout);
    }

    static CoreConfig withWaymarkEnabled() {
        CoreConfig baseline = valid();
        return new CoreConfig(
            baseline.configVersion(),
            baseline.serverId(),
            baseline.shutdownTimeout(),
            baseline.executor(),
            baseline.audit(),
            baseline.health(),
            baseline.mariadb(),
            baseline.redis(),
            baseline.migration(),
            new CoreConfig.WaymarkSettings(
                true,
                "RedisEconomy",
                Duration.ofMillis(50)
            )
        );
    }

    private static CoreConfig withShutdownTimeout(String prefix, Duration timeout) {
        return new CoreConfig(
            1,
            "test-server",
            timeout,
            new CoreConfig.ExecutorSettings(1, prefix),
            new CoreConfig.AuditSettings(false),
            new CoreConfig.HealthSettings(false),
            new CoreConfig.MariaDbSettings(
                false,
                "WAYFARER_DB_URL",
                "WAYFARER_DB_USERNAME",
                "WAYFARER_DB_PASSWORD",
                8,
                1,
                Duration.ofSeconds(5),
                null,
                null,
                null
            ),
            new CoreConfig.RedisSettings(
                false,
                "WAYFARER_REDIS_URI",
                Duration.ofSeconds(3),
                null
            ),
            new CoreConfig.MigrationSettings(false, List.of("db/migration/core")),
            new CoreConfig.WaymarkSettings(
                false,
                "RedisEconomy",
                Duration.ofSeconds(5)
            )
        );
    }
}
