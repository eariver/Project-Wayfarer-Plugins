package io.github.eariver.wayfarer.frontier.domain;

import java.util.Objects;

public final class LaunchpadPlacementPolicy {
    private final FrontierWorldGate worldGate;

    public LaunchpadPlacementPolicy(FrontierWorldGate worldGate) {
        this.worldGate = Objects.requireNonNull(worldGate, "worldGate");
    }

    public Result validate(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (!worldGate.allows(snapshot.exactWorldName())) {
            return Result.WORLD_UNAVAILABLE;
        }
        if (!snapshot.chunkLoaded()) {
            return Result.CHUNK_UNLOADED;
        }
        if (!snapshot.targetAir()) {
            return Result.TARGET_NOT_AIR;
        }
        if (!snapshot.solidSupport()) {
            return Result.INVALID_SUPPORT;
        }
        if (snapshot.liquid()) {
            return Result.LIQUID;
        }
        if (!snapshot.insideWorldBorder()) {
            return Result.OUTSIDE_BORDER;
        }
        if (snapshot.portalGateOrSpawn()) {
            return Result.PROTECTED_SYSTEM_AREA;
        }
        if (snapshot.regionDenied()) {
            return Result.REGION_DENIED;
        }
        if (snapshot.systemStructureOverlap()) {
            return Result.SYSTEM_STRUCTURE_OVERLAP;
        }
        if (snapshot.existingLaunchpad()) {
            return Result.LAUNCHPAD_OVERLAP;
        }
        return Result.ALLOWED;
    }

    public record Snapshot(
        String exactWorldName,
        boolean chunkLoaded,
        boolean targetAir,
        boolean solidSupport,
        boolean liquid,
        boolean insideWorldBorder,
        boolean portalGateOrSpawn,
        boolean regionDenied,
        boolean systemStructureOverlap,
        boolean existingLaunchpad
    ) {}

    public enum Result {
        ALLOWED,
        WORLD_UNAVAILABLE,
        CHUNK_UNLOADED,
        TARGET_NOT_AIR,
        INVALID_SUPPORT,
        LIQUID,
        OUTSIDE_BORDER,
        PROTECTED_SYSTEM_AREA,
        REGION_DENIED,
        SYSTEM_STRUCTURE_OVERLAP,
        LAUNCHPAD_OVERLAP
    }
}
