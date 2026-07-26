package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.*;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class WayfarerCorePlugin extends JavaPlugin {
    private WayfarerServices services;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        services = new ScaffoldServices();
        getServer().getServicesManager().register(
            WayfarerServices.class, services, this, ServicePriority.Normal
        );
        getLogger().warning("Scaffold only: implement the approved design specification before runtime integration.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        services = null;
    }

    private static final class ScaffoldServices implements WayfarerServices {
        private final WayfarerHealth health = new WayfarerHealth() {
            @Override public Status overall() { return Status.DEGRADED; }
            @Override public Map<String, Status> components() {
                return Map.of("scaffold", Status.DEGRADED);
            }
        };

        @Override public WayfarerDatabase database() { throw unavailable(); }
        @Override public WayfarerAudit audit() { throw unavailable(); }
        @Override public WayfarerTransactions transactions() { throw unavailable(); }
        @Override public WayfarerWaymark waymark() { throw unavailable(); }
        @Override public WayfarerItemIdentity itemIdentity() { throw unavailable(); }
        @Override public WayfarerTasks tasks() { throw unavailable(); }
        @Override public WayfarerHealth health() { return health; }

        private static IllegalStateException unavailable() {
            return new IllegalStateException("Wayfarer_Core scaffold service is not implemented");
        }
    }
}
