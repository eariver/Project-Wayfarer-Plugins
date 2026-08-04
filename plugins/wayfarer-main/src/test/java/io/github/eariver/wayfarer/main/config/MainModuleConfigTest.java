package io.github.eariver.wayfarer.main.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

final class MainModuleConfigTest {
    @Test
    void loadsSanitizedDefaultAndRejectsRoleOrWorldBroadening() {
        YamlConfiguration config = defaultConfig();
        MainModuleConfig loaded = MainModuleConfig.load(config);
        assertEquals(1, loaded.configVersion());
        assertEquals(
            loaded.evolutionPlan().configRevision(),
            loaded.configRevision()
        );
        assertEquals(1_000L, loaded.progressPolicy().unitsFor("GRANITE", null));
        assertEquals(2_000L,
            loaded.progressPolicy().unitsFor("CRYING_OBSIDIAN", null));
        assertEquals(1_500L,
            loaded.progressPolicy().unitsFor("STONE", "NETHER_QUARTZ"));
        assertEquals(2_240_000L, loaded.evolutionPlan().evaluate(
            1_200_000,
            io.github.eariver.wayfarer.main.domain.GrowthTool.Branch.FORTUNE,
            loaded.enchantmentCaps()
        ).nextThresholdUnits());
        assertEquals(0, loaded.database().minimumIdle());
        assertEquals(java.time.Duration.ofSeconds(300), loaded.checkpointInterval());
        assertEquals(false, loaded.debugCommandsEnabled());

        config.set("runtime-role", "FRONTIER");
        YamlConfiguration wrongRole = config;
        assertThrows(MainModuleConfig.ConfigException.class,
            () -> MainModuleConfig.load(wrongRole));

        config = defaultConfig();
        config.set("worlds.progress-allowlist", java.util.List.of("resource", "resource_backup"));
        YamlConfiguration broadened = config;
        assertThrows(MainModuleConfig.ConfigException.class,
            () -> MainModuleConfig.load(broadened));
    }

    @Test
    void rejectsMissingNegativeOverflowAndNonMonotonicDomainValues() {
        YamlConfiguration missing = defaultConfig();
        missing.set("progress.weights.GRANITE", null);
        assertThrows(MainModuleConfig.ConfigException.class,
            () -> MainModuleConfig.load(missing));

        YamlConfiguration negative = defaultConfig();
        negative.set("progress.fallback-weight", new BigDecimal("-1"));
        assertThrows(MainModuleConfig.ConfigException.class,
            () -> MainModuleConfig.load(negative));

        YamlConfiguration nonMonotonic = defaultConfig();
        nonMonotonic.set("evolution.material-thresholds.iron", 50);
        assertThrows(MainModuleConfig.ConfigException.class,
            () -> MainModuleConfig.load(nonMonotonic));

        YamlConfiguration overflow = defaultConfig();
        overflow.set("evolution.enchant-progression.base", "999999999999999999999");
        assertThrows(MainModuleConfig.ConfigException.class,
            () -> MainModuleConfig.load(overflow));
    }

    private static YamlConfiguration defaultConfig() {
        var stream = MainModuleConfigTest.class.getResourceAsStream("/config.yml");
        if (stream == null) {
            throw new AssertionError("Default Main config is missing");
        }
        return YamlConfiguration.loadConfiguration(
            new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
    }
}
