package io.github.eariver.wayfarer.frontier.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

final class FrontierModuleConfigTest {
    @Test
    void loadsDefaultAndKeepsWorldAndWaystoneFailClosed() {
        YamlConfiguration config = defaultConfig();
        FrontierModuleConfig loaded = FrontierModuleConfig.load(config);
        assertEquals("frontier_iris", loaded.exactWorldName());
        assertEquals(2, loaded.loadout().initialLaunchpadAmount());
        assertEquals(30, loaded.shopCatalog().findV002("launchpad")
            .orElseThrow().priceWaymark());
        assertEquals(3, loaded.shopCatalog().findV002("firework_rocket")
            .orElseThrow().flightDuration());
        assertEquals("LIGHT_WEIGHTED_PRESSURE_PLATE", loaded.launchpad().material());
        assertEquals(3, loaded.launchpad().maximumSuccessfulUses());
        assertEquals(java.time.Duration.ofDays(30), loaded.launchpad().expiration());
        assertEquals(0, loaded.database().minimumIdle());
        assertEquals(java.time.Duration.ofSeconds(300), loaded.checkpointInterval());
        assertEquals(true, loaded.portalPolicy().denyNether());
        assertEquals(true, loaded.features().worldsBeyond());

        config.set(
            "themes.worlds-beyond.worlds.allowlist",
            java.util.List.of("frontier_iris", "frontier_iris_nether")
        );
        YamlConfiguration broadened = config;
        assertThrows(FrontierModuleConfig.ConfigException.class,
            () -> FrontierModuleConfig.load(broadened));

        config = defaultConfig();
        config.set("themes.worlds-beyond.navigation.waystone", "ENABLED");
        YamlConfiguration enabledWaystone = config;
        assertThrows(FrontierModuleConfig.ConfigException.class,
            () -> FrontierModuleConfig.load(enabledWaystone));
    }

    @Test
    void rejectsMissingAndInvalidRuntimeDefinitions() {
        YamlConfiguration missing = defaultConfig();
        missing.set("themes.worlds-beyond.shop.launchpad.amount", null);
        assertThrows(FrontierModuleConfig.ConfigException.class,
            () -> FrontierModuleConfig.load(missing));

        YamlConfiguration invalidUses = defaultConfig();
        invalidUses.set("themes.worlds-beyond.launchpad.max-successful-uses", 0);
        assertThrows(FrontierModuleConfig.ConfigException.class,
            () -> FrontierModuleConfig.load(invalidUses));

        YamlConfiguration unsafeDrop = defaultConfig();
        unsafeDrop.set("themes.worlds-beyond.launchpad.drop-on-player-break", true);
        assertThrows(FrontierModuleConfig.ConfigException.class,
            () -> FrontierModuleConfig.load(unsafeDrop));

        YamlConfiguration unknownVersion = defaultConfig();
        unknownVersion.set("config-version", 2);
        assertThrows(FrontierModuleConfig.ConfigException.class,
            () -> FrontierModuleConfig.load(unknownVersion));
    }

    private static YamlConfiguration defaultConfig() {
        var stream = FrontierModuleConfigTest.class.getResourceAsStream("/config.yml");
        if (stream == null) {
            throw new AssertionError("Default Frontier config is missing");
        }
        return YamlConfiguration.loadConfiguration(
            new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
    }
}
