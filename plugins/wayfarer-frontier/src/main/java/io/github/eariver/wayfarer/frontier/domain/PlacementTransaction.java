package io.github.eariver.wayfarer.frontier.domain;

import java.util.Objects;
import java.util.UUID;

public record PlacementTransaction(
    UUID transactionId,
    UUID itemInstanceId,
    UUID playerUuid,
    Launchpad.Location location,
    State state,
    UUID launchpadId,
    String failureCode
) {
    public PlacementTransaction {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(itemInstanceId, "itemInstanceId");
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(state, "state");
    }

    public PlacementTransaction physicalBlockPlaced() {
        require(State.PREPARED);
        return transition(State.BLOCK_PLACED, null, null);
    }

    public PlacementTransaction recordCommitted(UUID createdLaunchpadId) {
        require(State.BLOCK_PLACED);
        return transition(State.RECORDED, Objects.requireNonNull(createdLaunchpadId), null);
    }

    public PlacementTransaction itemConsumed() {
        require(State.RECORDED);
        return transition(State.COMMITTED, launchpadId, null);
    }

    public PlacementTransaction compensated(String code) {
        if (state == State.COMMITTED) {
            throw new IllegalStateException("Committed placement cannot be compensated");
        }
        return transition(State.COMPENSATED, launchpadId, sanitizeCode(code));
    }

    public PlacementTransaction unknown(String code) {
        if (state == State.COMMITTED) {
            return this;
        }
        return transition(State.UNKNOWN, launchpadId, sanitizeCode(code));
    }

    private void require(State required) {
        if (state != required) {
            throw new IllegalStateException("Invalid placement transition");
        }
    }

    private PlacementTransaction transition(State next, UUID domainId, String code) {
        return new PlacementTransaction(
            transactionId, itemInstanceId, playerUuid, location, next, domainId, code
        );
    }

    private static String sanitizeCode(String code) {
        if (code == null || !code.matches("[A-Z0-9_]{1,64}")) {
            return "PLACEMENT_FAILURE";
        }
        return code;
    }

    public enum State {
        PREPARED,
        BLOCK_PLACED,
        RECORDED,
        COMMITTED,
        COMPENSATED,
        UNKNOWN
    }
}
