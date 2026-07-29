package io.github.eariver.wayfarer.main.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record GrowthTool(
    UUID toolId,
    UUID ownerUuid,
    long instanceEpoch,
    long cumulativeProgressUnits,
    Branch branch,
    Status status,
    DeliveryStatus deliveryStatus,
    int storedDamage,
    int schemaVersion,
    long displayRevision,
    long lockVersion,
    Instant updatedAt
) {
    public static final String TOOL_TYPE = "PICKAXE";

    public GrowthTool {
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(ownerUuid, "ownerUuid");
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(deliveryStatus, "deliveryStatus");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (instanceEpoch < 1 || cumulativeProgressUnits < 0 || storedDamage < 0
            || schemaVersion < 1 || displayRevision < 1 || lockVersion < 0) {
            throw new IllegalArgumentException("Growth tool numeric state is invalid");
        }
    }

    public GrowthTool addProgress(long units, Instant now) {
        if (status != Status.ACTIVE || units <= 0) {
            throw new IllegalStateException("Progress requires an active tool and positive units");
        }
        return new GrowthTool(
            toolId,
            ownerUuid,
            instanceEpoch,
            Math.addExact(cumulativeProgressUnits, units),
            branch,
            status,
            deliveryStatus,
            storedDamage,
            schemaVersion,
            Math.addExact(displayRevision, 1),
            lockVersion,
            now
        );
    }

    public GrowthTool broken(int terminalDamage, Instant now) {
        if (status != Status.ACTIVE || terminalDamage < 0) {
            throw new IllegalStateException("Only an active tool can become broken");
        }
        return new GrowthTool(
            toolId,
            ownerUuid,
            instanceEpoch,
            cumulativeProgressUnits,
            branch,
            Status.BROKEN,
            deliveryStatus,
            terminalDamage,
            schemaVersion,
            Math.addExact(displayRevision, 1),
            lockVersion,
            now
        );
    }

    public GrowthTool repaired(Instant now) {
        if (status == Status.REVOKED) {
            throw new IllegalStateException("A revoked tool cannot be repaired");
        }
        return new GrowthTool(
            toolId,
            ownerUuid,
            instanceEpoch,
            cumulativeProgressUnits,
            branch,
            Status.ACTIVE,
            deliveryStatus,
            0,
            schemaVersion,
            Math.addExact(displayRevision, 1),
            lockVersion,
            now
        );
    }

    public enum Branch {
        FORTUNE,
        SILK_TOUCH
    }

    public enum Status {
        ACTIVE,
        BROKEN,
        REVOKED
    }

    public enum DeliveryStatus {
        DELIVERED,
        PENDING
    }
}
