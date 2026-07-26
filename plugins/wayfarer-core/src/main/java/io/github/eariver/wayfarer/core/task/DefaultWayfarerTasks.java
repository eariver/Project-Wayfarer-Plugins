package io.github.eariver.wayfarer.core.task;

import io.github.eariver.wayfarer.api.WayfarerTasks;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class DefaultWayfarerTasks implements WayfarerTasks {
    private final ManagedExecutor executor;
    private final MainThreadDispatcher mainThread;
    private final BooleanSupplier callbacksAllowed;

    public DefaultWayfarerTasks(
        ManagedExecutor executor,
        MainThreadDispatcher mainThread,
        BooleanSupplier callbacksAllowed
    ) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.callbacksAllowed = Objects.requireNonNull(callbacksAllowed, "callbacksAllowed");
    }

    @Override
    public <T> CompletionStage<T> database(Supplier<T> operation) {
        Objects.requireNonNull(operation, "operation");
        if (!callbacksAllowed.getAsBoolean()) {
            return rejected();
        }
        return executor.submit(operation::get);
    }

    @Override
    public CompletionStage<Void> mainThread(Runnable operation) {
        Objects.requireNonNull(operation, "operation");
        if (!callbacksAllowed.getAsBoolean()) {
            return rejected();
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            mainThread.dispatch(() -> {
                if (!callbacksAllowed.getAsBoolean()) {
                    result.completeExceptionally(
                        new RejectedExecutionException("Wayfarer lifecycle does not accept callbacks")
                    );
                    return;
                }
                try {
                    operation.run();
                    result.complete(null);
                } catch (Throwable failure) {
                    result.completeExceptionally(failure);
                }
            });
        } catch (RuntimeException failure) {
            result.completeExceptionally(failure);
        }
        return result;
    }

    private static <T> CompletableFuture<T> rejected() {
        return CompletableFuture.failedFuture(
            new RejectedExecutionException("Wayfarer lifecycle does not accept callbacks")
        );
    }
}
