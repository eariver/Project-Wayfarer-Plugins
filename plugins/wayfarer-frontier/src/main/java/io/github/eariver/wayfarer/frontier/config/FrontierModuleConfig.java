package io.github.eariver.wayfarer.frontier.config;

import io.github.eariver.wayfarer.frontier.domain.FrontierShopCatalog;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public record FrontierModuleConfig(
    int configVersion,
    String exactWorldName,
    String leafGrappleVersion,
    LoadoutDefinition loadout,
    FrontierShopCatalog shopCatalog,
    LaunchpadDefinition launchpad,
    PortalPolicy portalPolicy,
    FeatureAvailability features,
    Duration checkpointInterval,
    DatabaseConfig database,
    Duration disableTimeout
) {
    public static FrontierModuleConfig load(FileConfiguration config) {
        int version = config.getInt("config-version", -1);
        if (version != 1 || !"FRONTIER".equals(config.getString("runtime-role"))) {
            throw new ConfigException("Frontier config version or runtime role is invalid");
        }
        String root = "themes.worlds-beyond.";
        List<String> worlds = config.getStringList(root + "worlds.allowlist");
        if (!worlds.equals(List.of("frontier_iris"))) {
            throw new ConfigException(
                "Frontier world allowlist must be exactly frontier_iris"
            );
        }
        String leafVersion = requiredString(config, root + "leafgrapple.expected-version");
        if (!"1.0.2".equals(leafVersion)) {
            throw new ConfigException(
                "LeafGrapple version must match the reviewed adapter"
            );
        }
        String waystone = requiredString(config, root + "navigation.waystone");
        if (!"DEFERRED_BY_REQUIREMENT".equals(waystone)) {
            throw new ConfigException("Waystone must remain deferred in V0.0.2");
        }

        LoadoutDefinition loadout = new LoadoutDefinition(
            requiredInt(config, root + "loadout.initial-launchpad-amount", 0, 64),
            requiredBoolean(config, root + "loadout.initial-grant-once"),
            requiredBoolean(config, root + "loadout.reissue-launchpad-on-loss")
        );
        FrontierShopCatalog catalog = new FrontierShopCatalog(Map.of(
            "launchpad",
            new FrontierShopCatalog.Offer(
                "launchpad",
                PendingDelivery.ItemType.LAUNCHPAD,
                requiredInt(config, root + "shop.launchpad.amount", 1, 64),
                requiredLong(config, root + "shop.launchpad.price-wm", 0, Long.MAX_VALUE),
                0
            ),
            "firework_rocket",
            new FrontierShopCatalog.Offer(
                "firework_rocket",
                PendingDelivery.ItemType.FIREWORK_ROCKET,
                requiredInt(config, root + "shop.firework-rocket.amount", 1, 64),
                requiredLong(
                    config,
                    root + "shop.firework-rocket.price-wm",
                    0,
                    Long.MAX_VALUE
                ),
                requiredInt(
                    config,
                    root + "shop.firework-rocket.flight-duration",
                    1,
                    3
                )
            )
        ));
        LaunchpadDefinition launchpad = new LaunchpadDefinition(
            requiredString(config, root + "launchpad.material"),
            requiredBoolean(config, root + "launchpad.disable-while-sneaking"),
            requiredInt(config, root + "launchpad.max-successful-uses", 1, 10_000),
            requiredDouble(config, root + "launchpad.horizontal-velocity", 0.01, 20),
            requiredDouble(config, root + "launchpad.vertical-velocity", 0.01, 20),
            requiredBoolean(config, root + "launchpad.auto-deploy-elytra"),
            Duration.ofSeconds(requiredLong(
                config,
                root + "launchpad.use-cooldown-seconds",
                0,
                3_600
            )),
            Duration.ofDays(requiredLong(
                config,
                root + "launchpad.expire-after-days",
                1,
                3_650
            )),
            requiredBoolean(config, root + "launchpad.extend-expiration-on-use"),
            requiredBoolean(config, root + "launchpad.allow-player-break"),
            requiredBoolean(config, root + "launchpad.drop-on-player-break"),
            requiredInt(config, root + "launchpad.max-active-per-player", 0, 10_000)
        );
        PortalPolicy portals = new PortalPolicy(
            deny(config, root + "portals.nether"),
            deny(config, root + "portals.end"),
            deny(config, root + "portals.cross-theme-fallback")
        );
        FeatureAvailability features = new FeatureAvailability(
            requiredBoolean(config, root + "enabled"),
            true,
            false
        );

        DatabaseConfig database = new DatabaseConfig(
            reference(config.getString("persistence.jdbc-url-ref")),
            reference(config.getString("persistence.username-ref")),
            reference(config.getString("persistence.password-ref")),
            requiredInt(config, "persistence.maximum-pool-size", 1, 3),
            requiredInt(config, "persistence.minimum-idle", 0, 3),
            requiredLong(
                config,
                "persistence.connection-timeout-millis",
                250,
                30_000
            )
        );
        Duration checkpoint = Duration.ofSeconds(requiredLong(
            config,
            "checkpoint.interval-seconds",
            1,
            86_400
        ));
        Duration disable = Duration.ofSeconds(requiredLong(
            config,
            "checkpoint.disable-timeout-seconds",
            1,
            15
        ));
        return new FrontierModuleConfig(
            version,
            "frontier_iris",
            leafVersion,
            loadout,
            catalog,
            launchpad,
            portals,
            features,
            checkpoint,
            database,
            disable
        );
    }

    private static boolean deny(FileConfiguration config, String path) {
        if (!"DENY".equals(config.getString(path))) {
            throw new ConfigException("Frontier portal policy must fail closed");
        }
        return true;
    }

    private static String requiredString(FileConfiguration config, String path) {
        String value = config.getString(path);
        if (value == null || value.isBlank()) {
            throw new ConfigException("Frontier string setting is missing: " + path);
        }
        return value;
    }

    private static boolean requiredBoolean(FileConfiguration config, String path) {
        if (!config.isBoolean(path)) {
            throw new ConfigException("Frontier boolean setting is missing: " + path);
        }
        return config.getBoolean(path);
    }

    private static double requiredDouble(
        FileConfiguration config,
        String path,
        double minimum,
        double maximum
    ) {
        Object raw = config.get(path);
        if (!(raw instanceof Number number)) {
            throw new ConfigException("Frontier numeric setting is missing: " + path);
        }
        double value = number.doubleValue();
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new ConfigException("Frontier numeric setting is invalid: " + path);
        }
        return value;
    }

    private static int requiredInt(
        FileConfiguration config,
        String path,
        int minimum,
        int maximum
    ) {
        return Math.toIntExact(requiredLong(config, path, minimum, maximum));
    }

    private static long requiredLong(
        FileConfiguration config,
        String path,
        long minimum,
        long maximum
    ) {
        Object raw = config.get(path);
        if (!(raw instanceof Number number)) {
            throw new ConfigException("Frontier integer setting is missing: " + path);
        }
        long value = number.longValue();
        if (value < minimum || value > maximum
            || number.doubleValue() != (double) value) {
            throw new ConfigException("Frontier integer setting is invalid: " + path);
        }
        return value;
    }

    private static String reference(String value) {
        if (value == null || !value.matches("[A-Z][A-Z0-9_]{2,95}")) {
            throw new ConfigException("Frontier persistence secret reference is invalid");
        }
        return value;
    }

    public record LoadoutDefinition(
        int initialLaunchpadAmount,
        boolean initialGrantOnce,
        boolean reissueLaunchpadOnLoss
    ) {
        public LoadoutDefinition {
            if (initialLaunchpadAmount < 0 || !initialGrantOnce
                || reissueLaunchpadOnLoss) {
                throw new ConfigException("Frontier loadout policy is invalid");
            }
        }
    }

    public record LaunchpadDefinition(
        String material,
        boolean disableWhileSneaking,
        int maximumSuccessfulUses,
        double horizontalVelocity,
        double verticalVelocity,
        boolean autoDeployElytra,
        Duration cooldown,
        Duration expiration,
        boolean extendExpirationOnUse,
        boolean allowPlayerBreak,
        boolean dropOnPlayerBreak,
        int maximumActivePerPlayer
    ) {
        public LaunchpadDefinition {
            if (!"LIGHT_WEIGHTED_PRESSURE_PLATE".equals(material)
                || maximumSuccessfulUses < 1
                || horizontalVelocity <= 0
                || verticalVelocity <= 0
                || cooldown.isNegative() || cooldown.isZero()
                || expiration.isNegative() || expiration.isZero()
                || !disableWhileSneaking || !extendExpirationOnUse
                || !allowPlayerBreak || dropOnPlayerBreak
                || maximumActivePerPlayer < 0) {
                throw new ConfigException("Frontier launchpad definition is invalid");
            }
        }
    }

    public record PortalPolicy(
        boolean denyNether,
        boolean denyEnd,
        boolean denyCrossThemeFallback
    ) {}

    public record FeatureAvailability(
        boolean worldsBeyond,
        boolean leafGrappleRequired,
        boolean waystone
    ) {}

    public record DatabaseConfig(
        String jdbcUrlReference,
        String usernameReference,
        String passwordReference,
        int maximumPoolSize,
        int minimumIdle,
        long connectionTimeoutMillis
    ) {
        public DatabaseConfig {
            if (maximumPoolSize < 1 || maximumPoolSize > 3
                || minimumIdle < 0 || minimumIdle > maximumPoolSize
                || connectionTimeoutMillis < 250
                || connectionTimeoutMillis > 30_000) {
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
