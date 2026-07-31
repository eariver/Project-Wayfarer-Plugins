package io.github.eariver.wayfarer.main.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record GrowthTool(
    UUID toolId,
    UUID itemInstanceId,
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
        Objects.requireNonNull(itemInstanceId, "itemInstanceId");
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

    public GrowthTool(
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
        this(
            toolId,
            physicalId(toolId, instanceEpoch),
            ownerUuid,
            instanceEpoch,
            cumulativeProgressUnits,
            branch,
            status,
            deliveryStatus,
            storedDamage,
            schemaVersion,
            displayRevision,
            lockVersion,
            updatedAt
        );
    }

    public GrowthTool addProgress(long units, Instant now) {
        if (status != Status.ACTIVE || units <= 0) {
            throw new IllegalStateException("Progress requires an active tool and positive units");
        }
        return new GrowthTool(
            toolId,
            itemInstanceId,
            ownerUuid,
            instanceEpoch,
            saturatingAddPositive(cumulativeProgressUnits, units),
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

    static long saturatingAddPositive(long current, long earned) {
        if (current < 0 || earned < 0) {
            throw new IllegalArgumentException("Progress values must be non-negative");
        }
        return current > Long.MAX_VALUE - earned
            ? Long.MAX_VALUE
            : current + earned;
    }

    public GrowthTool broken(int terminalDamage, Instant now) {
        if (status != Status.ACTIVE || terminalDamage < 0) {
            throw new IllegalStateException("Only an active tool can become broken");
        }
        return new GrowthTool(
            toolId,
            itemInstanceId,
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
            itemInstanceId,
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

    public GrowthTool withBranch(Branch nextBranch, Instant now) {
        Objects.requireNonNull(nextBranch, "nextBranch");
        if (status == Status.REVOKED) {
            throw new IllegalStateException("A revoked tool cannot change branch");
        }
        return new GrowthTool(
            toolId,
            itemInstanceId,
            ownerUuid,
            instanceEpoch,
            cumulativeProgressUnits,
            nextBranch,
            status,
            deliveryStatus,
            storedDamage,
            schemaVersion,
            Math.addExact(displayRevision, 1),
            lockVersion,
            now
        );
    }

    public GrowthTool revoked(Instant now) {
        Objects.requireNonNull(now, "now");
        if (status == Status.REVOKED) {
            return this;
        }
        return new GrowthTool(
            toolId,
            itemInstanceId,
            ownerUuid,
            instanceEpoch,
            cumulativeProgressUnits,
            branch,
            Status.REVOKED,
            deliveryStatus,
            storedDamage,
            schemaVersion,
            Math.addExact(displayRevision, 1),
            lockVersion,
            now
        );
    }

    public GrowthTool reissued(Instant now) {
        Objects.requireNonNull(now, "now");
        return new GrowthTool(
            toolId,
            UUID.randomUUID(),
            ownerUuid,
            Math.addExact(instanceEpoch, 1),
            cumulativeProgressUnits,
            branch,
            Status.ACTIVE,
            DeliveryStatus.PENDING,
            0,
            schemaVersion,
            Math.addExact(displayRevision, 1),
            lockVersion,
            now
        );
    }

    private static UUID physicalId(UUID toolId, long instanceEpoch) {
        return UUID.nameUUIDFromBytes(
            (toolId + ":" + instanceEpoch)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)
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
