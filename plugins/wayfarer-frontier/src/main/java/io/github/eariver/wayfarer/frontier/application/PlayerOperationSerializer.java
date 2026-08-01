package io.github.eariver.wayfarer.frontier.application;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Per-player FIFO serializer. Same player operations run in registration order;
 * different players may run concurrently. Nested enqueue for the same player is
 * forbidden and must not be performed from inside an operation supplier.
 */
public final class PlayerOperationSerializer {
    private final ConcurrentHashMap<UUID, CompletableFuture<?>> tails =
        new ConcurrentHashMap<>();
    private final AtomicBoolean accepting = new AtomicBoolean(true);

    public <T> CompletionStage<T> enqueue(
        UUID playerUuid,
        Supplier<CompletionStage<T>> operation
    ) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(operation, "operation");
        if (!accepting.get()) {
            return CompletableFuture.failedFuture(rejected());
        }
        CompletableFuture<T> run = new CompletableFuture<>();
        CompletableFuture<?> previous = tails.put(playerUuid, run);
        CompletionStage<?> gate = previous == null
            ? CompletableFuture.completedFuture(null)
            : previous.handle((ignored, failure) -> null);
        gate.whenComplete((ignored, gateFailure) -> {
            if (!accepting.get()) {
                run.completeExceptionally(rejected());
                return;
            }
            CompletionStage<T> started;
            try {
                started = operation.get();
            } catch (Throwable failure) {
                run.completeExceptionally(failure);
                return;
            }
            if (started == null) {
                run.completeExceptionally(new NullPointerException("operation"));
                return;
            }
            started.whenComplete((value, failure) -> {
                if (failure != null) {
                    run.completeExceptionally(failure);
                } else {
                    run.complete(value);
                }
            });
        });
        run.whenComplete((ignored, failure) -> tails.remove(playerUuid, run));
        return run;
    }

    public void shutdown() {
        accepting.set(false);
    }

    public boolean accepting() {
        return accepting.get();
    }

    private static RejectedExecutionException rejected() {
        return new RejectedExecutionException(
            "Player operation serializer is shut down"
        );
    }
}
