package io.github.eariver.wayfarer.core.persistence;

import java.util.Objects;

public record PersistenceDrainResult(
    PersistenceDrainStatus status,
    int remainingInFlight
) {
    public PersistenceDrainResult {
        Objects.requireNonNull(status, "status");
        if (remainingInFlight < 0) {
            throw new IllegalArgumentException("remainingInFlight must not be negative");
        }
        if (status == PersistenceDrainStatus.DRAINED && remainingInFlight != 0) {
            throw new IllegalArgumentException("A drained result cannot retain in-flight work");
        }
    }
}
