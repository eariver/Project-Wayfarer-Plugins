package io.github.eariver.wayfarer.core.config;

import io.github.eariver.wayfarer.common.secret.EnvironmentSecretResolver;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreConfigLoaderTest {
    private static final String SECRET = "private-password-value";

    @Test
    void validConfigLoadsTypedValues() {
        try (CoreConfig config = load(validValues(), Map.of())) {
            assertEquals(1, config.configVersion());
            assertEquals("test-server", config.serverId());
            assertEquals(2, config.executor().threads());
            assertEquals(15, config.shutdownTimeout().toSeconds());
            assertFalse(config.redis().enabled());
            assertEquals(
                List.of("classpath:db/migration/core"),
                config.migration().locations()
            );
        }
    }

    @Test
    void missingConfigVersionFailsClosed() {
        Map<String, Object> values = validValues();
        values.remove("config-version");
        assertMessage(values, Map.of(), "config-version");
    }

    @Test
    void unsupportedConfigVersionFailsClosed() {
        Map<String, Object> values = validValues();
        values.put("config-version", 2);
        assertMessage(values, Map.of(), "Unsupported config-version");
    }

    @Test
    void missingServerIdFailsClosed() {
        Map<String, Object> values = validValues();
        values.remove("server-id");
        assertMessage(values, Map.of(), "server-id");
    }

    @Test
    void blankServerIdFailsClosed() {
        Map<String, Object> values = validValues();
        values.put("server-id", " ");
        assertMessage(values, Map.of(), "server-id");
    }

    @Test
    void placeholderServerIdsFailClosed() {
        for (String placeholder : List.of(
            "CHANGE_ME",
            "change_me",
            "Change_Me",
            "change-me",
            "changeme",
            "default",
            "example"
        )) {
            Map<String, Object> values = validValues();
            values.put("server-id", placeholder);
            assertMessage(values, Map.of(), "explicitly configured");
        }
    }

    @Test
    void explicitServerIdLoads() {
        Map<String, Object> values = validValues();
        values.put("server-id", "wayfarer-test");
        try (CoreConfig config = load(values, Map.of())) {
            assertEquals("wayfarer-test", config.serverId());
        }
    }

    @Test
    void placeholderFailureDoesNotExposeSecrets() {
        Map<String, Object> values = validValues();
        values.put("server-id", "CHANGE_ME");
        values.put("mariadb.enabled", true);
        CoreConfigException failure = assertThrows(
            CoreConfigException.class,
            () -> load(values, Map.of(
                "WAYFARER_DB_URL", "jdbc:mariadb://example/wayfarer",
                "WAYFARER_DB_USERNAME", "wayfarer",
                "WAYFARER_DB_PASSWORD", SECRET
            ))
        );
        assertTrue(failure.getMessage().contains("explicitly configured"));
        assertFalse(failure.getMessage().contains(SECRET));
    }

    @Test
    void invalidShutdownTimeoutFailsClosed() {
        Map<String, Object> values = validValues();
        values.put("shutdown-timeout.seconds", 0);
        assertMessage(values, Map.of(), "shutdown-timeout.seconds");
    }

    @Test
    void invalidExecutorSettingFailsClosed() {
        Map<String, Object> values = validValues();
        values.put("executor.threads", 0);
        assertMessage(values, Map.of(), "executor.threads");
    }

    @Test
    void executorThreadPrefixMustIdentifyWayfarer() {
        Map<String, Object> values = validValues();
        values.put("executor.thread-name-prefix", "generic");
        assertMessage(values, Map.of(), "Wayfarer");
    }

    @Test
    void enabledSecretReferencesResolve() {
        Map<String, Object> values = validValues();
        values.put("mariadb.enabled", true);
        Map<String, String> environment = Map.of(
            "WAYFARER_DB_URL", "jdbc:mariadb://example/wayfarer",
            "WAYFARER_DB_USERNAME", "wayfarer",
            "WAYFARER_DB_PASSWORD", SECRET
        );
        try (CoreConfig config = load(values, environment)) {
            assertTrue(config.mariadb().enabled());
            assertEquals(SECRET, config.mariadb().password().use(String::new));
            assertFalse(config.toString().contains(SECRET));
        }
    }

    @Test
    void missingSecretReferenceFailsClosed() {
        Map<String, Object> values = validValues();
        values.put("redis.enabled", true);
        CoreConfigException failure = assertThrows(
            CoreConfigException.class,
            () -> load(values, Map.of())
        );
        assertTrue(failure.getMessage().contains("WAYFARER_REDIS_URI"));
        assertFalse(failure.getMessage().contains(SECRET));
    }

    @Test
    void validationFailureAfterResolutionDoesNotExposeSecrets() {
        Map<String, Object> values = validValues();
        values.put("mariadb.enabled", true);
        values.put("migration.enabled", true);
        values.put("migration.locations", List.of());
        Map<String, String> environment = Map.of(
            "WAYFARER_DB_URL", "jdbc:mariadb://example/wayfarer",
            "WAYFARER_DB_USERNAME", "wayfarer",
            "WAYFARER_DB_PASSWORD", SECRET
        );
        CoreConfigException failure = assertThrows(
            CoreConfigException.class,
            () -> load(values, environment)
        );
        assertFalse(failure.getMessage().contains(SECRET));
    }

    @Test
    void migrationRequiresMariaDb() {
        Map<String, Object> values = validValues();
        values.put("migration.enabled", true);
        assertMessage(values, Map.of(), "mariadb.enabled");
    }

    @Test
    void durableAuditRequiresMariaDb() {
        Map<String, Object> values = validValues();
        values.put("audit.enabled", true);
        assertMessage(values, Map.of(), "audit.enabled requires mariadb.enabled");
    }

    @Test
    void durableAuditRequiresMigration() {
        Map<String, Object> values = validValues();
        values.put("audit.enabled", true);
        values.put("mariadb.enabled", true);
        Map<String, String> environment = Map.of(
            "WAYFARER_DB_URL", "jdbc:mariadb://localhost/wayfarer",
            "WAYFARER_DB_USERNAME", "wayfarer",
            "WAYFARER_DB_PASSWORD", "test-password"
        );
        assertMessage(values, environment, "audit.enabled requires migration.enabled");
    }

    @Test
    void invalidMigrationLocationFailsClosed() {
        Map<String, Object> values = validValues();
        values.put("migration.locations", List.of("../runtime"));
        assertMessage(values, Map.of(), "Migration location is invalid");
    }

    private static CoreConfig load(
        Map<String, Object> values,
        Map<String, String> environment
    ) {
        return new CoreConfigLoader().load(
            new MapConfigView(values),
            new EnvironmentSecretResolver(environment)
        );
    }

    private static void assertMessage(
        Map<String, Object> values,
        Map<String, String> environment,
        String expected
    ) {
        CoreConfigException failure = assertThrows(
            CoreConfigException.class,
            () -> load(values, environment)
        );
        assertTrue(failure.getMessage().contains(expected));
    }

    private static Map<String, Object> validValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("config-version", 1);
        values.put("server-id", "test-server");
        values.put("shutdown-timeout.seconds", 15);
        values.put("executor.threads", 2);
        values.put("executor.thread-name-prefix", "Wayfarer-Test");
        values.put("audit.enabled", false);
        values.put("health.player-details", false);
        values.put("mariadb.enabled", false);
        values.put("mariadb.jdbc-url-env", "WAYFARER_DB_URL");
        values.put("mariadb.username-env", "WAYFARER_DB_USERNAME");
        values.put("mariadb.password-env", "WAYFARER_DB_PASSWORD");
        values.put("mariadb.maximum-pool-size", 8);
        values.put("mariadb.minimum-idle", 1);
        values.put("mariadb.connection-timeout-ms", 5000);
        values.put("redis.enabled", false);
        values.put("redis.uri-env", "WAYFARER_REDIS_URI");
        values.put("redis.connect-timeout-ms", 3000);
        values.put("migration.enabled", false);
        values.put("migration.locations", List.of("db/migration/core"));
        values.put("waymark.enabled", false);
        values.put("waymark.expected-provider", "RedisEconomy");
        values.put("waymark.operation-timeout-ms", 5000);
        return values;
    }
}
