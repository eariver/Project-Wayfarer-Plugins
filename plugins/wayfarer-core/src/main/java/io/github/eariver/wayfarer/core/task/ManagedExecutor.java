package io.github.eariver.wayfarer.core.task;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class ManagedExecutor implements AutoCloseable {
    private final ExecutorService executor;
    private final Duration shutdownTimeout;
    private final Consumer<Throwable> failureObserver;
    private final Consumer<String> warningSink;
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final Object shutdownLock = new Object();
    private volatile ShutdownResult shutdownResult;

    public ManagedExecutor(
        int threads,
        String threadNamePrefix,
        Duration shutdownTimeout,
        Consumer<Throwable> failureObserver,
        Consumer<String> warningSink
    ) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be positive");
        }
        if (threadNamePrefix == null || !threadNamePrefix.contains("Wayfarer")) {
            throw new IllegalArgumentException("threadNamePrefix must contain Wayfarer");
        }
        this.shutdownTimeout = Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        if (shutdownTimeout.isZero() || shutdownTimeout.isNegative()) {
            throw new IllegalArgumentException("shutdownTimeout must be positive");
        }
        this.failureObserver = Objects.requireNonNull(failureObserver, "failureObserver");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
        this.executor = Executors.newFixedThreadPool(
            threads,
            threadFactory(threadNamePrefix, failureObserver)
        );
    }

    public <T> CompletableFuture<T> submit(Callable<T> operation) {
        Objects.requireNonNull(operation, "operation");
        if (!accepting.get()) {
            return CompletableFuture.failedFuture(
                new RejectedExecutionException("Wayfarer executor is stopping")
            );
        }

        CompletableFuture<T> result = new CompletableFuture<>();
        try {
            executor.execute(() -> {
                if (!accepting.get()) {
                    result.completeExceptionally(
                        new RejectedExecutionException("Wayfarer executor is stopping")
                    );
                    return;
                }
                try {
                    result.complete(operation.call());
                } catch (Throwable failure) {
                    observeFailure(failure);
                    result.completeExceptionally(failure);
                }
            });
        } catch (RejectedExecutionException failure) {
            result.completeExceptionally(failure);
        }
        return result;
    }

    public boolean isAccepting() {
        return accepting.get();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public void close() {
        shutdown();
    }

    public ShutdownResult shutdown() {
        synchronized (shutdownLock) {
            if (shutdownResult != null) {
                return shutdownResult;
            }

            accepting.set(false);
            executor.shutdown();
            boolean interrupted = false;
            try {
                if (awaitTermination()) {
                    shutdownResult = new ShutdownResult(
                        ShutdownStatus.GRACEFUL,
                        true,
                        0
                    );
                    return shutdownResult;
                }
            } catch (InterruptedException interruption) {
                interrupted = true;
            }

            List<Runnable> droppedTasks = executor.shutdownNow();
            warn(
                interrupted
                    ? "Wayfarer executor shutdown was interrupted; forcing termination"
                    : "Wayfarer executor exceeded graceful shutdown timeout; forcing termination"
            );

            boolean terminated = executor.isTerminated();
            if (!terminated) {
                try {
                    terminated = awaitTermination();
                } catch (InterruptedException interruption) {
                    interrupted = true;
                    terminated = executor.isTerminated();
                }
            }

            ShutdownStatus status;
            if (interrupted) {
                status = ShutdownStatus.INTERRUPTED;
                warn("Wayfarer executor shutdown was interrupted");
            } else if (terminated) {
                status = ShutdownStatus.FORCED_TERMINATED;
            } else {
                status = ShutdownStatus.INCOMPLETE;
                warn("Wayfarer executor did not terminate after forced shutdown");
            }
            shutdownResult = new ShutdownResult(status, terminated, droppedTasks.size());
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return shutdownResult;
        }
    }

    private boolean awaitTermination() throws InterruptedException {
        return executor.awaitTermination(shutdownTimeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    private void warn(String warning) {
        try {
            warningSink.accept(warning);
        } catch (RuntimeException ignored) {
            // Shutdown result must survive unavailable diagnostics.
        }
    }

    private void observeFailure(Throwable failure) {
        try {
            failureObserver.accept(failure);
        } catch (RuntimeException ignored) {
            // Failure observation must not prevent task completion.
        }
    }

    private static ThreadFactory threadFactory(
        String prefix,
        Consumer<Throwable> failureObserver
    ) {
        AtomicInteger sequence = new AtomicInteger();
        return operation -> {
            Thread thread = new Thread(operation, prefix + "-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((ignored, failure) -> {
                try {
                    failureObserver.accept(failure);
                } catch (RuntimeException observerFailure) {
                    // The executor cannot safely do more if its observer fails.
                }
            });
            return thread;
        };
    }
}
