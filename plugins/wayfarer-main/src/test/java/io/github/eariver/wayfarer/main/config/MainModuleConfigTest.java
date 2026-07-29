package io.github.eariver.wayfarer.main.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

final class MainModuleConfigTest {
    @Test
    void loadsSanitizedDefaultAndRejectsRoleOrWorldBroadening() {
        YamlConfiguration config = defaultConfig();
        assertEquals(1, MainModuleConfig.load(config).configVersion());

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
