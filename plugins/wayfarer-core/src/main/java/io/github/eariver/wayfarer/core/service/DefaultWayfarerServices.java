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
    private final WayfarerAudit audit;
    private final WayfarerItemIdentity itemIdentity;
    private final Supplier<WayfarerTransactions> transactions;
    private final Supplier<WayfarerWaymark> waymark;

    DefaultWayfarerServices(
        String serverId,
        int configVersion,
        Supplier<WayfarerLifecycleState> lifecycleState,
        WayfarerTasks tasks,
        WayfarerHealth health,
        WayfarerAudit audit,
        WayfarerItemIdentity itemIdentity,
        Supplier<WayfarerTransactions> transactions,
        Supplier<WayfarerWaymark> waymark
    ) {
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.configVersion = configVersion;
        this.lifecycleState = Objects.requireNonNull(lifecycleState, "lifecycleState");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.health = Objects.requireNonNull(health, "health");
        this.audit = audit;
        this.itemIdentity = itemIdentity;
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.waymark = Objects.requireNonNull(waymark, "waymark");
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
        if (audit == null) {
            throw unavailable("Audit");
        }
        return audit;
    }

    @Override
    public WayfarerTransactions transactions() {
        WayfarerTransactions current = transactions.get();
        if (current == null) {
            throw unavailable("Transactions");
        }
        return current;
    }

    @Override
    public WayfarerWaymark waymark() {
        WayfarerWaymark current = waymark.get();
        if (current == null) {
            throw unavailable("Waymark");
        }
        return current;
    }

    @Override
    public WayfarerItemIdentity itemIdentity() {
        if (itemIdentity == null) {
            throw unavailable("Item identity");
        }
        return itemIdentity;
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
        return new IllegalStateException(component + " is unavailable");
    }
}
