package io.github.eariver.wayfarer.frontier.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.frontier.domain.FrontierShopCatalog;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

final class FrontierPurchaseCoordinatorTest {
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000040");
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");

    @Test
    void rejectsWorldAndWaystoneBeforeAnyDebit() {
        FakeTransactions transactions = new FakeTransactions();
        FrontierPurchaseCoordinator coordinator = coordinator(transactions);

        assertEquals(
            FrontierPurchaseCoordinator.Status.FAILED,
            coordinator.purchase(request("main", "launchpad")).toCompletableFuture().join().status()
        );
        assertEquals(
            FrontierPurchaseCoordinator.Status.FAILED,
            coordinator.purchase(request("frontier_iris", "waystone_placement_tool"))
                .toCompletableFuture().join().status()
        );
        assertEquals(0, transactions.executeCalls);
    }

    @Test
    void usesCoreTransactionAndSameIdempotencyKeyDoesNotDebitTwice() {
        FakeTransactions transactions = new FakeTransactions();
        FrontierPurchaseCoordinator coordinator = coordinator(transactions);
        FrontierPurchaseCoordinator.Request request = request("frontier_iris", "launchpad");

        assertEquals(
            FrontierPurchaseCoordinator.Status.ACCEPTED,
            coordinator.purchase(request).toCompletableFuture().join().status()
        );
        assertEquals(
            FrontierPurchaseCoordinator.Status.ACCEPTED,
            coordinator.purchase(request).toCompletableFuture().join().status()
        );
        assertEquals(1, transactions.executeCalls);
    }

    private static FrontierPurchaseCoordinator coordinator(FakeTransactions transactions) {
        return new FrontierPurchaseCoordinator(
            FrontierWorldGate.worldsBeyondDefault(),
            new FrontierShopCatalog(),
            new FakeRepository(),
            transactions,
            new DirectTasks(),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private static FrontierPurchaseCoordinator.Request request(String world, String offer) {
        return new FrontierPurchaseCoordinator.Request(
            "request:00000001",
            PLAYER,
            world,
            offer
        );
    }

    private static final class FakeRepository implements FrontierPurchaseRepository {
        private final Map<String, Purchase> purchases = new HashMap<>();

        @Override
        public Purchase prepare(
            String idempotencyKey,
            UUID playerUuid,
            FrontierShopCatalog.Offer offer,
            Instant now
        ) {
            return purchases.computeIfAbsent(idempotencyKey, ignored -> new Purchase(
                UUID.fromString("00000000-0000-0000-0000-000000000041"),
                idempotencyKey,
                playerUuid,
                offer,
                State.PREPARED,
                null,
                0
            ));
        }

        @Override
        public boolean markPaymentCommitted(
            UUID purchaseId,
            UUID transactionId,
            long expectedLockVersion,
            Instant now
        ) {
            Purchase current = purchases.values().stream()
                .filter(value -> value.purchaseId().equals(purchaseId))
                .findFirst()
                .orElseThrow();
            purchases.put(current.idempotencyKey(), new Purchase(
                current.purchaseId(),
                current.idempotencyKey(),
                current.playerUuid(),
                current.offer(),
                State.PAYMENT_COMMITTED,
                transactionId,
                1
            ));
            return true;
        }

        @Override
        public Optional<Purchase> find(UUID purchaseId) {
            return purchases.values().stream()
                .filter(value -> value.purchaseId().equals(purchaseId))
                .findFirst();
        }
    }

    private static final class FakeTransactions implements WayfarerTransactions {
        private int executeCalls;

        @Override
        public CompletionStage<TransactionResult> execute(TransactionRequest request) {
            executeCalls++;
            return CompletableFuture.completedFuture(new TransactionResult(
                UUID.fromString("00000000-0000-0000-0000-000000000042"),
                State.COMMITTED,
                null
            ));
        }

        @Override
        public CompletionStage<TransactionResult> reconcile(UUID transactionId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class DirectTasks implements WayfarerTasks {
        @Override
        public <T> CompletionStage<T> database(Supplier<T> operation) {
            return CompletableFuture.completedFuture(operation.get());
        }

        @Override
        public <I, O> CompletionStage<TaskBridgeResult<O>> bridge(
            I immutableRequest,
            Function<? super I, ? extends O> asyncOperation,
            Predicate<? super O> mainThreadRevalidation,
            Consumer<? super O> mainThreadMutation
        ) {
            O result = asyncOperation.apply(immutableRequest);
            boolean applied = mainThreadRevalidation.test(result);
            if (applied) {
                mainThreadMutation.accept(result);
            }
            return CompletableFuture.completedFuture(new TaskBridgeResult<>(result, applied));
        }

        @Override
        public CompletionStage<Void> mainThread(Runnable operation) {
            operation.run();
            return CompletableFuture.completedFuture(null);
        }
    }
}
