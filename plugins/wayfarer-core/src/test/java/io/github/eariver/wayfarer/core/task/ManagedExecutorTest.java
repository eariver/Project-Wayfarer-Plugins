package io.github.eariver.wayfarer.core.task;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import static org.junit.jupiter.api.Assertions.assertSame;
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
        executor.shutdown();
        CompletionException failure = assertThrows(
            CompletionException.class,
            () -> executor.submit(() -> 1).join()
        );
        assertInstanceOf(RejectedExecutionException.class, failure.getCause());
    }

    @Test
    void gracefulShutdownReturnsGracefulResult() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean completed = new AtomicBoolean();
        ManagedExecutor executor = executor(Duration.ofSeconds(1));
        executor.submit(() -> {
            started.countDown();
            completed.set(true);
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));
        ShutdownResult result = executor.shutdown();
        assertTrue(completed.get());
        assertEquals(ShutdownStatus.GRACEFUL, result.status());
        assertTrue(result.terminated());
        assertEquals(0, result.droppedTaskCount());
        assertTrue(executor.isTerminated());
    }

    @Test
    void forcedShutdownWaitsForTermination() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<ShutdownResult> result = new AtomicReference<>();
        ManagedExecutor executor = executor(Duration.ofMillis(250));
        executor.submit(() -> {
            started.countDown();
            try {
                release.await();
            } catch (InterruptedException expected) {
                interrupted.countDown();
                release.await();
            }
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));

        Thread shutdownThread = new Thread(
            () -> result.set(executor.shutdown()),
            "Wayfarer-Test-Shutdown"
        );
        shutdownThread.start();
        assertTrue(interrupted.await(1, TimeUnit.SECONDS));
        assertTrue(shutdownThread.isAlive());
        release.countDown();
        shutdownThread.join(1_000);

        assertFalse(shutdownThread.isAlive());
        assertEquals(ShutdownStatus.FORCED_TERMINATED, result.get().status());
        assertTrue(result.get().terminated());
    }

    @Test
    void forcedTerminationReturnsForcedResult() throws Exception {
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
        ShutdownResult result = executor.shutdown();
        assertEquals(ShutdownStatus.FORCED_TERMINATED, result.status());
        assertTrue(result.terminated());
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("forcing"));
    }

    @Test
    void interruptIgnoringTaskReturnsIncompleteResult() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ManagedExecutor executor = executor(Duration.ofMillis(20));
        try {
            executor.submit(() -> {
                started.countDown();
                awaitUninterruptibly(release);
                return null;
            });
            assertTrue(started.await(1, TimeUnit.SECONDS));

            ShutdownResult result = executor.shutdown();

            assertEquals(ShutdownStatus.INCOMPLETE, result.status());
            assertFalse(result.terminated());
            assertFalse(executor.isTerminated());
        } finally {
            release.countDown();
            awaitTermination(executor);
        }
    }

    @Test
    void secondShutdownIsSafe() {
        ManagedExecutor executor = executor(Duration.ofSeconds(1));
        ShutdownResult first = executor.shutdown();
        ShutdownResult second = executor.shutdown();
        assertSame(first, second);
        assertEquals(ShutdownStatus.GRACEFUL, second.status());
    }

    @Test
    void interruptedShutdownRestoresInterruptFlag() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        AtomicReference<ShutdownResult> result = new AtomicReference<>();
        AtomicBoolean interruptRestored = new AtomicBoolean();
        ManagedExecutor executor = executor(Duration.ofSeconds(1));
        executor.submit(() -> {
            started.countDown();
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException expected) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));

        Thread shutdownThread = new Thread(() -> {
            result.set(executor.shutdown());
            interruptRestored.set(Thread.currentThread().isInterrupted());
        }, "Wayfarer-Test-Interrupted-Shutdown");
        shutdownThread.start();
        awaitNotAccepting(executor);
        shutdownThread.interrupt();
        shutdownThread.join(2_000);

        assertFalse(shutdownThread.isAlive());
        assertEquals(ShutdownStatus.INTERRUPTED, result.get().status());
        assertTrue(result.get().terminated());
        assertTrue(interruptRestored.get());
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
    void boundedQueueRejectsOverflowAndDrainsAlreadyAcceptedTask() throws Exception {
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        List<Throwable> observed = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Bounded",
            1,
            Duration.ofSeconds(1),
            observed::add,
            warnings::add
        );
        try {
            CompletableFuture<Integer> first = executor.submit(() -> {
                firstStarted.countDown();
                releaseFirst.await();
                return 1;
            });
            assertTrue(firstStarted.await(1, TimeUnit.SECONDS));
            CompletableFuture<Integer> queued = executor.submit(() -> 2);
            assertEquals(1, executor.queuedTaskCount());
            assertEquals(0, executor.remainingQueueCapacity());

            CompletionException overflow = assertThrows(
                CompletionException.class,
                () -> executor.submit(() -> 3).join()
            );
            assertInstanceOf(RejectedExecutionException.class, overflow.getCause());
            assertEquals(1, observed.size());
            assertEquals(
                "Wayfarer executor queue capacity exceeded; task rejected",
                warnings.getFirst()
            );

            AtomicReference<ShutdownResult> shutdownResult = new AtomicReference<>();
            Thread shutdown = new Thread(
                () -> shutdownResult.set(executor.shutdown()),
                "Wayfarer-Bounded-Shutdown"
            );
            shutdown.start();
            awaitNotAccepting(executor);
            releaseFirst.countDown();
            assertEquals(1, first.join());
            assertEquals(2, queued.join());
            shutdown.join(1_000);
            assertFalse(shutdown.isAlive());
            assertEquals(ShutdownStatus.GRACEFUL, shutdownResult.get().status());
        } finally {
            releaseFirst.countDown();
            executor.shutdown();
        }
    }

    @Test
    void forcedShutdownCompletesDroppedQueuedTaskExceptionally() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Dropped",
            1,
            Duration.ofMillis(10),
            ignored -> {},
            ignored -> {}
        );
        try {
            executor.submit(() -> {
                started.countDown();
                awaitUninterruptibly(release);
                return 1;
            });
            assertTrue(started.await(1, TimeUnit.SECONDS));
            CompletableFuture<Integer> queued = executor.submit(() -> 2);

            ShutdownResult result = executor.shutdown();

            assertEquals(ShutdownStatus.INCOMPLETE, result.status());
            assertEquals(1, result.droppedTaskCount());
            CompletionException failure = assertThrows(CompletionException.class, queued::join);
            assertInstanceOf(RejectedExecutionException.class, failure.getCause());
        } finally {
            release.countDown();
            awaitTermination(executor);
        }
    }

    @Test
    void acceptingFlagIsClearedBeforeShutdownWait() {
        ManagedExecutor executor = executor(Duration.ofSeconds(1));
        assertTrue(executor.isAccepting());
        executor.shutdown();
        assertFalse(executor.isAccepting());
    }

    @Test
    void diagnosticFailureDoesNotHideShutdownResult() throws Exception {
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
        AtomicReference<ShutdownResult> result = new AtomicReference<>();
        assertDoesNotThrow(() -> result.set(executor.shutdown()));
        assertEquals(ShutdownStatus.FORCED_TERMINATED, result.get().status());
        assertTrue(result.get().terminated());
        assertFalse(executor.isAccepting());
    }

    private static void awaitUninterruptibly(CountDownLatch release) {
        boolean interrupted = false;
        while (release.getCount() > 0) {
            try {
                release.await();
            } catch (InterruptedException ignored) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static void awaitTermination(ManagedExecutor executor) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (!executor.isTerminated() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertTrue(executor.isTerminated());
    }

    private static void awaitNotAccepting(ManagedExecutor executor) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (executor.isAccepting() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
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
