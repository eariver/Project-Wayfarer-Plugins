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
        assertEquals("frontier_iris", FrontierModuleConfig.load(config).exactWorldName());

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
