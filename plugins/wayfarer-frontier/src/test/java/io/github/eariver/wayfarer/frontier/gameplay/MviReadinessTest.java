package io.github.eariver.wayfarer.frontier.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.frontier.application.EntryCycleRegistry;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class MviReadinessTest {
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-00000000c410");

    @Test
    void lateMviRestartIsConsumedOncePerExternalEntryCycle() {
        EntryCycleRegistry cycles = new EntryCycleRegistry();
        long cycle = cycles.beginExternalEntry(PLAYER);

        assertTrue(cycles.consumeLateRestart(PLAYER, cycle));
        assertFalse(cycles.consumeLateRestart(PLAYER, cycle));
    }

    @Test
    void newerEntrySupersedesOldCycleAndClearsPendingState() {
        EntryCycleRegistry cycles = new EntryCycleRegistry();
        long oldCycle = cycles.beginExternalEntry(PLAYER);
        long newCycle = cycles.beginExternalEntry(PLAYER);

        assertFalse(cycles.consumeLateRestart(PLAYER, oldCycle));
        assertTrue(cycles.consumeLateRestart(PLAYER, newCycle));

        cycles.clear(PLAYER, newCycle);
        assertFalse(cycles.consumeLateRestart(PLAYER, newCycle));
    }
}
