package io.github.eariver.wayfarer.main.gameplay;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.identity.GrowthToolPhysicalClaim;
import java.util.Objects;
import java.util.UUID;

/** Pure mapping from one full identity comparison into a cached capability state. */
public final class HeldGrowthToolAuthorizer {
    private HeldGrowthToolAuthorizer() {
    }

    public static HeldGrowthToolAuthorization authorize(
        boolean managedItemPresent,
        GrowthToolPhysicalClaim.ParseResult parseResult,
        UUID actorUuid,
        GrowthTool authority
    ) {
        if (!managedItemPresent) {
            return new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM
            );
        }
        if (authority == null) {
            return state(HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE);
        }
        if (parseResult == null || parseResult.claim().isEmpty()) {
            return state(parseFailure(parseResult == null
                ? GrowthToolPhysicalClaim.Failure.MALFORMED
                : parseResult.failure()));
        }
        if (actorUuid == null) {
            return state(HeldGrowthToolAuthorization.State.WRONG_OWNER);
        }
        GrowthToolPhysicalClaim.Validation validation =
            parseResult.claim().orElseThrow().validate(actorUuid, authority);
        return switch (validation) {
            case VALID -> authority.status() == GrowthTool.Status.BROKEN
                ? state(HeldGrowthToolAuthorization.State.VALID_BROKEN_OWNER)
                : state(HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER);
            case WRONG_OWNER -> state(HeldGrowthToolAuthorization.State.WRONG_OWNER);
            case STALE_INSTANCE -> state(HeldGrowthToolAuthorization.State.STALE_INSTANCE);
            case STALE_EPOCH -> state(HeldGrowthToolAuthorization.State.STALE_EPOCH);
            case UNKNOWN_SCHEMA -> state(HeldGrowthToolAuthorization.State.UNKNOWN_SCHEMA);
            case REVOKED -> state(HeldGrowthToolAuthorization.State.REVOKED);
            case WRONG_ITEM_STATE -> state(HeldGrowthToolAuthorization.State.WRONG_ITEM_STATE);
        };
    }

    private static HeldGrowthToolAuthorization.State parseFailure(
        GrowthToolPhysicalClaim.Failure failure
    ) {
        if (failure == null) {
            return HeldGrowthToolAuthorization.State.MALFORMED;
        }
        return failure == GrowthToolPhysicalClaim.Failure.UNKNOWN_SCHEMA
            ? HeldGrowthToolAuthorization.State.UNKNOWN_SCHEMA
            : HeldGrowthToolAuthorization.State.MALFORMED;
    }

    private static HeldGrowthToolAuthorization state(
        HeldGrowthToolAuthorization.State state
    ) {
        return new HeldGrowthToolAuthorization(Objects.requireNonNull(state, "state"));
    }
}
