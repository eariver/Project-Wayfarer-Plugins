package io.github.eariver.wayfarer.main.config;

import java.time.Duration;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;

public record MainModuleConfig(
    int configVersion,
    Set<String> progressWorlds,
    DatabaseConfig database,
    Duration disableTimeout
) {
    private static final Set<String> REQUIRED_PROGRESS_WORLDS =
        Set.of("resource", "resource_nether", "resource_end");

    public static MainModuleConfig load(FileConfiguration config) {
        int version = config.getInt("config-version", -1);
        if (version != 1 || !"MAIN".equals(config.getString("runtime-role"))) {
            throw new ConfigException("Main config version or runtime role is invalid");
        }
        Set<String> worlds = Set.copyOf(config.getStringList("worlds.progress-allowlist"));
        if (!worlds.equals(REQUIRED_PROGRESS_WORLDS)) {
            throw new ConfigException("Main progress world allowlist must use the approved exact set");
        }
        DatabaseConfig database = new DatabaseConfig(
            reference(config.getString("persistence.jdbc-url-ref")),
            reference(config.getString("persistence.username-ref")),
            reference(config.getString("persistence.password-ref")),
            config.getInt("persistence.maximum-pool-size", -1),
            config.getLong("persistence.connection-timeout-millis", -1)
        );
        long disableSeconds = config.getLong("checkpoint.disable-timeout-seconds", -1);
        if (disableSeconds < 1 || disableSeconds > 60) {
            throw new ConfigException("Main disable timeout is invalid");
        }
        return new MainModuleConfig(version, worlds, database, Duration.ofSeconds(disableSeconds));
    }

    private static String reference(String value) {
        if (value == null || !value.matches("[A-Z][A-Z0-9_]{2,95}")) {
            throw new ConfigException("Main persistence secret reference is invalid");
        }
        return value;
    }

    public record DatabaseConfig(
        String jdbcUrlReference,
        String usernameReference,
        String passwordReference,
        int maximumPoolSize,
        long connectionTimeoutMillis
    ) {
        public DatabaseConfig {
            if (maximumPoolSize < 1 || maximumPoolSize > 4
                || connectionTimeoutMillis < 250 || connectionTimeoutMillis > 30_000) {
                throw new ConfigException("Main database pool bounds are invalid");
            }
        }
    }

    public static final class ConfigException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ConfigException(String message) {
            super(message);
        }
    }
}
