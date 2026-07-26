package io.github.eariver.wayfarer.core.service;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerDatabase;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymark;

import java.util.Objects;
import java.util.function.Supplier;

final class DefaultWayfarerServices implements WayfarerServices {
    private final String serverId;
    private final int configVersion;
    private final Supplier<WayfarerLifecycleState> lifecycleState;
    private final WayfarerTasks tasks;
    private final WayfarerHealth health;

    DefaultWayfarerServices(
        String serverId,
        int configVersion,
        Supplier<WayfarerLifecycleState> lifecycleState,
        WayfarerTasks tasks,
        WayfarerHealth health
    ) {
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.configVersion = configVersion;
        this.lifecycleState = Objects.requireNonNull(lifecycleState, "lifecycleState");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.health = Objects.requireNonNull(health, "health");
    }

    @Override
    public String serverId() {
        return serverId;
    }

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Override
    public WayfarerLifecycleState lifecycleState() {
        return lifecycleState.get();
    }

    @Override
    public WayfarerDatabase database() {
        throw unavailable("MariaDB");
    }

    @Override
    public WayfarerAudit audit() {
        throw unavailable("Audit");
    }

    @Override
    public WayfarerTransactions transactions() {
        throw unavailable("Transactions");
    }

    @Override
    public WayfarerWaymark waymark() {
        throw unavailable("Waymark");
    }

    @Override
    public WayfarerItemIdentity itemIdentity() {
        throw unavailable("Item identity");
    }

    @Override
    public WayfarerTasks tasks() {
        return tasks;
    }

    @Override
    public WayfarerHealth health() {
        return health;
    }

    private static IllegalStateException unavailable(String component) {
        return new IllegalStateException(component + " is not implemented in V0.0.1-alpha.1");
    }
}
