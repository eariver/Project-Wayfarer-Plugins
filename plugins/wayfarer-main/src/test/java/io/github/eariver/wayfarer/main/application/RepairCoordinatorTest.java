package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymark;
import io.github.eariver.wayfarer.main.domain.RepairOperation;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

final class RepairCoordinatorTest {
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000050");
    private static final UUID TOOL = UUID.fromString("00000000-0000-0000-0000-000000000051");
    private static final UUID TRANSACTION =
        UUID.fromString("00000000-0000-0000-0000-000000000052");
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");

    @Test
    void repeatedRequestDoesNotChargeTwiceAfterCompletedRepair() {
        FakeRepository repository = new FakeRepository();
        FakeTransactions transactions = new FakeTransactions();
        FakeWaymark waymark = new FakeWaymark();
        RepairCoordinator coordinator =
            coordinator(repository, transactions, waymark, (player, tool, epoch, repair) -> true);

        assertEquals(
            RepairCoordinator.Status.COMPLETED,
            coordinator.repair(request()).toCompletableFuture().join().status()
        );
        assertEquals(
            RepairCoordinator.Status.COMPLETED,
            coordinator.repair(request()).toCompletableFuture().join().status()
        );
        assertEquals(1, transactions.executeCalls);
        assertEquals(0, waymark.refundCalls);
    }

    @Test
    void failedPhysicalCommitClaimsAndCallsRefundOnlyOnce() {
        FakeRepository repository = new FakeRepository();
        FakeTransactions transactions = new FakeTransactions();
        FakeWaymark waymark = new FakeWaymark();
        RepairCoordinator coordinator =
            coordinator(repository, transactions, waymark, (player, tool, epoch, repair) -> false);

        assertEquals(
            RepairCoordinator.Status.REFUNDED,
            coordinator.repair(request()).toCompletableFuture().join().status()
        );
        assertEquals(
            RepairCoordinator.Status.REFUNDED,
            coordinator.repair(request()).toCompletableFuture().join().status()
        );
        assertEquals(1, transactions.executeCalls);
        assertEquals(1, waymark.refundCalls);
    }

    @Test
    void successfulPhysicalRepairWithUnknownPersistenceIsNotRefundedOrRetried() {
        FakeRepository repository = new FakeRepository();
        repository.rejectDomainCommit = true;
        FakeTransactions transactions = new FakeTransactions();
        FakeWaymark waymark = new FakeWaymark();
        RepairCoordinator coordinator =
            coordinator(repository, transactions, waymark, (player, tool, epoch, repair) -> true);

        assertEquals(
            RepairCoordinator.Status.UNKNOWN,
            coordinator.repair(request()).toCompletableFuture().join().status()
        );
        assertEquals(
            RepairCoordinator.Status.UNKNOWN,
            coordinator.repair(request()).toCompletableFuture().join().status()
        );
        assertEquals(1, transactions.executeCalls);
        assertEquals(0, waymark.refundCalls);
    }

    @Test
    void clearPaymentFailuresPersistFailedAndReplayWithoutDebit() {
        for (String code : java.util.List.of("INSUFFICIENT_FUNDS", "PROVIDER_REJECTED")) {
            FakeRepository repository = new FakeRepository();
            FakeTransactions transactions = new FakeTransactions();
            transactions.resultState = WayfarerTransactions.State.FAILED;
            transactions.failureCode = code;
            RepairCoordinator coordinator = coordinator(
                repository,
                transactions,
                new FakeWaymark(),
                (player, tool, epoch, repair) -> true
            );

            assertEquals(code,
                coordinator.repair(request()).toCompletableFuture().join().failureCode());
            assertEquals(RepairCoordinator.Status.FAILED,
                coordinator.repair(request()).toCompletableFuture().join().status());
            assertEquals(RepairOperation.State.FAILED, repository.operation.state());
            assertEquals(1, transactions.executeCalls);
        }
    }

