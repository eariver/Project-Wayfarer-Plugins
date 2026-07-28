package io.github.eariver.wayfarer.core.config;

import io.github.eariver.wayfarer.common.secret.SecretReferenceResolver;
import io.github.eariver.wayfarer.common.secret.SecretResolutionException;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import io.github.eariver.wayfarer.core.persistence.MigrationLocations;
import io.github.eariver.wayfarer.core.persistence.PersistenceException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class CoreConfigLoader {
    public static final int SUPPORTED_CONFIG_VERSION = 1;
    private static final Pattern SERVER_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Pattern PROVIDER_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Pattern REDIS_KEY_PREFIX = Pattern.compile("[a-z0-9][a-z0-9._-]{0,31}");
    private static final Set<String> RESERVED_SERVER_IDS = Set.of(
        "change_me",
        "change-me",
        "changeme",
        "default",
        "example"
    );

    public CoreConfig load(ConfigView source, SecretReferenceResolver secrets) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(secrets, "secrets");

        int version = requiredInt(source, "config-version");
        if (version != SUPPORTED_CONFIG_VERSION) {
            throw new CoreConfigException("Unsupported config-version: " + version);
        }

        String serverId = validateServerId(requiredString(source, "server-id"));

        Duration shutdownTimeout = durationSeconds(
            requiredInt(source, "shutdown-timeout.seconds"),
            "shutdown-timeout.seconds",
            1,
            300
        );
        int executorThreads = rangedInt(source, "executor.threads", 1, 64);
        int executorQueueCapacity = optionalRangedInt(
            source,
            "executor.queue-capacity",
            256,
            1,
            65_536
        );
        String threadNamePrefix = requiredString(source, "executor.thread-name-prefix");
        if (!threadNamePrefix.contains("Wayfarer")) {
            throw new CoreConfigException("executor.thread-name-prefix must contain Wayfarer");
        }

        boolean auditEnabled = requiredBoolean(source, "audit.enabled");
        boolean playerDetails = requiredBoolean(source, "health.player-details");

        CoreConfig.MariaDbSettings mariadb = loadMariaDb(source, secrets);
        CoreConfig.RedisSettings redis = null;
        try {
            redis = loadRedis(source, secrets);
            boolean migrationEnabled = requiredBoolean(source, "migration.enabled");
            if (migrationEnabled && !mariadb.enabled()) {
                throw new CoreConfigException("migration.enabled requires mariadb.enabled");
            }
            if (auditEnabled && !mariadb.enabled()) {
                throw new CoreConfigException("audit.enabled requires mariadb.enabled");
            }
            if (auditEnabled && !migrationEnabled) {
                throw new CoreConfigException("audit.enabled requires migration.enabled");
            }
            List<String> locations = stringList(source, "migration.locations");
            if (locations.isEmpty()) {
                throw new CoreConfigException("migration.locations must not be empty");
            }
            try {
                locations = MigrationLocations.canonicalize(locations);
            } catch (PersistenceException failure) {
                throw new CoreConfigException(failure.getMessage());
            }

            boolean waymarkEnabled = requiredBoolean(source, "waymark.enabled");
            String providerMode = optionalString(source, "waymark.provider-mode", "vault")
                .toLowerCase(Locale.ROOT);
            if (!providerMode.equals("vault")) {
                throw new CoreConfigException("waymark.provider-mode must be vault");
            }
            String expectedProvider = requiredString(source, "waymark.expected-provider");
            if (!PROVIDER_ID.matcher(expectedProvider).matches()) {
                throw new CoreConfigException(
                    "waymark.expected-provider must match " + PROVIDER_ID.pattern()
                );
            }
            Duration waymarkTimeout = durationMillis(
                rangedInt(source, "waymark.operation-timeout-ms", 100, 60_000)
            );

            return new CoreConfig(
                version,
                serverId,
                shutdownTimeout,
                new CoreConfig.ExecutorSettings(
                    executorThreads,
                    threadNamePrefix,
                    executorQueueCapacity
                ),
                new CoreConfig.AuditSettings(auditEnabled),
                new CoreConfig.HealthSettings(playerDetails),
                mariadb,
                redis,
                new CoreConfig.MigrationSettings(migrationEnabled, locations),
                new CoreConfig.WaymarkSettings(
                    waymarkEnabled,
                    providerMode,
                    expectedProvider,
                    waymarkTimeout
                )
            );
        } catch (RuntimeException failure) {
            mariadb.close();
            if (redis != null) {
                redis.close();
            }
            throw failure;
        }
    }

    private static CoreConfig.MariaDbSettings loadMariaDb(
        ConfigView source,
        SecretReferenceResolver secrets
    ) {
        boolean enabled = requiredBoolean(source, "mariadb.enabled");
        String jdbcReference = requiredString(source, "mariadb.jdbc-url-env");
        String usernameReference = requiredString(source, "mariadb.username-env");
        String passwordReference = requiredString(source, "mariadb.password-env");
        int maximumPoolSize = rangedInt(source, "mariadb.maximum-pool-size", 1, 64);
        int minimumIdle = rangedInt(source, "mariadb.minimum-idle", 0, 64);
        if (minimumIdle > maximumPoolSize) {
            throw new CoreConfigException("mariadb.minimum-idle must not exceed maximum-pool-size");
        }
        Duration timeout = durationMillis(
            rangedInt(source, "mariadb.connection-timeout-ms", 250, 60_000)
        );

        SecretValue jdbcUrl = null;
        SecretValue username = null;
        SecretValue password = null;
        try {
            if (enabled) {
                jdbcUrl = resolve(secrets, jdbcReference);
                username = resolve(secrets, usernameReference);
                password = resolve(secrets, passwordReference);
            }
            return new CoreConfig.MariaDbSettings(
                enabled,
                jdbcReference,
                usernameReference,
                passwordReference,
                maximumPoolSize,
                minimumIdle,
                timeout,
                jdbcUrl,
                username,
                password
            );
        } catch (RuntimeException failure) {
            closeSecret(jdbcUrl);
            closeSecret(username);
            closeSecret(password);
            throw failure;
        }
    }

    private static CoreConfig.RedisSettings loadRedis(
        ConfigView source,
        SecretReferenceResolver secrets
    ) {
        boolean enabled = requiredBoolean(source, "redis.enabled");
        String uriReference = requiredString(source, "redis.uri-env");
        Duration timeout = durationMillis(
            rangedInt(source, "redis.connect-timeout-ms", 100, 60_000)
        );
        Duration operationTimeout = durationMillis(optionalRangedInt(
            source,
            "redis.operation-timeout-ms",
            3_000,
            100,
            60_000
        ));
        Duration cacheMaximumTtl = Duration.ofSeconds(optionalRangedInt(
            source,
            "redis.cache-maximum-ttl-seconds",
            3_600,
            1,
            86_400
        ));
        Duration lockMaximumLease = Duration.ofSeconds(optionalRangedInt(
            source,
            "redis.lock-maximum-lease-seconds",
            30,
            1,
            300
        ));
        String keyPrefix = optionalString(source, "redis.key-prefix", "wayfarer");
        if (!REDIS_KEY_PREFIX.matcher(keyPrefix).matches()) {
            throw new CoreConfigException(
                "redis.key-prefix must match " + REDIS_KEY_PREFIX.pattern()
            );
        }
        SecretValue uri = enabled ? resolve(secrets, uriReference) : null;
        return new CoreConfig.RedisSettings(
            enabled,
            uriReference,
            timeout,
            operationTimeout,
            cacheMaximumTtl,
            lockMaximumLease,
            keyPrefix,
            uri
        );
    }

    private static SecretValue resolve(SecretReferenceResolver resolver, String reference) {
        try {
            return resolver.resolve(reference);
        } catch (SecretResolutionException failure) {
            throw new CoreConfigException(failure.getMessage());
        }
    }

    private static String validateServerId(String value) {
        String serverId = value.trim();
        if (!SERVER_ID.matcher(serverId).matches()) {
            throw new CoreConfigException("server-id must match " + SERVER_ID.pattern());
        }
        if (RESERVED_SERVER_IDS.contains(serverId.toLowerCase(Locale.ROOT))) {
            throw new CoreConfigException("server-id must be explicitly configured");
        }
        return serverId;
    }

    private static String requiredString(ConfigView source, String path) {
        Object value = required(source, path);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new CoreConfigException(path + " must be a non-blank string");
        }
        return text.trim();
    }

    private static String optionalString(
        ConfigView source,
        String path,
        String defaultValue
    ) {
        return source.contains(path) ? requiredString(source, path) : defaultValue;
    }

    private static int requiredInt(ConfigView source, String path) {
        Object value = required(source, path);
        if (!(value instanceof Number number)) {
            throw new CoreConfigException(path + " must be an integer");
        }
        double decimal = number.doubleValue();
        int integer = number.intValue();
        if (decimal != integer) {
            throw new CoreConfigException(path + " must be an integer");
        }
        return integer;
    }

    private static int rangedInt(ConfigView source, String path, int minimum, int maximum) {
        int value = requiredInt(source, path);
        if (value < minimum || value > maximum) {
            throw new CoreConfigException(
                path + " must be between " + minimum + " and " + maximum
            );
        }
        return value;
    }

    private static int optionalRangedInt(
        ConfigView source,
        String path,
        int defaultValue,
        int minimum,
        int maximum
    ) {
        if (!source.contains(path)) {
            return defaultValue;
        }
        return rangedInt(source, path, minimum, maximum);
    }

    private static boolean requiredBoolean(ConfigView source, String path) {
        Object value = required(source, path);
        if (!(value instanceof Boolean flag)) {
            throw new CoreConfigException(path + " must be true or false");
        }
        return flag;
    }

    private static List<String> stringList(ConfigView source, String path) {
        Object value = required(source, path);
        if (!(value instanceof List<?> values)) {
            throw new CoreConfigException(path + " must be a list");
        }
        List<String> result = new ArrayList<>(values.size());
        for (Object item : values) {
            if (!(item instanceof String text) || text.isBlank()) {
                throw new CoreConfigException(path + " entries must be non-blank strings");
            }
            result.add(text.trim());
        }
        return List.copyOf(result);
    }

    private static Object required(ConfigView source, String path) {
        if (!source.contains(path)) {
            throw new CoreConfigException("Missing required configuration: " + path);
        }
        return source.get(path);
    }

    private static Duration durationSeconds(int seconds, String path, int minimum, int maximum) {
        if (seconds < minimum || seconds > maximum) {
            throw new CoreConfigException(
                path + " must be between " + minimum + " and " + maximum
            );
        }
        return Duration.ofSeconds(seconds);
    }

    private static Duration durationMillis(int milliseconds) {
        return Duration.ofMillis(milliseconds);
    }

    private static void closeSecret(SecretValue secret) {
        if (secret != null) {
            secret.close();
        }
    }
}
