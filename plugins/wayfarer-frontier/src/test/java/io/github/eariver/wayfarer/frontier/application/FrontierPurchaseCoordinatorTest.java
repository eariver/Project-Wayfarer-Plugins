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
            FrontierPurchaseCoordinator.Status.PENDING,
            coordinator.purchase(request).toCompletableFuture().join().status()
        );
        assertEquals(
            FrontierPurchaseCoordinator.Status.PENDING,
            coordinator.purchase(request).toCompletableFuture().join().status()
        );
        assertEquals(1, transactions.executeCalls);
    }

    @Test
    void deliversOnMainThreadAndReplayDoesNotGrantTwice() {
        FakeTransactions transactions = new FakeTransactions();
        java.util.concurrent.atomic.AtomicInteger grants =
            new java.util.concurrent.atomic.AtomicInteger();
        FrontierPurchaseCoordinator coordinator = coordinator(
            transactions,
            (purchase, deliveryId) -> {
                grants.incrementAndGet();
                return true;
            }
        );
        FrontierPurchaseCoordinator.Request request =
            request("frontier_iris", "firework_rocket");

        assertEquals(FrontierPurchaseCoordinator.Status.DELIVERED,
            coordinator.purchase(request).toCompletableFuture().join().status());
        assertEquals(FrontierPurchaseCoordinator.Status.DELIVERED,
            coordinator.purchase(request).toCompletableFuture().join().status());
        assertEquals(1, transactions.executeCalls);
        assertEquals(1, grants.get());
    }

    @Test
    void ambiguousTransactionFailureIsUnknownAndIsNotRetried() {
        FakeTransactions transactions = new FakeTransactions();
        transactions.failSynchronously = true;
        FrontierPurchaseCoordinator coordinator = coordinator(transactions);
        FrontierPurchaseCoordinator.Request request = request("frontier_iris", "launchpad");

        assertEquals(
            FrontierPurchaseCoordinator.Status.UNKNOWN,
            coordinator.purchase(request).toCompletableFuture().join().status()
        );
        assertEquals(
            FrontierPurchaseCoordinator.Status.UNKNOWN,
            coordinator.purchase(request).toCompletableFuture().join().status()
        );
        assertEquals(1, transactions.executeCalls);
    }

    private static FrontierPurchaseCoordinator coordinator(FakeTransactions transactions) {
        return coordinator(transactions, (purchase, deliveryId) -> false);
    }

    private static FrontierPurchaseCoordinator coordinator(
        FakeTransactions transactions,
        FrontierPurchaseCoordinator.DeliveryGateway delivery
    ) {
        return new FrontierPurchaseCoordinator(
            FrontierWorldGate.worldsBeyondDefault(),
            new FrontierShopCatalog(),
            new FakeRepository(),
            transactions,
            new DirectTasks(),
            delivery,
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
        public Optional<Purchase> claimPayment(
            UUID purchaseId,
            long expectedLockVersion,
            Instant now
        ) {
            Purchase current = find(purchaseId).orElseThrow();
            if (current.state() != State.PREPARED
                || current.lockVersion() != expectedLockVersion) {
                return Optional.empty();
            }
            Purchase claimed = transition(current, State.PAYMENT_PENDING, null);
            purchases.put(claimed.idempotencyKey(), claimed);
            return Optional.of(claimed);
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
            if (current.state() != State.PAYMENT_PENDING
                || current.lockVersion() != expectedLockVersion) {
                return false;
            }
            purchases.put(current.idempotencyKey(),
                transition(current, State.PAYMENT_COMMITTED, transactionId, null));
            return true;
        }

        @Override
        public Optional<Purchase> attachPendingDelivery(
            UUID purchaseId,
            long expectedLockVersion,
            io.github.eariver.wayfarer.frontier.domain.PendingDelivery delivery,
            Instant now
        ) {
            Purchase current = find(purchaseId).orElseThrow();
            if (current.state() != State.PAYMENT_COMMITTED
                || current.lockVersion() != expectedLockVersion) {
                return Optional.empty();
            }
            Purchase pending = transition(
                current,
                State.PENDING_DELIVERY,
                current.transactionId(),
                delivery.deliveryId()
            );
            purchases.put(current.idempotencyKey(), pending);
            return Optional.of(pending);
        }

        @Override
        public boolean markDelivered(
            UUID purchaseId,
            UUID deliveryId,
            long expectedLockVersion,
            Instant now
        ) {
            Purchase current = find(purchaseId).orElseThrow();
            if (current.state() != State.PENDING_DELIVERY
                || !deliveryId.equals(current.deliveryId())
                || current.lockVersion() != expectedLockVersion) {
                return false;
            }
            purchases.put(current.idempotencyKey(),
                transition(current, State.DELIVERED, current.transactionId(), deliveryId));
            return true;
        }

        @Override
        public boolean markFailed(
            UUID purchaseId,
            long expectedLockVersion,
            String failureCode,
            Instant now
        ) {
            Purchase current = find(purchaseId).orElseThrow();
            if (current.lockVersion() != expectedLockVersion) {
                return false;
            }
            purchases.put(current.idempotencyKey(),
                transition(current, State.FAILED, null, null));
            return true;
        }

        @Override
        public void markUnknown(
            UUID purchaseId,
            long expectedLockVersion,
            String failureCode,
            Instant now
        ) {
            Purchase current = find(purchaseId).orElseThrow();
            purchases.put(current.idempotencyKey(),
                transition(current, State.UNKNOWN, current.transactionId(),
                    current.deliveryId()));
        }

        @Override
        public Optional<Purchase> find(UUID purchaseId) {
            return purchases.values().stream()
                .filter(value -> value.purchaseId().equals(purchaseId))
                .findFirst();
        }

        private static Purchase transition(
            Purchase current,
            State state,
            UUID transactionId
        ) {
            return transition(current, state, transactionId, current.deliveryId());
        }

        private static Purchase transition(
            Purchase current,
            State state,
            UUID transactionId,
            UUID deliveryId
        ) {
            return new Purchase(
                current.purchaseId(),
                current.idempotencyKey(),
                current.playerUuid(),
                current.offer(),
                state,
                transactionId,
                deliveryId,
                current.lockVersion() + 1
            );
        }
    }

    private static final class FakeTransactions implements WayfarerTransactions {
        private int executeCalls;
        private boolean failSynchronously;

        @Override
        public CompletionStage<TransactionResult> execute(TransactionRequest request) {
            executeCalls++;
            if (failSynchronously) {
                throw new IllegalStateException("synthetic ambiguous failure");
            }
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
