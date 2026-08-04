package io.github.eariver.wayfarer.frontier.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class DeliveryRetryClassificationTest {
    @Test
    void acceptingAndOfflineIsPlayerOfflineNotUnavailable() {
        DeliveryRetryClassification.Kind kind =
            DeliveryRetryClassification.classify(true, false);
        assertEquals(DeliveryRetryClassification.Kind.PLAYER_OFFLINE, kind);
        TraversalDeliveryCoordinator.Result result =
            DeliveryRetryClassification.terminalResult(kind);
        assertEquals(1, result.playerOffline());
        assertFalse(result.repositoryUnavailable());
        assertTrue(DeliveryRetryClassification.playerNotificationProhibited(kind));
    }

    @Test
    void notAcceptingIsShutdownUnavailable() {
        DeliveryRetryClassification.Kind kind =
            DeliveryRetryClassification.classify(false, false);
        assertEquals(
            DeliveryRetryClassification.Kind.SHUTDOWN_UNAVAILABLE,
            kind
        );
        TraversalDeliveryCoordinator.Result result =
            DeliveryRetryClassification.terminalResult(kind);
        assertTrue(result.repositoryUnavailable());
        assertEquals(0, result.playerOffline());
        assertTrue(DeliveryRetryClassification.playerNotificationProhibited(kind));
    }

    @Test
    void acceptingAndOnlineProceedsToSafeEntry() {
        DeliveryRetryClassification.Kind kind =
            DeliveryRetryClassification.classify(true, true);
        assertEquals(
            DeliveryRetryClassification.Kind.PROCEED_SAFE_ENTRY,
            kind
        );
        assertFalse(DeliveryRetryClassification.playerNotificationProhibited(kind));
        assertThrows(
            IllegalArgumentException.class,
            () -> DeliveryRetryClassification.terminalResult(kind)
        );
    }

    @Test
    void offlineAndUnavailableAreNotConfused() {
        TraversalDeliveryCoordinator.Result offline =
            DeliveryRetryClassification.terminalResult(
                DeliveryRetryClassification.Kind.PLAYER_OFFLINE
            );
        TraversalDeliveryCoordinator.Result unavailable =
            DeliveryRetryClassification.terminalResult(
                DeliveryRetryClassification.Kind.SHUTDOWN_UNAVAILABLE
            );
        assertEquals(1, offline.playerOffline());
        assertFalse(offline.repositoryUnavailable());
        assertEquals(0, unavailable.playerOffline());
        assertTrue(unavailable.repositoryUnavailable());
    }
}
