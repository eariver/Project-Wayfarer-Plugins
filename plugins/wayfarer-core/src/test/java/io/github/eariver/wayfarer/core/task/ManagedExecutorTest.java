package io.github.eariver.wayfarer.core.task;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagedExecutorTest {
    @Test
    void taskIsAcceptedWhileEnabled() {
        try (ManagedExecutor executor = executor(Duration.ofSeconds(1))) {
            assertEquals(42, executor.submit(() -> 42).join());
        }
    }

    @Test
    void newTaskIsRejectedAfterShutdown() {
        ManagedExecutor executor = executor(Duration.ofSeconds(1));
        executor.close();
        CompletionException failure = assertThrows(
            CompletionException.class,
            () -> executor.submit(() -> 1).join()
        );
        assertInstanceOf(RejectedExecutionException.class, failure.getCause());
    }

    @Test
    void gracefulShutdownWaitsForAcceptedTask() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean completed = new AtomicBoolean();
        ManagedExecutor executor = executor(Duration.ofSeconds(1));
        executor.submit(() -> {
            started.countDown();
            completed.set(true);
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));
        executor.close();
        assertTrue(completed.get());
        assertTrue(executor.isTerminated());
    }

    @Test
    void timeoutForcesShutdownAndWarns() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        List<String> warnings = new ArrayList<>();
        ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Timeout",
            Duration.ofMillis(10),
            ignored -> {},
            warnings::add
        );
        executor.submit(() -> {
            started.countDown();
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));
        executor.close();
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("forcing"));
    }

    @Test
    void workerThreadNameContainsWayfarerIdentifier() {
        try (ManagedExecutor executor = executor(Duration.ofSeconds(1))) {
            String name = executor.submit(() -> Thread.currentThread().getName()).join();
            assertTrue(name.contains("Wayfarer"));
        }
    }

    @Test
    void taskFailureIsObserved() {
        AtomicReference<Throwable> observed = new AtomicReference<>();
        try (ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Failure",
            Duration.ofSeconds(1),
            observed::set,
            ignored -> {}
        )) {
            CompletionException failure = assertThrows(
                CompletionException.class,
                () -> executor.submit(() -> {
                    throw new IllegalStateException("expected");
                }).join()
            );
            assertInstanceOf(IllegalStateException.class, failure.getCause());
            assertInstanceOf(IllegalStateException.class, observed.get());
        }
    }

    @Test
    void acceptingFlagIsClearedBeforeShutdownWait() {
        ManagedExecutor executor = executor(Duration.ofSeconds(1));
        assertTrue(executor.isAccepting());
        executor.close();
        assertFalse(executor.isAccepting());
    }

    @Test
    void diagnosticFailureDoesNotPreventForcedShutdown() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-DiagnosticFailure",
            Duration.ofMillis(10),
            ignored -> {},
            warning -> {
                throw new IllegalStateException("diagnostics unavailable");
            }
        );
        executor.submit(() -> {
            started.countDown();
            Thread.sleep(10_000);
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));
        assertDoesNotThrow(executor::close);
        assertFalse(executor.isAccepting());
    }

    private static ManagedExecutor executor(Duration timeout) {
        return new ManagedExecutor(
            1,
            "Wayfarer-Test",
            timeout,
            ignored -> {},
            ignored -> {}
        );
    }
}
