package io.github.eariver.wayfarer.frontier.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.frontier.gameplay.FrontierGameplayRuntime.ReconcileClassification;
import org.junit.jupiter.api.Test;

final class LaunchpadExpirationDecisionTest {
    @Test
    void unknownWorldClassificationDefersExpirationWithoutDestructiveTransition() {
        assertFalse(
            FrontierGameplayRuntime.mayPerformExpirationTransition(
                ReconcileClassification.UNKNOWN
            )
        );
    }

    @Test
    void loadedWorldClassificationKeepsExpirationEligible() {
        assertTrue(
            FrontierGameplayRuntime.mayPerformExpirationTransition(
                ReconcileClassification.DB_AND_BLOCK_MATCH
            )
        );
        assertTrue(
            FrontierGameplayRuntime.mayPerformExpirationTransition(
                ReconcileClassification.DB_ONLY
            )
        );
        assertTrue(
            FrontierGameplayRuntime.mayPerformExpirationTransition(
                ReconcileClassification.BLOCK_ONLY
            )
        );
        assertTrue(
            FrontierGameplayRuntime.mayPerformExpirationTransition(
                ReconcileClassification.CONFLICT
            )
        );
        assertTrue(
            FrontierGameplayRuntime.mayPerformExpirationTransition(
                ReconcileClassification.NOT_FOUND
            )
        );
    }
}
