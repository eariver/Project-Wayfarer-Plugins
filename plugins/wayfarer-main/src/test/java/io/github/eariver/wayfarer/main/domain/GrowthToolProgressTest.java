package io.github.eariver.wayfarer.main.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class GrowthToolProgressTest {
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");

    @Test
    void addsProgressExactlyBelowLongMax() {
        assertEquals(
            Long.MAX_VALUE - 50,
            tool(Long.MAX_VALUE - 100)
                .addProgress(50, NOW)
                .cumulativeProgressUnits()
        );
    }

    @Test
    void saturatesProgressAtLongMaxWhenSumWouldOverflow() {
        assertEquals(
            Long.MAX_VALUE,
            tool(Long.MAX_VALUE - 100)
                .addProgress(200, NOW)
                .cumulativeProgressUnits()
        );
    }

    @Test
    void keepsProgressAtLongMaxWhenAlreadySaturated() {
        assertEquals(
            Long.MAX_VALUE,
            tool(Long.MAX_VALUE)
                .addProgress(1, NOW)
                .cumulativeProgressUnits()
        );
    }

    @Test
    void rejectsNonPositiveUnitsAndNegativeInputsFailClosed() {
        assertThrows(
            IllegalStateException.class,
            () -> tool(0).addProgress(0, NOW)
        );
        assertThrows(
            IllegalStateException.class,
            () -> tool(0).addProgress(-1, NOW)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> GrowthTool.saturatingAddPositive(-1, 1)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> GrowthTool.saturatingAddPositive(1, -1)
        );
    }

    private static GrowthTool tool(long progress) {
        return new GrowthTool(
            UUID.fromString("00000000-0000-0000-0000-000000000111"),
            UUID.fromString("00000000-0000-0000-0000-000000000112"),
            1,
            progress,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.ACTIVE,
            GrowthTool.DeliveryStatus.DELIVERED,
            0,
            1,
            1,
            0,
            NOW
        );
    }
}
