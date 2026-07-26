package io.github.eariver.wayfarer.core.service;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTasks;

import java.util.function.Supplier;

public final class WayfarerServiceFactory {
    private WayfarerServiceFactory() {}

    public static WayfarerServices create(
        String serverId,
        int configVersion,
        Supplier<WayfarerLifecycleState> lifecycle,
        WayfarerTasks tasks,
        WayfarerHealth health
    ) {
        return new DefaultWayfarerServices(
            serverId,
            configVersion,
            lifecycle,
            tasks,
            health
        );
    }
}
