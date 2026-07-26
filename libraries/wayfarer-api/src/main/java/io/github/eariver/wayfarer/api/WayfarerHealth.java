package io.github.eariver.wayfarer.api;

import java.time.Instant;
import java.util.Map;

public interface WayfarerHealth {
    HealthSnapshot snapshot();

    default Status overall() {
        return snapshot().overall();
    }

    default Map<String, Status> components() {
        return snapshot().components().entrySet().stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().status()
            ));
    }

    enum Status {
        UP, DEGRADED, DOWN, UNKNOWN, DISABLED
    }

    record ComponentHealth(
        String component,
        Status status,
        Instant timestamp,
        String detail
    ) {}

    record HealthSnapshot(
        Status overall,
        Instant timestamp,
        WayfarerLifecycleState lifecycleState,
        Map<String, ComponentHealth> components
    ) {
        public HealthSnapshot {
            components = Map.copyOf(components);
        }
    }
}
