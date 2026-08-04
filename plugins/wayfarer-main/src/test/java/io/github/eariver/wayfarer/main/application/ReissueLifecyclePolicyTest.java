package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ReissueLifecyclePolicyTest {
    @Test
    void recoveryStartGateAllowsOnlyOneRecoveryStart() {
        ReissueLifecyclePolicy.RecoveryStartGate gate =
            new ReissueLifecyclePolicy.RecoveryStartGate();

        assertTrue(gate.tryStart());
        assertFalse(gate.tryStart());
    }

    @Test
    void commandsAreExposedOnlyAfterSuccessfulRecoveryAndRegistration() {
        assertFalse(ReissueLifecyclePolicy.canExposeCommands(
            true, true, false, true
        ));
        assertFalse(ReissueLifecyclePolicy.canExposeCommands(
            true, true, true, false
        ));
        assertFalse(ReissueLifecyclePolicy.canExposeCommands(
            false, true, true, true
        ));
        assertFalse(ReissueLifecyclePolicy.canExposeCommands(
            true, false, true, true
        ));
        assertTrue(ReissueLifecyclePolicy.canExposeCommands(
            true, true, true, true
        ));
    }

    @Test
    void recoveryFailureCannotExposeCommandSurface() {
        assertFalse(ReissueLifecyclePolicy.canExposeCommands(
            true,
            true,
            false,
            true
        ));
    }

    @Test
    void disableRejectsLateCallbacksAndEnabledRuntimeAcceptsThem() {
        assertFalse(ReissueLifecyclePolicy.acceptsCallback(false, true));
        assertFalse(ReissueLifecyclePolicy.acceptsCallback(true, false));
        assertFalse(ReissueLifecyclePolicy.acceptsCallback(false, false));
        assertTrue(ReissueLifecyclePolicy.acceptsCallback(true, true));
    }
}
