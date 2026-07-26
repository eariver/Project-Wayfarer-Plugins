package io.github.eariver.wayfarer.core.task;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultWayfarerTasksTest {
    @Test
    void queuedMainThreadCallbackRevalidatesLifecycle() {
        AtomicBoolean enabled = new AtomicBoolean(true);
        List<Runnable> callbacks = new ArrayList<>();
        try (ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Callback",
            Duration.ofSeconds(1),
            ignored -> {},
            ignored -> {}
        )) {
            DefaultWayfarerTasks tasks = new DefaultWayfarerTasks(
                executor,
                callbacks::add,
                enabled::get
            );
            var result = tasks.mainThread(() -> {});
            enabled.set(false);
            callbacks.getFirst().run();
            CompletionException failure = assertThrows(
                CompletionException.class,
                () -> result.toCompletableFuture().join()
            );
            assertInstanceOf(RejectedExecutionException.class, failure.getCause());
        }
    }

    @Test
    void enabledMainThreadCallbackCompletes() {
        try (ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Callback",
            Duration.ofSeconds(1),
            ignored -> {},
            ignored -> {}
        )) {
            DefaultWayfarerTasks tasks = new DefaultWayfarerTasks(
                executor,
                Runnable::run,
                () -> true
            );
            assertEquals(null, tasks.mainThread(() -> {}).toCompletableFuture().join());
        }
    }
}
