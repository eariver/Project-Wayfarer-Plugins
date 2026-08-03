package io.github.eariver.wayfarer.frontier.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class SafeEntryReadinessTest {
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-00000000c201");

    @Test
    void publicMviEventRequiresPostEventContinuation() {
        SafeEntryReadiness readiness = new SafeEntryReadiness();
        readiness.markPublicShareEvent(PLAYER);
        SafeEntryReadiness.Request request = readiness.request(PLAYER);

        assertTrue(readiness.hasPublicShareEvent(request));
        assertEquals(
            SafeEntryReadiness.Decision.READY,
            readiness.continueAfterPublicShareEvent(request, true)
        );
        assertFalse(readiness.hasPublicShareEvent(request));
    }

    @Test
    void fallbackRequiresTwoStableFingerprintsAndIsBounded() {
        SafeEntryReadiness readiness = new SafeEntryReadiness();
        SafeEntryReadiness.Request request = readiness.request(PLAYER);

        assertEquals(
            SafeEntryReadiness.Decision.WAIT,
            readiness.observeFingerprint(request, true, 1)
        );
        assertEquals(
            SafeEntryReadiness.Decision.READY,
            readiness.observeFingerprint(request, true, 1)
        );

        SafeEntryReadiness.Request next = readiness.request(PLAYER);
        for (int observation = 1; observation < 40; observation++) {
            assertEquals(
                SafeEntryReadiness.Decision.WAIT,
                readiness.observeFingerprint(next, true, observation)
            );
        }
        assertEquals(
            SafeEntryReadiness.Decision.TIMEOUT,
            readiness.observeFingerprint(next, true, 40)
        );
    }

    @Test
    void existingProfileWaitsForRequiredManagedItemsBeforeReady() {
        SafeEntryReadiness readiness = new SafeEntryReadiness();
        SafeEntryReadiness.Request request = readiness.request(PLAYER);

        assertEquals(
            SafeEntryReadiness.Decision.WAIT,
            readiness.observeFingerprint(request, true, 1, 0, 3)
        );
        assertEquals(
            SafeEntryReadiness.Decision.WAIT,
            readiness.observeFingerprint(request, true, 1, 0, 3)
        );
        assertEquals(
            SafeEntryReadiness.Decision.WAIT,
            readiness.observeFingerprint(request, true, 2, 2, 3)
        );
        assertEquals(
            SafeEntryReadiness.Decision.WAIT,
            readiness.observeFingerprint(request, true, 3, 3, 3)
        );
        assertEquals(
            SafeEntryReadiness.Decision.READY,
            readiness.observeFingerprint(request, true, 3, 3, 3)
        );
    }

    @Test
    void missingRequiredManagedItemsFailClosedAtBoundedTimeout() {
        SafeEntryReadiness readiness = new SafeEntryReadiness();
        SafeEntryReadiness.Request request = readiness.request(PLAYER);

        for (int observation = 1; observation < 40; observation++) {
            assertEquals(
                SafeEntryReadiness.Decision.WAIT,
                readiness.observeFingerprint(request, true, observation, 0, 3)
            );
        }
        assertEquals(
            SafeEntryReadiness.Decision.TIMEOUT,
            readiness.observeFingerprint(request, true, 40, 0, 3)
        );
    }

    @Test
    void timeoutRetainsSanitizedTerminalObservationAndDoesNotAdvance() {
        SafeEntryReadiness readiness = new SafeEntryReadiness();
        SafeEntryReadiness.Request request = readiness.request(PLAYER);

        for (int poll = 1; poll <= 40; poll++) {
            assertEquals(
                poll == 40
                    ? SafeEntryReadiness.Decision.TIMEOUT
                    : SafeEntryReadiness.Decision.WAIT,
                readiness.observeFingerprint(request, true, 17, 1, 3)
            );
        }

        SafeEntryReadiness.TimeoutObservation observation = readiness
            .timeoutObservation(request, "BOUNDED_FINGERPRINT")
            .orElseThrow();
        assertEquals("BOUNDED_FINGERPRINT", observation.source());
        assertEquals(request.generation(), observation.generation());
        assertEquals(40, observation.pollCount());
        assertEquals(1, observation.visibleManagedItems());
        assertEquals(3, observation.requiredManagedItems());
        assertEquals(17, observation.fingerprint());
        assertEquals(SafeEntryReadiness.Decision.TIMEOUT, observation.decision());
        assertEquals(
            SafeEntryReadiness.Decision.TIMEOUT,
            readiness.observeFingerprint(request, true, 99, 0, 3)
        );
        assertEquals(
            observation,
            readiness.timeoutObservation(request, "BOUNDED_FINGERPRINT")
                .orElseThrow()
        );
    }

    @Test
    void zeroRequiredItemsStillNeedsTwoStableObservationsWithoutFixedDelay() {
        SafeEntryReadiness readiness = new SafeEntryReadiness();
        SafeEntryReadiness.Request request = readiness.request(PLAYER);

        assertEquals(
            SafeEntryReadiness.Decision.WAIT,
            readiness.observeFingerprint(request, true, 11, 0, 0)
        );
        assertEquals(
            SafeEntryReadiness.Decision.READY,
            readiness.observeFingerprint(request, true, 11, 0, 0)
        );
    }

    @Test
    void newerRequestSupersedesOlderAndCancelRemovesIt() {
        SafeEntryReadiness readiness = new SafeEntryReadiness();
        SafeEntryReadiness.Request first = readiness.request(PLAYER);
        SafeEntryReadiness.Request second = readiness.request(PLAYER);

        assertEquals(
            SafeEntryReadiness.Decision.SUPERSEDED,
            readiness.observeFingerprint(first, true, 7)
        );
        assertTrue(readiness.isCurrent(second));

        readiness.cancel(PLAYER);
        assertFalse(readiness.isCurrent(second));
        assertEquals(
            SafeEntryReadiness.Decision.SUPERSEDED,
            readiness.observeFingerprint(second, true, 7)
        );
    }
}
