package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class ReissueDeliveryPolicyTest {
    private static final UUID OWNER = UUID.fromString(
        "00000000-0000-0000-0000-000000000301"
    );
    private static final UUID TOOL = UUID.fromString(
        "00000000-0000-0000-0000-000000000302"
    );
    private static final UUID NEW_INSTANCE = UUID.fromString(
        "00000000-0000-0000-0000-000000000303"
    );

    @Test
    void deliveryClassificationCoversAllRuntimeOutcomes() {
        assertEquals(
            DeliveryOutcome.DELIVERED,
            ReissueDeliveryPolicy.classify(true, true, true, 0, true)
        );
        assertEquals(
            DeliveryOutcome.ALREADY_PRESENT,
            ReissueDeliveryPolicy.classify(true, true, true, 1, true)
        );
        assertEquals(
            DeliveryOutcome.UNAVAILABLE,
            ReissueDeliveryPolicy.classify(true, true, true, 2, true)
        );
        assertEquals(
            DeliveryOutcome.INVENTORY_FULL,
            ReissueDeliveryPolicy.classify(true, true, true, 0, false)
        );
        assertEquals(
            DeliveryOutcome.PLAYER_OFFLINE,
            ReissueDeliveryPolicy.classify(true, false, true, 0, true)
        );
        assertEquals(
            DeliveryOutcome.WORLD_NOT_ALLOWED,
            ReissueDeliveryPolicy.classify(true, true, false, 0, true)
        );
        assertEquals(
            DeliveryOutcome.UNAVAILABLE,
            ReissueDeliveryPolicy.classify(false, true, true, 0, true)
        );
    }

    @Test
    void rotatedIdentityUsesPersistedInstanceAndEpochWithoutIncrementing() {
        GrowthTool rotated = new GrowthTool(
            TOOL,
            NEW_INSTANCE,
            OWNER,
            4,
            12_345,
            GrowthTool.Branch.SILK_TOUCH,
            GrowthTool.Status.ACTIVE,
            GrowthTool.DeliveryStatus.PENDING,
            17,
            1,
            5,
            0,
            Instant.EPOCH
        );

        ReissueDeliveryPolicy.ReissueDeliveryIdentity identity =
            ReissueDeliveryPolicy.identity(rotated);

        assertEquals(NEW_INSTANCE, identity.itemInstanceId());
        assertEquals(4, identity.instanceEpoch());
        assertEquals(12_345, identity.cumulativeProgressUnits());
        assertEquals(GrowthTool.Branch.SILK_TOUCH, identity.branch());
        assertEquals(0, identity.damage());
    }

    @Test
    void onlyDeliveredClassificationCreatesOneItem() {
        assertTrue(ReissueDeliveryPolicy.createsOneItem(DeliveryOutcome.DELIVERED));
        assertFalse(ReissueDeliveryPolicy.createsOneItem(DeliveryOutcome.ALREADY_PRESENT));
        assertFalse(ReissueDeliveryPolicy.createsOneItem(DeliveryOutcome.UNAVAILABLE));
        assertFalse(ReissueDeliveryPolicy.createsOneItem(DeliveryOutcome.INVENTORY_FULL));
        assertFalse(ReissueDeliveryPolicy.createsOneItem(DeliveryOutcome.PLAYER_OFFLINE));
        assertFalse(ReissueDeliveryPolicy.createsOneItem(DeliveryOutcome.WORLD_NOT_ALLOWED));
    }

    @Test
    void duplicateOrReplayClassificationCannotCreateASecondItem() {
        DeliveryOutcome duplicate = ReissueDeliveryPolicy.classify(
            true,
            true,
            true,
            2,
            true
        );
        DeliveryOutcome current = ReissueDeliveryPolicy.classify(
            true,
            true,
            true,
            1,
            true
        );

        assertEquals(DeliveryOutcome.UNAVAILABLE, duplicate);
        assertEquals(DeliveryOutcome.ALREADY_PRESENT, current);
        assertFalse(ReissueDeliveryPolicy.createsOneItem(duplicate));
        assertFalse(ReissueDeliveryPolicy.createsOneItem(current));
    }
}
