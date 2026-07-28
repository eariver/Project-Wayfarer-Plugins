package io.github.eariver.wayfarer.core.task;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void bridgeUsesImmutableWorkerDataThenRevalidatesAndMutatesOnMainThread() {
        List<Runnable> callbacks = new ArrayList<>();
        AtomicReference<String> workerThread = new AtomicReference<>();
        AtomicInteger mutations = new AtomicInteger();
        try (ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Bridge",
            Duration.ofSeconds(1),
            ignored -> {},
            ignored -> {}
        )) {
            DefaultWayfarerTasks tasks = new DefaultWayfarerTasks(
                executor,
                callbacks::add,
                () -> true
            );
            TaskRequest request = new TaskRequest(UUID.randomUUID(), 3);
            var completion = tasks.bridge(
                request,
                value -> {
                    workerThread.set(Thread.currentThread().getName());
                    return new TaskResult(value.ownerId(), value.revision() + 1);
                },
                result -> result.revision() == 4,
                ignored -> mutations.incrementAndGet()
            ).toCompletableFuture();

            awaitCallback(callbacks);
            assertFalse(completion.isDone());
            callbacks.getFirst().run();

            assertTrue(workerThread.get().contains("Wayfarer-Bridge"));
            assertTrue(completion.join().applied());
            assertEquals(4, completion.join().immutableResult().revision());
            assertEquals(1, mutations.get());
        }
    }

    @Test
    void staleBridgeResultDoesNotMutate() {
        AtomicInteger mutations = new AtomicInteger();
        try (ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Stale",
            Duration.ofSeconds(1),
            ignored -> {},
            ignored -> {}
        )) {
            DefaultWayfarerTasks tasks = new DefaultWayfarerTasks(
                executor,
                Runnable::run,
                () -> true
            );
            var result = tasks.bridge(
                new TaskRequest(UUID.randomUUID(), 1),
                value -> new TaskResult(value.ownerId(), 2),
                ignored -> false,
                ignored -> mutations.incrementAndGet()
            ).toCompletableFuture().join();

            assertFalse(result.applied());
            assertEquals(0, mutations.get());
        }
    }

    @Test
    void bridgeRejectsMutableAndBukkitSnapshotsBeforeWorkerSubmission() {
        try (ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Guard",
            Duration.ofSeconds(1),
            ignored -> {},
            ignored -> {}
        )) {
            DefaultWayfarerTasks tasks = new DefaultWayfarerTasks(
                executor,
                Runnable::run,
                () -> true
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> tasks.bridge(new Object(), ignored -> "ok", ignored -> true, ignored -> {})
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> tasks.bridge(Material.STONE, ignored -> "ok", ignored -> true, ignored -> {})
            );
            CompletionException resultFailure = assertThrows(
                CompletionException.class,
                () -> tasks.bridge(
                    "request",
                    ignored -> Material.STONE,
                    ignored -> true,
                    ignored -> {}
                ).toCompletableFuture().join()
            );
            assertInstanceOf(IllegalArgumentException.class, resultFailure.getCause());
        }
    }

    private static void awaitCallback(List<Runnable> callbacks) {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(1);
        while (callbacks.isEmpty() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertFalse(callbacks.isEmpty());
    }

    private record TaskRequest(UUID ownerId, int revision) {}

    private record TaskResult(UUID ownerId, int revision) {}
}
