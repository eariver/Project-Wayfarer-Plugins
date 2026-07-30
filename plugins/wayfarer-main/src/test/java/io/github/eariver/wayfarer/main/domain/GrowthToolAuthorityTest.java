package io.github.eariver.wayfarer.main.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class GrowthToolAuthorityTest {
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");

    @Test
    void revokeIsIdempotentAndReissueInvalidatesTheOldEpoch() {
        GrowthTool active = tool();

        GrowthTool revoked = active.revoked(NOW.plusSeconds(1));
        assertEquals(GrowthTool.Status.REVOKED, revoked.status());
        assertEquals(active.instanceEpoch(), revoked.instanceEpoch());
        assertSame(revoked, revoked.revoked(NOW.plusSeconds(2)));

        GrowthTool reissued = revoked.reissued(NOW.plusSeconds(3));
        assertEquals(GrowthTool.Status.ACTIVE, reissued.status());
        assertEquals(GrowthTool.DeliveryStatus.PENDING, reissued.deliveryStatus());
        assertEquals(active.instanceEpoch() + 1, reissued.instanceEpoch());
        assertEquals(active.cumulativeProgressUnits(), reissued.cumulativeProgressUnits());
        assertEquals(active.branch(), reissued.branch());
    }

    private static GrowthTool tool() {
        return new GrowthTool(
            UUID.fromString("00000000-0000-0000-0000-000000000101"),
            UUID.fromString("00000000-0000-0000-0000-000000000102"),
            4,
            2_240_000,
            GrowthTool.Branch.SILK_TOUCH,
            GrowthTool.Status.ACTIVE,
            GrowthTool.DeliveryStatus.DELIVERED,
            0,
            1,
            7,
            3,
            NOW
        );
    }
}
