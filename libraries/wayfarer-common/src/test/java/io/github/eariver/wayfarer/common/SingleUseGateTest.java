package io.github.eariver.wayfarer.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class SingleUseGateTest {
    @Test
    void onlyFirstConfirmationAcquiresTheGate() {
        SingleUseGate gate = new SingleUseGate();

        assertTrue(gate.tryAcquire());
        assertFalse(gate.tryAcquire());
        assertTrue(gate.acquired());
    }
}
