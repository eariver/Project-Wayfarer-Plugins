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
        return new CoreConfig(
            1,
            "test-server",
            Duration.ofSeconds(1),
            new CoreConfig.ExecutorSettings(1, prefix),
            new CoreConfig.AuditSettings(true),
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
