package io.github.eariver.wayfarer.frontier.config;

import java.time.Duration;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

public record FrontierModuleConfig(
    int configVersion,
    String exactWorldName,
    String leafGrappleVersion,
    DatabaseConfig database,
    Duration disableTimeout
) {
    public static FrontierModuleConfig load(FileConfiguration config) {
        int version = config.getInt("config-version", -1);
        if (version != 1 || !"FRONTIER".equals(config.getString("runtime-role"))) {
            throw new ConfigException("Frontier config version or runtime role is invalid");
        }
        List<String> worlds =
            config.getStringList("themes.worlds-beyond.worlds.allowlist");
        if (!worlds.equals(List.of("frontier_iris"))) {
            throw new ConfigException("Frontier world allowlist must be exactly frontier_iris");
        }
        String leafVersion =
            config.getString("themes.worlds-beyond.leafgrapple.expected-version");
        if (!"1.0.2".equals(leafVersion)) {
            throw new ConfigException("LeafGrapple version must match the reviewed adapter");
        }
        String waystone =
            config.getString("themes.worlds-beyond.navigation.waystone");
        if (!"DEFERRED_BY_REQUIREMENT".equals(waystone)) {
            throw new ConfigException("Waystone must remain deferred in V0.0.2");
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
            throw new ConfigException("Frontier disable timeout is invalid");
        }
        return new FrontierModuleConfig(
            version,
            "frontier_iris",
            leafVersion,
            database,
            Duration.ofSeconds(disableSeconds)
        );
    }

    private static String reference(String value) {
        if (value == null || !value.matches("[A-Z][A-Z0-9_]{2,95}")) {
            throw new ConfigException("Frontier persistence secret reference is invalid");
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
                throw new ConfigException("Frontier database pool bounds are invalid");
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
