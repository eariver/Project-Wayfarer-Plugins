package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.core.transaction.WaymarkProviderSource;
import org.bukkit.plugin.ServicesManager;

import java.util.Objects;
import java.util.Optional;

public final class BukkitWaymarkProviderSource implements WaymarkProviderSource {
    private final ServicesManager servicesManager;

    public BukkitWaymarkProviderSource(ServicesManager servicesManager) {
        this.servicesManager = Objects.requireNonNull(servicesManager, "servicesManager");
    }

    @Override
    public Optional<WayfarerWaymarkProvider> discover() {
        return Optional.ofNullable(
            servicesManager.load(WayfarerWaymarkProvider.class)
        );
    }
}
