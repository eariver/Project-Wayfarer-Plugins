package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ReissueEligibilityPolicyTest {
    @Test
    void exactResourceWorldsAreAllowedAndVariantsAreRejected() {
        assertTrue(ReissueEligibilityPolicy.isAllowedWorld("resource"));
        assertTrue(ReissueEligibilityPolicy.isAllowedWorld("resource_nether"));
        assertTrue(ReissueEligibilityPolicy.isAllowedWorld("resource_end"));
        assertFalse(ReissueEligibilityPolicy.isAllowedWorld("resource_backup"));
        assertFalse(ReissueEligibilityPolicy.isAllowedWorld("RESOURCE"));
        assertFalse(ReissueEligibilityPolicy.isAllowedWorld("unknown"));
    }

    @Test
    void currentItemIsCollectedAcrossStorageArmorOffhandAndCursor() {
        PhysicalItemPresence presence = ReissueEligibilityPolicy.scan(
            new String[] {"old-instance", "current-instance"},
            new String[] {"current-armor"},
            "current-offhand",
            "current-cursor",
            value -> value.startsWith("current-")
        );

        assertTrue(presence.storage());
        assertTrue(presence.armor());
        assertTrue(presence.offhand());
        assertTrue(presence.cursor());
        assertTrue(presence.anyPresent());
    }

    @Test
    void oldInstanceOrEpochClaimsAreNotCurrentClaims() {
        PhysicalItemPresence presence = ReissueEligibilityPolicy.scan(
            new String[] {
                "item_instance_id=old;epoch=4",
                "item_instance_id=current;epoch=3"
            },
            new String[] {"item_instance_id=old;epoch=4"},
            "item_instance_id=old;epoch=3",
            "item_instance_id=old;epoch=4",
            value -> "item_instance_id=current;epoch=3".equals(value)
        );

        assertTrue(presence.storage());
        assertFalse(presence.armor());
        assertFalse(presence.offhand());
        assertFalse(presence.cursor());
    }

    @Test
    void missingAuthorityFailsClosedAsIfEveryRegionCouldContainAClaim() {
        PhysicalItemPresence failClosed =
            ReissueEligibilityPolicy.failClosedPresence();

        assertTrue(failClosed.storage());
        assertTrue(failClosed.armor());
        assertTrue(failClosed.offhand());
        assertTrue(failClosed.cursor());
        assertTrue(failClosed.anyPresent());
    }
}
