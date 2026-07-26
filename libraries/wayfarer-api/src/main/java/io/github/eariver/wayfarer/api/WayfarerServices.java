package io.github.eariver.wayfarer.api;

public interface WayfarerServices {
    String serverId();
    int configVersion();
    WayfarerLifecycleState lifecycleState();
    WayfarerDatabase database();
    WayfarerAudit audit();
    WayfarerTransactions transactions();
    WayfarerWaymark waymark();
    WayfarerItemIdentity itemIdentity();
    WayfarerTasks tasks();
    WayfarerHealth health();
}
