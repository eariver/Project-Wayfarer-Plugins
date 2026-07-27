package io.github.eariver.wayfarer.core.persistence;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceWorkGateTest {
    @Test
    void drainTimeoutIsBoundedAndKeepsIntakeClosed() {
        PersistenceWorkGate gate = new PersistenceWorkGate();
        PersistenceWorkGate.Permit permit = gate.acquire();
        long started = System.nanoTime();

        PersistenceDrainResult result =
            gate.stopAcceptingAndAwait(Duration.ofMillis(20));

        assertEquals(PersistenceDrainStatus.TIMED_OUT, result.status());
        assertEquals(1, result.remainingInFlight());
        assertFalse(gate.isAccepting());
        assertTrue(Duration.ofNanos(System.nanoTime() - started).compareTo(
            Duration.ofSeconds(1)
        ) < 0);
        permit.close();
        assertEquals(0, gate.inFlightCount());
    }

    @Test
    void interruptedDrainRestoresInterruptAndDoesNotReportCleanDrain() throws Exception {
        PersistenceWorkGate gate = new PersistenceWorkGate();
        PersistenceWorkGate.Permit permit = gate.acquire();
        AtomicReference<PersistenceDrainResult> result = new AtomicReference<>();
        AtomicBoolean interruptRestored = new AtomicBoolean();
        Thread drainThread = new Thread(() -> {
            result.set(gate.stopAcceptingAndAwait(Duration.ofSeconds(5)));
            interruptRestored.set(Thread.currentThread().isInterrupted());
        }, "Wayfarer-Persistence-Drain-Test");

        drainThread.start();
        assertTrue(gate.awaitIntakeStopped(Duration.ofSeconds(1)));
        drainThread.interrupt();
        drainThread.join(1_000);

        assertFalse(drainThread.isAlive());
        assertEquals(PersistenceDrainStatus.INTERRUPTED, result.get().status());
        assertEquals(1, result.get().remainingInFlight());
        assertTrue(interruptRestored.get());
        assertFalse(gate.isAccepting());
        permit.close();
    }

    @Test
    void permitAndDrainDoubleCloseAreIdempotent() {
        PersistenceWorkGate gate = new PersistenceWorkGate();
        PersistenceWorkGate.Permit permit = gate.acquire();
        permit.close();
        permit.close();

        PersistenceDrainResult first =
            gate.stopAcceptingAndAwait(Duration.ofSeconds(1));
        PersistenceDrainResult second =
            gate.stopAcceptingAndAwait(Duration.ofSeconds(1));

        assertEquals(PersistenceDrainStatus.DRAINED, first.status());
        assertEquals(first, second);
        assertEquals(0, gate.inFlightCount());
    }
}
