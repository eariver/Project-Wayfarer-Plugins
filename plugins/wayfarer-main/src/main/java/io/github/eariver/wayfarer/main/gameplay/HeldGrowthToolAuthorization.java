≠rá^—f•ñÿ¶{Oly 'v√Æ∂õ≠package io.github.eariver.wayfarer.main.gameplay;

import java.util.Objects;

/**
 * Main-thread decision cached for the currently held item.
 *
 * <p>The cache deliberately exposes capabilities instead of making callers
 * re-parse PDC or query persistence from an event handler.</p>
 */
public final class HeldGrowthToolAuthorization {
    private final State state;

    public HeldGrowthToolAuthorization(State state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    public State state() {
        return state;
    }

    public boolean managedItemPresent() {
        return state != State.NO_MANAGED_ITEM;
    }

    public boolean allowsBlockBreak() {
        return state == State.NO_MANAGED_ITEM || state == State.VALID_ACTIVE_OWNER;
    }

    public boolean allowsProgress() {
        return state == State.VALID_ACTIVE_OWNER;
    }

    public boolean allowsGui() {
        return state == State.VALID_ACTIVE_OWNER
            || state == State.VALID_BROKEN_OWNER;
    }

    public boolean allowsRepair() {
        return state == State.VALID_ACTIVE_OWNER
            || state == State.VALID_BROKEN_OWNER;
    }

    public boolean allowsBranchMutation() {
        return state == State.VALID_ACTIVE_OWNER
            || state == State.VALID_BROKEN_OWNER;
    }

    public enum State {
        NO_MANAGED_ITEM,
        AUTHORITY_UNAVAILABLE,
        MALFORMED,
        VALID_ACTIVE_OWNER,
        VALID_BROKEN_OWNER,
        WRONG_OWNER,
        STALE_INSTANCE,
        STALE_EPOCH,
        UNKNOWN_SCHEMA,
        REVOKED,
        WRONG_ITEM_STATE
    }
}