    @Test
    void paymentUnknownAndExceptionAreNotRetried() {
        for (boolean exception : java.util.List.of(false, true)) {
            FakeRepository repository = new FakeRepository();
            FakeTransactions transactions = new FakeTransactions();
            transactions.resultState = WayfarerTransactions.State.UNKNOWN;
            transactions.failAsync = exception;
            RepairCoordinator coordinator = coordinator(
                repository,
                transactions,
                new FakeWaymark(),
                (player, tool, epoch, repair) -> true
            );

            assertEquals(RepairCoordinator.Status.UNKNOWN,
                coordinator.repair(request()).toCompletableFuture().join().status());
            assertEquals(RepairCoordinator.Status.UNKNOWN,
                coordinator.repair(request()).toCompletableFuture().join().status());
            assertEquals(1, transactions.executeCalls);
        }
    }

    @Test
    void unknownRefundIsNeverIssuedTwice() {
        FakeRepository repository = new FakeRepository();
        FakeTransactions transactions = new FakeTransactions();
        FakeWaymark waymark = new FakeWaymark();
        waymark.failAsync = true;
        RepairCoordinator coordinator = coordinator(
            repository,
            transactions,
            waymark,
            (player, tool, epoch, repair) -> false
        );

        assertEquals(RepairCoordinator.Status.UNKNOWN,
            coordinator.repair(request()).toCompletableFuture().join().status());
        assertEquals(RepairCoordinator.Status.UNKNOWN,
            coordinator.repair(request()).toCompletableFuture().join().status());
        assertEquals(1, waymark.refundCalls);
    }

