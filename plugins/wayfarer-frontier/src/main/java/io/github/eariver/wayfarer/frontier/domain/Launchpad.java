package io.github.eariver.wayfarer.frontier.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Launchpad(
    UUID launchpadId,
    Location location,
    float yaw,
    UUID placerUuid,
    int successfulUseCount,
    int maxUsesAtCreation,
    Instant createdAt,
    Instant lastUsedAt,
    Instant expiresAt,
    String definitionId,
    State state,
    int schemaVersion,
    long lockVersion
) {
    public Launchpad {
        Objects.requireNonNull(launchpadId, "launchpadId");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(placerUuid, "placerUuid");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(state, "state");
        if (successfulUseCount < 0 || maxUsesAtCreation <= 0
            || successfulUseCount > maxUsesAtCreation || definitionId.isBlank()
            || schemaVersion < 1 || lockVersion < 0 || !Float.isFinite(yaw)) {
            throw new IllegalArgumentException("Launchpad state is invalid");
        }
    }

    public UseResult use(
        Instant now,
        Instant playerCooldownUntil,
        boolean sneaking,
        boolean safeLaunch,
        Duration expiryAfterUse
    ) {
        return use(
            now,
            playerCooldownUntil,
            sneaking,
            safeLaunch,
            expiryAfterUse,
            true
        );
    }

    public UseResult use(
        Instant now,
        Instant playerCooldownUntil,
        boolean sneaking,
        boolean safeLaunch,
        Duration expiryAfterUse,
        boolean extendExpiration
    ) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(expiryAfterUse, "expiryAfterUse");
        if (state != State.ACTIVE || !now.isBefore(expiresAt)) {
            return new UseResult(this, Outcome.UNAVAILABLE);
        }
        if (sneaking) {
            return new UseResult(this, Outcome.SNEAKING);
        }
        if (playerCooldownUntil != null && now.isBefore(playerCooldownUntil)) {
            return new UseResult(this, Outcome.COOLDOWN);
        }
        if (!safeLaunch) {
            return new UseResult(this, Outcome.UNSAFE);
        }

        int uses = Math.addExact(successfulUseCount, 1);
        State nextState = uses >= maxUsesAtCreation ? State.EXHAUSTED : State.ACTIVE;
        Launchpad updated = new Launchpad(
            launchpadId,
            location,
            yaw,
            placerUuid,
            uses,
            maxUsesAtCreation,
            createdAt,
            now,
            extendExpiration ? now.plus(expiryAfterUse) : expiresAt,
            definitionId,
            nextState,
            schemaVersion,
            Math.addExact(lockVersion, 1)
        );
        return new UseResult(updated, Outcome.LAUNCHED);
    }

    public Launchpad expire(Instant now) {
        if (state != State.ACTIVE || now.isBefore(expiresAt)) {
            return this;
        }
        return withState(State.EXPIRED);
    }

    public Launchpad remove(State removalState) {
        if (removalState != State.PLAYER_BROKEN && removalState != State.ADMIN_REMOVED
            && removalState != State.RECONCILED_REMOVED) {
            throw new IllegalArgumentException("Invalid removal state");
        }
        return state == State.ACTIVE ? withState(removalState) : this;
    }

    private Launchpad withState(State nextState) {
        return new Launchpad(
            launchpadId,
            location,
            yaw,
            placerUuid,
            successfulUseCount,
            maxUsesAtCreation,
            createdAt,
            lastUsedAt,
            expiresAt,
            definitionId,
            nextState,
            schemaVersion,
            Math.addExact(lockVersion, 1)
        );
    }

    public record Location(String worldId, int x, int y, int z) {
        public Location {
            if (worldId == null || worldId.isBlank()) {
                throw new IllegalArgumentException("World id is required");
            }
        }
    }

    public record UseResult(Launchpad launchpad, Outcome outcome) {}

    public enum Outcome {
        LAUNCHED,
        UNAVAILABLE,
        SNEAKING,
        COOLDOWN,
        UNSAFE
    }

    public enum State {
        ACTIVE,
        EXHAUSTED,
        EXPIRED,
        PLAYER_BROKEN,
        ADMIN_REMOVED,
        RECONCILED_REMOVED,
        UNKNOWN
    }
}
