package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class HeldGrowthToolAuthorizationTest {
    @Test
    void noManagedItemLeavesOrdinaryBreakAloneButHasNoManagedCapabilities() {
        HeldGrowthToolAuthorization authorization =
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM
            );

        assertTrue(authorization.allowsBlockBreak());
        assertFalse(authorization.allowsProgress());
        assertFalse(authorization.allowsGui());
        assertFalse(authorization.allowsRepair());
        assertFalse(authorization.allowsBranchMutation());
    }

    @Test
    void activeOwnerHasAllExistingGrowthCapabilities() {
        HeldGrowthToolAuthorization authorization =
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            );

        assertTrue(authorization.managedItemPresent());
        assertTrue(authorization.allowsBlockBreak());
        assertTrue(authorization.allowsProgress());
        assertTrue(authorization.allowsGui());
        assertTrue(authorization.allowsRepair());
        assertTrue(authorization.allowsBranchMutation());
    }

    @Test
    void brokenOwnerCanOpenRepairButCannotBreakOrProgress() {
        HeldGrowthToolAuthorization authorization =
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_BROKEN_OWNER
            );

        assertTrue(authorization.managedItemPresent());
        assertFalse(authorization.allowsBlockBreak());
        assertFalse(authorization.allowsProgress());
        assertTrue(authorization.allowsGui());
        assertTrue(authorization.allowsRepair());
        assertFalse(authorization.allowsBranchMutation());
    }

    @Test
    void everyInvalidManagedStateIsFailClosed() {
        for (HeldGrowthToolAuthorization.State state :
            HeldGrowthToolAuthorization.State.values()) {
            if (state == HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM
                || state == HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
                || state == HeldGrowthToolAuthorization.State.VALID_BROKEN_OWNER) {
                continue;
            }
            HeldGrowthToolAuthorization authorization =
                new HeldGrowthToolAuthorization(state);
            assertTrue(authorization.managedItemPresent(), state.name());
            assertFalse(authorization.allowsBlockBreak(), state.name());
            assertFalse(authorization.allowsProgress(), state.name());
            assertFalse(authorization.allowsGui(), state.name());
            assertFalse(authorization.allowsRepair(), state.name());
            assertFalse(authorization.allowsBranchMutation(), state.name());
        }
    }
}
