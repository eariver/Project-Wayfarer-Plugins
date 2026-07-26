package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BukkitServicePublisher implements ServicePublisher {
    private final ServicesManager servicesManager;
    private final JavaPlugin plugin;
    private boolean published;

    public BukkitServicePublisher(ServicesManager servicesManager, JavaPlugin plugin) {
        this.servicesManager = Objects.requireNonNull(servicesManager, "servicesManager");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public synchronized void publish(WayfarerServices services, WayfarerHealth health) {
        if (published) {
            throw new IllegalStateException("Wayfarer services are already published");
        }
        try {
            servicesManager.register(
                WayfarerHealth.class,
                health,
                plugin,
                ServicePriority.Normal
            );
            servicesManager.register(
                WayfarerServices.class,
                services,
                plugin,
                ServicePriority.Normal
            );
            published = true;
        } catch (RuntimeException failure) {
            servicesManager.unregisterAll(plugin);
            throw failure;
        }
    }

    @Override
    public synchronized void unpublish() {
        if (published) {
            servicesManager.unregisterAll(plugin);
            published = false;
        }
    }
}
