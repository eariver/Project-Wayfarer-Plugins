package io.github.eariver.wayfarer.main.config;

import io.github.eariver.wayfarer.main.domain.EvolutionPlan;
import io.github.eariver.wayfarer.main.domain.ProgressPolicy;
import io.github.eariver.wayfarer.main.domain.RepairPricing;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record MainModuleConfig(
    int configVersion,
    Set<String> progressWorlds,
    ProgressPolicy progressPolicy,
    EvolutionPlan evolutionPlan,
    EvolutionPlan.EnchantmentCaps enchantmentCaps,
    RepairPricing repairPricing,
    Duration checkpointInterval,
    boolean debugCommandsEnabled,
    DatabaseConfig database,
    Duration disableTimeout
) {
    private static final Set<String> REQUIRED_PROGRESS_WORLDS =
        Set.of("resource", "resource_nether", "resource_end");
    private static final Set<String> REQUIRED_WEIGHTS = Set.of(
        "COBBLESTONE", "COBBLED_DEEPSLATE", "STONE", "GRANITE", "DIORITE",
        "ANDESITE", "TUFF", "CALCITE", "NETHERRACK", "BLACKSTONE", "BASALT",
        "SMOOTH_BASALT", "DEEPSLATE", "END_STONE", "OBSIDIAN",
        "CRYING_OBSIDIAN"
    );
    private static final Set<String> REQUIRED_ORE_MULTIPLIERS = Set.of(
        "COAL", "NETHER_QUARTZ", "COPPER", "REDSTONE", "IRON", "LAPIS",
        "NETHER_GOLD", "GOLD", "DIAMOND", "EMERALD", "ANCIENT_DEBRIS"
    );

    public static MainModuleConfig load(FileConfiguration config) {
        int version = config.getInt("config-version", -1);
        if (version != 1 || !"MAIN".equals(config.getString("runtime-role"))) {
            throw new ConfigException("Main config version or runtime role is invalid");
        }
        Set<String> worlds = Set.copyOf(config.getStringList("worlds.progress-allowlist"));
        if (!worlds.equals(REQUIRED_PROGRESS_WORLDS)) {
            throw new ConfigException(
                "Main progress world allowlist must use the approved exact set"
            );
        }

        int scale = requiredInt(config, "progress.scale", 1, 1_000_000);
        BigDecimal fallback = requiredDecimal(config, "progress.fallback-weight");
        Map<String, BigDecimal> weights = requiredMap(
            config,
            "progress.weights",
            REQUIRED_WEIGHTS
        );
        Map<String, BigDecimal> oreMultipliers = requiredMap(
            config,
            "progress.ore-multipliers",
            REQUIRED_ORE_MULTIPLIERS
        );
        ProgressPolicy progressPolicy;
        EvolutionPlan evolutionPlan;
        EvolutionPlan.EnchantmentCaps caps;
        RepairPricing repairPricing;
        try {
            progressPolicy = new ProgressPolicy(
                worlds,
                scale,
                fallback,
                weights,
                oreMultipliers
            );
            long stone = fixedUnits(config, "evolution.material-thresholds.stone", scale);
            long iron = fixedUnits(config, "evolution.material-thresholds.iron", scale);
            long diamond = fixedUnits(
                config,
                "evolution.material-thresholds.diamond",
                scale
            );
            long base = fixedUnits(config, "evolution.enchant-progression.base", scale);
            long linear = fixedUnits(
                config,
                "evolution.enchant-progression.linear",
                scale
            );
            long quadratic = fixedUnits(
                config,
                "evolution.enchant-progression.quadratic",
                scale
            );
            String revision = "main-" + version + "-"
                + Integer.toUnsignedString(config.saveToString().hashCode());
            evolutionPlan = new EvolutionPlan(
                revision,
                stone,
                iron,
                diamond,
                base,
                linear,
                quadratic
            );
            caps = new EvolutionPlan.EnchantmentCaps(
                requiredInt(config, "evolution.caps.efficiency", 0, 255),
                requiredInt(config, "evolution.caps.unbreaking", 0, 255),
                requiredInt(config, "evolution.caps.fortune", 0, 255),
                requiredInt(config, "evolution.caps.silk-touch", 0, 1)
            );
            repairPricing = new RepairPricing(
                requiredLong(config, "repair.base-cost-wm", 0, Long.MAX_VALUE),
                requiredDecimal(
                    config,
                    "repair.evolution-multiplier-per-step"
                ).doubleValue(),
                requiredDecimal(config, "repair.minimum-charge-ratio").doubleValue(),
                requiredLong(
                    config,
                    "repair.broken.flat-surcharge-wm",
                    0,
                    Long.MAX_VALUE
                ),
                requiredLong(
                    config,
                    "repair.broken.surcharge-per-evolution",
                    0,
                    Long.MAX_VALUE
                )
            );
        } catch (ArithmeticException | IllegalArgumentException failure) {
            throw new ConfigException("Main domain configuration is invalid");
        }

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
        long checkpointSeconds = requiredLong(
            config,
            "checkpoint.interval-seconds",
            1,
            86_400
        );
        long disableSeconds = requiredLong(
            config,
            "checkpoint.disable-timeout-seconds",
            1,
            15
        );
        if (!config.isBoolean("debug-commands.enabled")) {
            throw new ConfigException("Main debug command setting is missing");
        }
        return new MainModuleConfig(
            version,
            worlds,
            progressPolicy,
            evolutionPlan,
            caps,
            repairPricing,
            Duration.ofSeconds(checkpointSeconds),
            config.getBoolean("debug-commands.enabled"),
            database,
            Duration.ofSeconds(disableSeconds)
        );
    }

    private static Map<String, BigDecimal> requiredMap(
        FileConfiguration config,
        String path,
        Set<String> requiredKeys
    ) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null || !section.getKeys(false).containsAll(requiredKeys)) {
            throw new ConfigException("Main progress baseline is incomplete");
        }
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            BigDecimal value = decimal(section.get(key), path + "." + key);
            if (value.signum() <= 0) {
                throw new ConfigException("Main progress values must be positive");
            }
            result.put(key, value);
        }
        return Map.copyOf(result);
    }

    private static long fixedUnits(
        FileConfiguration config,
        String path,
        int scale
    ) {
        BigDecimal value = requiredDecimal(config, path);
        if (value.signum() <= 0) {
            throw new ConfigException("Main evolution value must be positive");
        }
        return value.multiply(BigDecimal.valueOf(scale))
            .setScale(0, RoundingMode.UNNECESSARY)
            .longValueExact();
    }

    private static BigDecimal requiredDecimal(FileConfiguration config, String path) {
        return decimal(config.get(path), path);
    }

    private static BigDecimal decimal(Object value, String path) {
        if (!(value instanceof Number) && !(value instanceof String)) {
            throw new ConfigException("Main numeric setting is missing: " + path);
        }
        try {
            BigDecimal result = new BigDecimal(value.toString());
            if (!result.toString().equalsIgnoreCase("NaN")) {
                return result;
            }
        } catch (NumberFormatException failure) {
            throw new ConfigException("Main numeric setting is invalid: " + path);
        }
        throw new ConfigException("Main numeric setting is invalid: " + path);
    }

    private static int requiredInt(
        FileConfiguration config,
        String path,
        int minimum,
        int maximum
    ) {
        long value = requiredLong(config, path, minimum, maximum);
        return Math.toIntExact(value);
    }

    private static long requiredLong(
        FileConfiguration config,
        String path,
        long minimum,
        long maximum
    ) {
        Object raw = config.get(path);
        if (!(raw instanceof Number number)) {
            throw new ConfigException("Main integer setting is missing: " + path);
        }
        long value = number.longValue();
        if (value < minimum || value > maximum
            || new BigDecimal(number.toString()).compareTo(BigDecimal.valueOf(value)) != 0) {
            throw new ConfigException("Main integer setting is invalid: " + path);
        }
        return value;
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
        int minimumIdle,
        long connectionTimeoutMillis
    ) {
        public DatabaseConfig {
            if (maximumPoolSize < 1 || maximumPoolSize > 3
                || minimumIdle < 0 || minimumIdle > maximumPoolSize
                || connectionTimeoutMillis < 250
                || connectionTimeoutMillis > 30_000) {
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
