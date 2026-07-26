package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.core.config.ConfigView;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public final class BukkitConfigView implements ConfigView {
    private final FileConfiguration config;

    public BukkitConfigView(FileConfiguration config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public boolean contains(String path) {
        return config.contains(path);
    }

    @Override
    public Object get(String path) {
        return config.get(path);
    }
}
