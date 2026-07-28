package io.github.eariver.wayfarer.core.service;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymark;

import java.util.function.Supplier;

public final class WayfarerServiceFactory {
    private WayfarerServiceFactory() {}

    public static WayfarerServices create(
        String serverId,
        int configVersion,
        Supplier<WayfarerLifecycleState> lifecycle,
        WayfarerTasks tasks,
        WayfarerHealth health,
        WayfarerAudit audit,
        WayfarerItemIdentity itemIdentity,
        WayfarerTransactions transactions,
        WayfarerWaymark waymark
    ) {
        return new DefaultWayfarerServices(
            serverId,
            configVersion,
            lifecycle,
            tasks,
            health,
            audit,
            itemIdentity,
            () -> transactions,
            () -> waymark
        );
    }

    public static WayfarerServices createDynamic(
        String serverId,
        int configVersion,
        Supplier<WayfarerLifecycleState> lifecycle,
        WayfarerTasks tasks,
        WayfarerHealth health,
        WayfarerAudit audit,
        WayfarerItemIdentity itemIdentity,
        Supplier<WayfarerTransactions> transactions,
        Supplier<WayfarerWaymark> waymark
    ) {
        return new DefaultWayfarerServices(
            serverId,
            configVersion,
            lifecycle,
            tasks,
            health,
            audit,
            itemIdentity,
            transactions,
            waymark
        );
    }

    public static WayfarerServices create(
        String serverId,
        int configVersion,
        Supplier<WayfarerLifecycleState> lifecycle,
        WayfarerTasks tasks,
        WayfarerHealth health,
        WayfarerAudit audit,
        WayfarerItemIdentity itemIdentity
    ) {
        return create(
            serverId,
            configVersion,
            lifecycle,
            tasks,
            health,
            audit,
            itemIdentity,
            null,
            null
        );
    }

    public static WayfarerServices create(
        String serverId,
        int configVersion,
        Supplier<WayfarerLifecycleState> lifecycle,
        WayfarerTasks tasks,
        WayfarerHealth health
    ) {
        return create(serverId, configVersion, lifecycle, tasks, health, null, null, null, null);
    }
}
