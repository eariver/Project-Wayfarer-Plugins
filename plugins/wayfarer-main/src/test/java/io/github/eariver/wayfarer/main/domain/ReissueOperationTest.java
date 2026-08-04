package io.github.eariver.wayfarer.main.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class ReissueOperationTest {
    private static final UUID ID = UUID.randomUUID();
    private static final UUID PLAYER = UUID.randomUUID();
    private static final UUID TOOL = UUID.randomUUID();
    private static final UUID OLD_INSTANCE = UUID.randomUUID();
    private static final UUID NEW_INSTANCE = UUID.randomUUID();
    private static final UUID TRANSACTION = UUID.randomUUID();
    private static final Instant COMMITTED_AT = Instant.parse("2026-08-01T00:00:00Z");

    @Test
    void stateSpecificPaymentEvidenceIsEnforced() {
        assertDoesNotThrow(() -> operation(
            ReissueOperation.State.PREPARED,
            null,
            null
        ));
        assertDoesNotThrow(() -> operation(
            ReissueOperation.State.UNKNOWN,
            TRANSACTION,
            null
        ));
        assertDoesNotThrow(() -> operation(
            ReissueOperation.State.PAYMENT_COMMITTED,
            TRANSACTION,
            COMMITTED_AT
        ));

        assertThrows(IllegalArgumentException.class, () -> operation(
            ReissueOperation.State.PREPARED,
            TRANSACTION,
            null
        ));
        assertThrows(IllegalArgumentException.class, () -> operation(
            ReissueOperation.State.PAYMENT_COMMITTED,
            TRANSACTION,
            null
        ));
        assertThrows(IllegalArgumentException.class, () -> operation(
            ReissueOperation.State.UNKNOWN,
            null,
            COMMITTED_AT
        ));
    }

    @Test
    void growthToolRotationUsesThePersistedUuidAndPreservesLogicalHistory() {
        Instant now = Instant.parse("2026-08-01T00:00:00Z");
        GrowthTool old = new GrowthTool(
            TOOL,
            OLD_INSTANCE,
            PLAYER,
            4,
            9_000,
            GrowthTool.Branch.SILK_TOUCH,
            GrowthTool.Status.BROKEN,
            GrowthTool.DeliveryStatus.DELIVERED,
            17,
            1,
            8,
            2,
            now
        );
        GrowthTool rotated = old.reissued(NEW_INSTANCE, now.plusSeconds(1));

        org.junit.jupiter.api.Assertions.assertEquals(NEW_INSTANCE, rotated.itemInstanceId());
        org.junit.jupiter.api.Assertions.assertEquals(5, rotated.instanceEpoch());
        org.junit.jupiter.api.Assertions.assertEquals(GrowthTool.Status.ACTIVE, rotated.status());
        org.junit.jupiter.api.Assertions.assertEquals(0, rotated.storedDamage());
        org.junit.jupiter.api.Assertions.assertEquals(
            GrowthTool.DeliveryStatus.PENDING,
            rotated.deliveryStatus()
        );
        org.junit.jupiter.api.Assertions.assertEquals(old.toolId(), rotated.toolId());
        org.junit.jupiter.api.Assertions.assertEquals(old.ownerUuid(), rotated.ownerUuid());
        org.junit.jupiter.api.Assertions.assertEquals(
            old.cumulativeProgressUnits(),
            rotated.cumulativeProgressUnits()
        );
        org.junit.jupiter.api.Assertions.assertEquals(old.branch(), rotated.branch());
        org.junit.jupiter.api.Assertions.assertEquals(old.schemaVersion(), rotated.schemaVersion());
        org.junit.jupiter.api.Assertions.assertEquals(
            NEW_INSTANCE,
            old.reissued(NEW_INSTANCE, now.plusSeconds(2)).itemInstanceId()
        );
    }

    private static ReissueOperation operation(
        ReissueOperation.State state,
        UUID transaction,
        Instant paymentCommittedAt
    ) {
        return new ReissueOperation(
            ID,
            "main-reissue:quote-1",
            PLAYER,
            TOOL,
            OLD_INSTANCE,
            NEW_INSTANCE,
            1,
            3,
            "main-test",
            363,
            state,
            transaction,
            paymentCommittedAt,
            null,
            0
        );
    }
}
