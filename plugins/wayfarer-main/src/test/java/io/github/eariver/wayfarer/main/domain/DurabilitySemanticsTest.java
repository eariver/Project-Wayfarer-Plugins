package io.github.eariver.wayfarer.main.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class DurabilitySemanticsTest {
    @Test
    void evolutionRestoresOnceButOrdinaryProgressPreservesDamage() {
        assertEquals(0, DurabilitySemantics.afterEvolution(37, true));
        assertEquals(37, DurabilitySemantics.afterEvolution(37, false));
    }

    @Test
    void configReconcilePreservesRemainingRatioAndNeverBreaksActiveTool() {
        assertEquals(50, DurabilitySemantics.reconcileActive(100, 50, 100));
        assertEquals(780, DurabilitySemantics.reconcileActive(100, 50, 1561));
        assertEquals(1545, DurabilitySemantics.reconcileActive(100, 99, 1561));
    }
}
