package io.github.eariver.wayfarer.core.task;

import java.time.Duration;
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
        accepting.set(false);
        executor.shutdown();
        boolean terminated = false;
        try {
            terminated = executor.awaitTermination(
                shutdownTimeout.toMillis(),
                TimeUnit.MILLISECONDS
            );
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
        if (!terminated) {
            executor.shutdownNow();
            try {
                warningSink.accept(
                    "Wayfarer executor exceeded shutdown timeout; forcing termination"
                );
            } catch (RuntimeException ignored) {
                // Forced shutdown must complete even if diagnostics are unavailable.
            }
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
