package io.github.eariver.wayfarer.core.health;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.common.secret.SecretValue;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class HealthRegistry implements WayfarerHealth {
    public static final String CONFIG = "Config";
    public static final String EXECUTOR = "Executor";
    public static final String SERVICES = "Services";
    public static final String LIFECYCLE = "Lifecycle";
    public static final String MARIA_DB = "MariaDB";
    public static final String MIGRATION = "Migration";
    public static final String AUDIT = "Audit";
    public static final String IDENTITY = "Identity";
    public static final String REDIS = "Redis";
    private static final String[] UNAVAILABLE_DEPENDENCIES = {
        REDIS, "Waymark", AUDIT, IDENTITY, "Transaction"
    };

    private final Clock clock;
    private final Supplier<WayfarerLifecycleState> lifecycleState;
    private final Map<String, ComponentHealth> components = new LinkedHashMap<>();

    public HealthRegistry(Clock clock, Supplier<WayfarerLifecycleState> lifecycleState) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.lifecycleState = Objects.requireNonNull(lifecycleState, "lifecycleState");
        update(MARIA_DB, Status.UNKNOWN, "Not initialized");
        update(MIGRATION, Status.UNKNOWN, "Not initialized");
        for (String dependency : UNAVAILABLE_DEPENDENCIES) {
            update(dependency, Status.UNKNOWN, "Unavailable");
        }
        update(CONFIG, Status.UNKNOWN, "Configuration not loaded");
        update(EXECUTOR, Status.UNKNOWN, "Executor not initialized");
        update(SERVICES, Status.UNKNOWN, "Services not registered");
        refreshLifecycle();
    }

    public synchronized void update(String component, Status status, String detail) {
        update(component, status, detail, new SecretValue[0]);
    }

    public synchronized void update(
        String component,
        Status status,
        String detail,
        SecretValue... secrets
    ) {
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(status, "status");
        String sanitized = sanitize(detail);
        for (SecretValue secret : secrets) {
            if (secret != null) {
                sanitized = secret.redact(sanitized);
            }
        }
        components.put(
            component,
            new ComponentHealth(component, status, clock.instant(), sanitized)
        );
    }

    public synchronized void refreshLifecycle() {
        WayfarerLifecycleState state = lifecycleState.get();
        Status status = switch (state) {
            case ENABLED -> Status.UP;
            case FAILED -> Status.DOWN;
            case STOPPING, DISABLED -> Status.DISABLED;
            case NEW, INITIALIZING -> Status.DEGRADED;
        };
        update(LIFECYCLE, status, state.name());
    }

    @Override
    public synchronized HealthSnapshot snapshot() {
        refreshLifecycle();
        WayfarerLifecycleState lifecycle = lifecycleState.get();
        Status overall = aggregate(lifecycle, components);
        return new HealthSnapshot(
            overall,
            clock.instant(),
            lifecycle,
            new LinkedHashMap<>(components)
        );
    }

    static Status aggregate(
        WayfarerLifecycleState lifecycle,
        Map<String, ComponentHealth> values
    ) {
        if (lifecycle == WayfarerLifecycleState.DISABLED
            || lifecycle == WayfarerLifecycleState.STOPPING) {
            return Status.DISABLED;
        }
        boolean degraded = lifecycle != WayfarerLifecycleState.ENABLED;
        for (ComponentHealth component : values.values()) {
            if (component.status() == Status.DOWN) {
                return Status.DOWN;
            }
            if (component.status() == Status.DEGRADED
                || component.status() == Status.UNKNOWN
                || component.status() == Status.DISABLED) {
                degraded = true;
            }
        }
        return degraded ? Status.DEGRADED : Status.UP;
    }

    private static String sanitize(String detail) {
        String value = detail == null ? "" : detail.replace('\r', ' ').replace('\n', ' ');
        return value.length() <= 256 ? value : value.substring(0, 256);
    }
}