    private static RepairCoordinator coordinator(
        FakeRepository repository,
        FakeTransactions transactions,
        FakeWaymark waymark,
        RepairCoordinator.RepairGateway gateway
    ) {
        return new RepairCoordinator(
            repository,
            transactions,
            waymark,
            new DirectTasks(),
            gateway,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private static RepairCoordinator.Request request() {
        return new RepairCoordinator.Request("repair:00000001", PLAYER, TOOL, 1, 100);
    }

    private static final class FakeRepository implements RepairOperationRepository {
        private RepairOperation operation;
        private boolean rejectDomainCommit;

        @Override
        public RepairOperation prepare(
            String idempotencyKey,
            UUID playerUuid,
            UUID toolId,
            long instanceEpoch,
            long amountWaymark,
            Instant now
        ) {
            if (operation == null) {
                operation = new RepairOperation(
                    UUID.fromString("00000000-0000-0000-0000-000000000053"),
                    idempotencyKey,
                    playerUuid,
                    toolId,
                    instanceEpoch,
                    amountWaymark,
                    RepairOperation.State.PREPARED,
                    null,
                    null,
                    0
                );
            }
            return operation;
        }

        @Override
        public Optional<RepairOperation> claimPayment(
            UUID repairId,
            long expectedLockVersion,
            Instant now
        ) {
            if (operation.state() != RepairOperation.State.PREPARED
                || operation.lockVersion() != expectedLockVersion) {
                return Optional.empty();
            }
            operation = transition(RepairOperation.State.PAYMENT_PENDING, null, null);
            return Optional.of(operation);
        }

        @Override
        public Optional<RepairOperation> paymentCommitted(
            UUID repairId,
            UUID transactionId,
            long expectedLockVersion,
            Instant now
        ) {
            if (operation.state() != RepairOperation.State.PAYMENT_PENDING
                || operation.lockVersion() != expectedLockVersion) {
                return Optional.empty();
            }
            operation = transition(RepairOperation.State.PAYMENT_COMMITTED, transactionId, null);
            return Optional.of(operation);
        }

        @Override
        public Optional<RepairOperation> failed(
            UUID repairId,
            long expectedLockVersion,
            String failureCode,
            Instant now
        ) {
            if (operation.state() != RepairOperation.State.PAYMENT_PENDING
                || operation.lockVersion() != expectedLockVersion) {
                return Optional.empty();
            }
            operation = transition(
                RepairOperation.State.FAILED,
                null,
                null,
                failureCode
            );
            return Optional.of(operation);
        }

        @Override
        public boolean domainCommitted(UUID repairId, long expectedLockVersion, Instant now) {
            if (rejectDomainCommit) {
                return false;
            }
            if (operation.state() != RepairOperation.State.PAYMENT_COMMITTED
                || operation.lockVersion() != expectedLockVersion) {
                return false;
            }
            operation = transition(RepairOperation.State.DOMAIN_COMMITTED, TRANSACTION, null);
            return true;
        }

        @Override
        public Optional<RepairOperation> claimRefund(
            UUID repairId,
            long expectedLockVersion,
            Instant now
        ) {
            if (operation.state() != RepairOperation.State.PAYMENT_COMMITTED
                || operation.lockVersion() != expectedLockVersion) {
                return Optional.empty();
            }
            operation = transition(
                RepairOperation.State.REFUND_PENDING,
                TRANSACTION,
                "main-refund:" + operation.repairId()
            );
            return Optional.of(operation);
        }

        @Override
        public boolean refunded(UUID repairId, long expectedLockVersion, Instant now) {
            if (operation.state() != RepairOperation.State.REFUND_PENDING
                || operation.lockVersion() != expectedLockVersion) {
                return false;
            }
            operation = transition(
                RepairOperation.State.REFUNDED,
                TRANSACTION,
                operation.refundOperationId()
            );
            return true;
        }

        @Override
        public void unknown(
            UUID repairId,
            long expectedLockVersion,
            String failureCode,
            Instant now
        ) {
            operation = transition(RepairOperation.State.UNKNOWN, operation.transactionId(),
                operation.refundOperationId());
        }

        private RepairOperation transition(
            RepairOperation.State state,
            UUID transactionId,
            String refundOperationId
        ) {
            return transition(state, transactionId, refundOperationId, null);
        }

        private RepairOperation transition(
            RepairOperation.State state,
            UUID transactionId,
            String refundOperationId,
            String failureCode
        ) {
            return new RepairOperation(
                operation.repairId(),
                operation.idempotencyKey(),
                operation.playerUuid(),
                operation.toolId(),
                operation.instanceEpoch(),
                operation.amountWaymark(),
                state,
                transactionId,
                refundOperationId,
                failureCode,
                operation.lockVersion() + 1
            );
        }
    }

    private static final class FakeTransactions implements WayfarerTransactions {
        private int executeCalls;
        private State resultState = State.COMMITTED;
        private String failureCode;
        private boolean failAsync;

        @Override
        public CompletionStage<TransactionResult> execute(TransactionRequest request) {
            executeCalls++;
            if (failAsync) {
                return CompletableFuture.failedFuture(
                    new IllegalStateException("sensitive transaction failure")
                );
            }
            return CompletableFuture.completedFuture(
                new TransactionResult(TRANSACTION, resultState, failureCode)
            );
        }

        @Override
        public CompletionStage<TransactionResult> reconcile(UUID transactionId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeWaymark implements WayfarerWaymark {
        private int refundCalls;
        private boolean failAsync;

        @Override
        public CompletionStage<BigDecimal> balance(UUID playerUuid) {
            return CompletableFuture.completedFuture(BigDecimal.ZERO);
        }

        @Override
        public CompletionStage<OperationResult> debit(
            UUID playerUuid,
            long amount,
            String reference
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<OperationResult> credit(
            UUID playerUuid,
            long amount,
            String reference
        ) {
            refundCalls++;
            if (failAsync) {
                return CompletableFuture.failedFuture(
                    new IllegalStateException("sensitive refund failure")
                );
            }
            return CompletableFuture.completedFuture(new OperationResult(true, null, null));
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
            O value = asyncOperation.apply(immutableRequest);
            boolean applied = mainThreadRevalidation.test(value);
            if (applied) {
                mainThreadMutation.accept(value);
            }
            return CompletableFuture.completedFuture(new TaskBridgeResult<>(value, applied));
        }

        @Override
        public CompletionStage<Void> mainThread(Runnable operation) {
            operation.run();
            return CompletableFuture.completedFuture(null);
        }
    }
}
