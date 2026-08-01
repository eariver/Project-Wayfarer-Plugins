package io.github.eariver.wayfarer.frontier.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

final class PlayerOperationSerializerTest {
    private static final UUID PLAYER_A =
        UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID PLAYER_B =
        UUID.fromString("00000000-0000-0000-0000-0000000000b1");

    @Test
    void samePlayerFifo() throws Exception {
        PlayerOperationSerializer serializer = new PlayerOperationSerializer();
        List<String> order = new CopyOnWriteArrayList<>();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CompletionStage<String> first = serializer.enqueue(PLAYER_A, () -> {
            order.add("first-start");
            firstStarted.countDown();
            return CompletableFuture.supplyAsync(() -> {
                await(releaseFirst);
                order.add("first-end");
                return "first";
            });
        });
        CompletionStage<String> second = serializer.enqueue(PLAYER_A, () -> {
            order.add("second-start");
            return CompletableFuture.completedFuture("second");
        });
        assertTrue(firstStarted.await(2, TimeUnit.SECONDS));
        Thread.sleep(50);
        assertFalse(order.contains("second-start"));
        releaseFirst.countDown();
        assertEquals("first", first.toCompletableFuture().get(2, TimeUnit.SECONDS));
        assertEquals("second", second.toCompletableFuture().get(2, TimeUnit.SECONDS));
        assertEquals(List.of("first-start", "first-end", "second-start"), order);
    }

    @Test
    void differentPlayersConcurrent() throws Exception {
        PlayerOperationSerializer serializer = new PlayerOperationSerializer();
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        CompletionStage<String> a = serializer.enqueue(PLAYER_A, () -> {
            bothStarted.countDown();
            return CompletableFuture.supplyAsync(() -> {
                await(release);
                return "a";
            });
        });
        CompletionStage<String> b = serializer.enqueue(PLAYER_B, () -> {
            bothStarted.countDown();
            return CompletableFuture.supplyAsync(() -> {
                await(release);
                return "b";
            });
        });
        assertTrue(bothStarted.await(2, TimeUnit.SECONDS));
        release.countDown();
        assertEquals("a", a.toCompletableFuture().get(2, TimeUnit.SECONDS));
        assertEquals("b", b.toCompletableFuture().get(2, TimeUnit.SECONDS));
    }

    @Test
    void failedOperationDoesNotPoisonTail() throws Exception {
        PlayerOperationSerializer serializer = new PlayerOperationSerializer();
        CompletionStage<String> failed = serializer.enqueue(PLAYER_A, () ->
            CompletableFuture.failedFuture(new IllegalStateException("boom"))
        );
        assertThrows(Exception.class, () ->
            failed.toCompletableFuture().get(2, TimeUnit.SECONDS)
        );
        CompletionStage<String> next = serializer.enqueue(PLAYER_A, () ->
            CompletableFuture.completedFuture("recovered")
        );
        assertEquals("recovered", next.toCompletableFuture().get(2, TimeUnit.SECONDS));
    }

    @Test
    void shutdownRejectsNewAndUnstartedSuppliers() throws Exception {
        PlayerOperationSerializer serializer = new PlayerOperationSerializer();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicBoolean secondStarted = new AtomicBoolean();
        serializer.enqueue(PLAYER_A, () -> {
            firstStarted.countDown();
            return CompletableFuture.supplyAsync(() -> {
                await(releaseFirst);
                return "first";
            });
        });
        assertTrue(firstStarted.await(2, TimeUnit.SECONDS));
        CompletionStage<String> second = serializer.enqueue(PLAYER_A, () -> {
            secondStarted.set(true);
            return CompletableFuture.completedFuture("second");
        });
        serializer.shutdown();
        releaseFirst.countDown();
        assertThrows(Exception.class, () ->
            second.toCompletableFuture().get(2, TimeUnit.SECONDS)
        );
        assertFalse(secondStarted.get());
        assertThrows(Exception.class, () ->
            serializer.enqueue(PLAYER_A, () ->
                CompletableFuture.completedFuture("nope")
            ).toCompletableFuture().get(2, TimeUnit.SECONDS)
        );
    }

    @Test
    void deathDoesNotOvertakeSafeEntry() throws Exception {
        PlayerOperationSerializer serializer = new PlayerOperationSerializer();
        List<String> order = new CopyOnWriteArrayList<>();
        CountDownLatch deathGate = new CountDownLatch(1);
        long[] safeStart = {0L};
        long[] deathEnd = {0L};
        CompletionStage<Void> death = serializer.enqueue(PLAYER_A, () -> {
            order.add("death");
            return CompletableFuture.runAsync(() -> {
                await(deathGate);
                deathEnd[0] = System.nanoTime();
            });
        });
        CompletionStage<Void> safe = serializer.enqueue(PLAYER_A, () -> {
            safeStart[0] = System.nanoTime();
            order.add("safe");
            return CompletableFuture.completedFuture(null);
        });
        Thread.sleep(30);
        assertEquals(List.of("death"), new ArrayList<>(order));
        deathGate.countDown();
        death.toCompletableFuture().get(2, TimeUnit.SECONDS);
        safe.toCompletableFuture().get(2, TimeUnit.SECONDS);
        assertTrue(safeStart[0] >= deathEnd[0]);
        assertEquals(List.of("death", "safe"), order);
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failure);
        }
    }
}
