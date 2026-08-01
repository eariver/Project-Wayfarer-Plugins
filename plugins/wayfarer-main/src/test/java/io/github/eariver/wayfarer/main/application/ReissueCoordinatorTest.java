package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.main.domain.EvolutionPlan;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.domain.ReissueOperation;
import io.github.eariver.wayfarer.main.domain.ReissuePricing;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

final class ReissueCoordinatorTest {
    private static final UUID PLAYER = UUID.fromString(
        "00000000-0000-0000-0000-000000000101"
    );
    private static final UUID TOOL = UUID.fromString(
        "00000000-0000-0000-0000-000000000102"
    );
    private static final UUID OLD_INSTANCE = UUID.fromString(
        "00000000-0000-0000-0000-000000000103"
    );
    private static final UUID TRANSACTION = UUID.fromString(
        "00000000-0000-0000-0000-000000000104"
    );
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void committedPaymentRotatesAuthorityOnceAndDelivers() {
        FakeRepository operations = new FakeRepository();
        FakeGrowthTools growth = new FakeGrowthTools(tool());
        FakeTransactions transactions = new FakeTransactions(
            new WayfarerTransactions.TransactionResult(
                TRANSACTION,
                WayfarerTransactions.State.COMMITTED,
                null
            )
        );
        ReissueCoordinator coordinator = coordinator(
            operations,
            growth,
            transactions,
            DeliveryOutcome.DELIVERED
        );

        ReissueCoordinator.QuoteResult quote = coordinator.quote(
            new QuoteRequest(PLAYER)
        ).toCompletableFuture().join();
        assertEquals(ReissueCoordinator.QuoteStatus.ISSUED, quote.status());
        ReissueCoordinator.Result result = coordinator.confirm(
            new ConfirmRequest(PLAYER)
        ).toCompletableFuture().join();

        assertEquals(ReissueCoordinator.Status.DELIVERED, result.status());
        assertEquals(1, transactions.executeCalls);
        assertEquals(1, growth.replaceCalls);
        assertNotNull(operations.operation);
        assertEquals(ReissueOperation.State.DELIVERED, operations.operation.state());
        assertEquals(2, growth.tool.instanceEpoch());
        assertEquals(GrowthTool.Status.ACTIVE, growth.tool.status());
        assertEquals(0, growth.tool.storedDamage());
        assertEquals(GrowthTool.DeliveryStatus.DELIVERED, growth.tool.deliveryStatus());
        assertEquals(TRANSACTION, operations.operation.transactionId());
    }

    @Test
    void unknownPaymentStoresTransactionAndDoesNotDebitAgain() {
        FakeRepository operations = new FakeRepository();
        FakeGrowthTools growth = new FakeGrowthTools(tool());
        FakeTransactions transactions = new FakeTransactions(
            new WayfarerTransactions.TransactionResult(
                TRANSACTION,
                WayfarerTransactions.State.UNKNOWN,
                null
            )
        );
        ReissueCoordinator coordinator = coordinator(
            operations,
            growth,
            transactions,
            DeliveryOutcome.DELIVERED
        );

        coordinator.quote(new QuoteRequest(PLAYER)).toCompletableFuture().join();
        ReissueCoordinator.Result result = coordinator.confirm(
            new ConfirmRequest(PLAYER)
        ).toCompletableFuture().join();

        assertEquals(ReissueCoordinator.Status.UNKNOWN, result.status());
        assertEquals(TRANSACTION, operations.operation.transactionId());
        assertEquals(ReissueOperation.State.UNKNOWN, operations.operation.state());
        assertEquals(0, growth.replaceCalls);
        assertEquals(1, transactions.executeCalls);
        assertEquals(
            ReissueCoordinator.QuoteStatus.REJECTED,
            coordinator.quote(new QuoteRequest(PLAYER)).toCompletableFuture().join().status()
        );
        assertEquals(1, transactions.executeCalls);
    }

    @Test
    void confirmPaymentAndResumeRotationInspectsCommittedCoreWithoutExecute() {
        FakeRepository operations = new FakeRepository();
        ReissueOperation unknown = operation(
            ReissueOperation.State.UNKNOWN,
            TRANSACTION,
            null,
            null,
            0
        );
        operations.operation = unknown;
        FakeGrowthTools growth = new FakeGrowthTools(tool());
        FakeTransactions transactions = new FakeTransactions(
            new WayfarerTransactions.TransactionResult(
                TRANSACTION,
                WayfarerTransactions.State.UNKNOWN,
                null
            )
        );
        transactions.inspectState = WayfarerTransactions.State.COMMITTED;
        ReissueCoordinator coordinator = coordinator(
            operations,
            growth,
            transactions,
            DeliveryOutcome.INVENTORY_FULL
        );

        ReissueCoordinator.Result result = coordinator.confirmPaymentAndResumeRotation(
            unknown.reissueId()
        ).toCompletableFuture().join();

        assertEquals(ReissueCoordinator.Status.PENDING, result.status());
        assertEquals(0, transactions.executeCalls);
        assertEquals(1, transactions.inspectCalls);
        assertEquals(1, growth.replaceCalls);
        assertEquals(ReissueOperation.State.PENDING_DELIVERY, operations.operation.state());
        assertNotNull(operations.operation.paymentCommittedAt());
    }

    @Test
    void failedCoreConfirmationLeavesModuleRowAndAuthorityUnchanged() {
        FakeRepository operations = new FakeRepository();
        ReissueOperation unknown = operation(
            ReissueOperation.State.UNKNOWN,
            TRANSACTION,
            null,
            "PAYMENT_UNKNOWN",
            0
        );
        operations.operation = unknown;
        FakeGrowthTools growth = new FakeGrowthTools(tool());
        FakeTransactions transactions = new FakeTransactions(
            new WayfarerTransactions.TransactionResult(
                TRANSACTION,
                WayfarerTransactions.State.UNKNOWN,
                null
            )
        );
        transactions.inspectState = WayfarerTransactions.State.FAILED;
        ReissueCoordinator coordinator = coordinator(
            operations,
            growth,
            transactions,
            DeliveryOutcome.DELIVERED
        );

        ReissueCoordinator.Result result = coordinator.confirmPaymentAndResumeRotation(
            unknown.reissueId()
        ).toCompletableFuture().join();

        assertEquals(ReissueCoordinator.Status.UNKNOWN, result.status());
        assertEquals("CORE_PAYMENT_FAILED", result.failureCode());
        assertEquals(0, transactions.executeCalls);
        assertEquals(0, growth.replaceCalls);
        assertEquals(unknown, operations.operation);
    }

    @Test
    void preparedRecoveryAbandonsBeforeDebitAndReleasesGuard() {
        FakeRepository operations = new FakeRepository();
        operations.operation = operation(
            ReissueOperation.State.PREPARED,
            null,
            null,
            null,
            0
        );
        FakeTransactions transactions = new FakeTransactions(
            new WayfarerTransactions.TransactionResult(
                TRANSACTION,
                WayfarerTransactions.State.COMMITTED,
                null
            )
        );
        ReissueCoordinator coordinator = coordinator(
            operations,
            new FakeGrowthTools(tool()),
            transactions,
            DeliveryOutcome.DELIVERED
        );

        assertEquals(1, coordinator.recoverAfterRestart().toCompletableFuture().join());
        assertEquals(ReissueOperation.State.ABANDONED, operations.operation.state());
        assertFalse(operations.active());
        assertEquals(0, transactions.executeCalls);
    }

    @Test
    void alreadyRotatedResumeDoesNotGenerateAnotherUuidOrCallRotation() {
        UUID newInstance = UUID.randomUUID();
        FakeRepository operations = new FakeRepository();
        operations.operation = operation(
            ReissueOperation.State.PAYMENT_COMMITTED,
            TRANSACTION,
            NOW,
            null,
            0,
            newInstance
        );
        GrowthTool rotated = tool().reissued(newInstance, NOW);
        FakeGrowthTools growth = new FakeGrowthTools(rotated);
        ReissueCoordinator coordinator = coordinator(
            operations,
            growth,
            new FakeTransactions(
                new WayfarerTransactions.TransactionResult(
                    TRANSACTION,
                    WayfarerTransactions.State.COMMITTED,
                    null
                )
            ),
            DeliveryOutcome.DELIVERED
        );

        ReissueCoordinator.Result result = coordinator.resumeRotation(
            operations.operation.reissueId()
        ).toCompletableFuture().join();

        assertEquals(ReissueCoordinator.Status.DELIVERED, result.status());
        assertEquals(0, growth.replaceCalls);
        assertEquals(newInstance, growth.tool.itemInstanceId());
    }

    private static ReissueCoordinator coordinator(
        FakeRepository operations,
        FakeGrowthTools growth,
        FakeTransactions transactions,
        DeliveryOutcome deliveryOutcome
    ) {
        return new ReissueCoordinator(
            operations,
            growth,
            transactions,
            new DirectTasks(),
            new FakeAudit(),
            new ReissuePricing(ReissuePricingDefaults.repairPricing()),
            EvolutionPlan.defaults(),
            "main-test",
            new ReissueQuoteStore(),
            player -> new ReissueEligibilitySnapshot(
                player,
                true,
                "resource",
                true,
                new PhysicalItemPresence(false, false, false, false)
            ),
            ignored -> deliveryOutcome,
            "test-server",
            CLOCK
        );
    }

    private static GrowthTool tool() {
        return new GrowthTool(
            TOOL,
            OLD_INSTANCE,
            PLAYER,
            1,
            10,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.BROKEN,
            GrowthTool.DeliveryStatus.DELIVERED,
            20,
            1,
            1,
            0,
            NOW
        );
    }

    private static ReissueOperation operation(
        ReissueOperation.State state,
        UUID transactionId,
        Instant paymentCommittedAt,
        String failureCode,
        long lockVersion
    ) {
        return operation(
            state,
            transactionId,
            paymentCommittedAt,
            failureCode,
            lockVersion,
            UUID.randomUUID()
        );
    }

    private static ReissueOperation operation(
        ReissueOperation.State state,
        UUID transactionId,
        Instant paymentCommittedAt,
        String failureCode,
        long lockVersion,
        UUID newInstance
    ) {
        return new ReissueOperation(
            UUID.randomUUID(),
            "main-reissue:quote-test",
            PLAYER,
            TOOL,
            OLD_INSTANCE,
            newInstance,
            1,
            0,
            "main-test",
            300,
            state,
            transactionId,
            paymentCommittedAt,
            failureCode,
            lockVersion
        );
    }

    private static final class ReissuePricingDefaults {
        private static io.github.eariver.wayfarer.main.domain.RepairPricing repairPricing() {
            return io.github.eariver.wayfarer.main.domain.RepairPricing.defaults();
        }
    }

    private static final class FakeRepository implements ReissueOperationRepository {
        private ReissueOperation operation;

        private boolean active() {
            return operation != null && operation.activeGuardRequired();
        }

        @Override
        public Optional<ReissueOperation> find(UUID reissueId) {
            return operation != null && operation.reissueId().equals(reissueId)
                ? Optional.of(operation)
                : Optional.empty();
        }

        @Override
        public Optional<ReissueOperation> findByIdempotency(String idempotencyKey) {
            return operation != null && operation.idempotencyKey().equals(idempotencyKey)
                ? Optional.of(operation)
                : Optional.empty();
        }

        @Override
        public Optional<ReissueOperation> findActiveByTool(UUID toolId) {
            return active() && operation.toolId().equals(toolId)
                ? Optional.of(operation)
                : Optional.empty();
        }

        @Override
        public PrepareOutcome prepare(ReissueOperation next, Instant now) {
            Optional<ReissueOperation> same = findByIdempotency(next.idempotencyKey());
            if (same.isPresent()) {
                return new PrepareOutcome(PrepareResult.EXISTING, same.orElseThrow());
            }
            if (findActiveByTool(next.toolId()).isPresent()) {
                return new PrepareOutcome(PrepareResult.IN_FLIGHT, operation);
            }
            operation = next;
            return new PrepareOutcome(PrepareResult.CREATED, operation);
        }

        @Override
        public Optional<ReissueOperation> claimPayment(UUID id, long lock, Instant now) {
            if (!matches(id, lock, ReissueOperation.State.PREPARED)) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.PAYMENT_PENDING,
                operation.transactionId(),
                operation.paymentCommittedAt(),
                operation.failureCode(),
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> paymentCommitted(
            UUID id,
            UUID transactionId,
            long lock,
            Instant now
        ) {
            if (!matches(id, lock, ReissueOperation.State.PAYMENT_PENDING)
                || (operation.transactionId() != null
                    && !operation.transactionId().equals(transactionId))) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.PAYMENT_COMMITTED,
                transactionId,
                now,
                null,
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> unknown(
            UUID id,
            long lock,
            UUID transactionId,
            String failureCode,
            Instant now
        ) {
            if (operation == null || !operation.reissueId().equals(id)
                || operation.lockVersion() != lock
                || switch (operation.state()) {
                    case PENDING_DELIVERY, DELIVERED, FAILED, ABANDONED -> true;
                    default -> false;
                }
                || (transactionId != null && operation.transactionId() != null
                    && !transactionId.equals(operation.transactionId()))) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.UNKNOWN,
                transactionId == null ? operation.transactionId() : transactionId,
                operation.paymentCommittedAt(),
                failureCode,
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> failed(
            UUID id,
            UUID transactionId,
            long lock,
            String failureCode,
            Instant now
        ) {
            if (!matches(id, lock, ReissueOperation.State.PAYMENT_PENDING)
                || (transactionId != null && operation.transactionId() != null
                    && !transactionId.equals(operation.transactionId()))) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.FAILED,
                transactionId == null ? operation.transactionId() : transactionId,
                null,
                failureCode,
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> abandoned(UUID id, long lock, Instant now) {
            if (!matches(id, lock, ReissueOperation.State.PREPARED)) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.ABANDONED,
                null,
                null,
                operation.failureCode(),
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> pendingDelivery(UUID id, long lock, Instant now) {
            if (!matches(id, lock, ReissueOperation.State.PAYMENT_COMMITTED)) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.PENDING_DELIVERY,
                operation.transactionId(),
                operation.paymentCommittedAt(),
                operation.failureCode(),
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public boolean delivered(UUID id, long lock, Instant now) {
            if (!matches(id, lock, ReissueOperation.State.PENDING_DELIVERY)) {
                return false;
            }
            operation = copy(
                ReissueOperation.State.DELIVERED,
                operation.transactionId(),
                operation.paymentCommittedAt(),
                operation.failureCode(),
                lock + 1
            );
            return true;
        }

        @Override
        public Optional<ReissueOperation> reopenPayment(UUID id, long lock, Instant now) {
            if (!matches(id, lock, ReissueOperation.State.UNKNOWN)
                || operation.transactionId() != null
                || operation.paymentCommittedAt() != null) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.PAYMENT_PENDING,
                null,
                null,
                operation.failureCode(),
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> reopenToPaymentCommitted(
            UUID id,
            long lock,
            Instant now
        ) {
            if (!matches(id, lock, ReissueOperation.State.UNKNOWN)
                || operation.paymentCommittedAt() == null) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.PAYMENT_COMMITTED,
                operation.transactionId(),
                operation.paymentCommittedAt(),
                operation.failureCode(),
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> confirmPaymentCommittedFromUnknown(
            UUID id,
            UUID expectedTransactionId,
            long lock,
            Instant committedAt,
            Instant now
        ) {
            if (!matches(id, lock, ReissueOperation.State.UNKNOWN)
                || !expectedTransactionId.equals(operation.transactionId())
                || operation.paymentCommittedAt() != null) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.PAYMENT_COMMITTED,
                operation.transactionId(),
                committedAt,
                null,
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public Optional<ReissueOperation> failFromUnknown(
            UUID id,
            long lock,
            String failureCode,
            Instant now
        ) {
            if (!matches(id, lock, ReissueOperation.State.UNKNOWN)
                || operation.paymentCommittedAt() != null) {
                return Optional.empty();
            }
            operation = copy(
                ReissueOperation.State.FAILED,
                operation.transactionId(),
                null,
                failureCode,
                lock + 1
            );
            return Optional.of(operation);
        }

        @Override
        public List<ReissueOperation> findRecoveryCandidates() {
            return operation == null ? List.of() : switch (operation.state()) {
                case PREPARED, PAYMENT_PENDING, PAYMENT_COMMITTED, PENDING_DELIVERY ->
                    List.of(operation);
                default -> List.of();
            };
        }

        private boolean matches(UUID id, long lock, ReissueOperation.State state) {
            return operation != null && operation.reissueId().equals(id)
                && operation.lockVersion() == lock && operation.state() == state;
        }

        private ReissueOperation copy(
            ReissueOperation.State state,
            UUID transactionId,
            Instant paymentCommittedAt,
            String failureCode,
            long lock
        ) {
            return new ReissueOperation(
                operation.reissueId(),
                operation.idempotencyKey(),
                operation.playerUuid(),
                operation.toolId(),
                operation.expectedItemInstanceId(),
                operation.newItemInstanceId(),
                operation.instanceEpoch(),
                operation.evolutionCount(),
                operation.configRevision(),
                operation.amountWaymark(),
                state,
                transactionId,
                paymentCommittedAt,
                failureCode,
                lock
            );
        }
    }

    private static final class FakeGrowthTools implements GrowthToolRepository {
        private GrowthTool tool;
        private int replaceCalls;

        private FakeGrowthTools(GrowthTool tool) {
            this.tool = tool;
        }

        @Override
        public GrowthTool findOrCreate(UUID ownerUuid, Instant now) {
            return tool;
        }

        @Override
        public Optional<GrowthTool> findByOwner(UUID ownerUuid) {
            return tool.ownerUuid().equals(ownerUuid) ? Optional.of(tool) : Optional.empty();
        }

        @Override
        public boolean markDelivered(UUID toolId, long lock, Instant now) {
            if (!tool.toolId().equals(toolId)
                || tool.lockVersion() != lock
                || tool.deliveryStatus() != GrowthTool.DeliveryStatus.PENDING) {
                return false;
            }
            tool = new GrowthTool(
                tool.toolId(),
                tool.itemInstanceId(),
                tool.ownerUuid(),
                tool.instanceEpoch(),
                tool.cumulativeProgressUnits(),
                tool.branch(),
                tool.status(),
                GrowthTool.DeliveryStatus.DELIVERED,
                tool.storedDamage(),
                tool.schemaVersion(),
                tool.displayRevision(),
                lock + 1,
                now
            );
            return true;
        }

        @Override
        public Optional<GrowthTool> checkpoint(
            GrowthTool submitted,
            long lock,
            Instant now
        ) {
            return Optional.empty();
        }

        @Override
        public Optional<GrowthTool> replaceAuthority(
            GrowthTool submitted,
            long lock,
            Instant now
        ) {
            if (!tool.toolId().equals(submitted.toolId()) || tool.lockVersion() != lock) {
                return Optional.empty();
            }
            replaceCalls++;
            tool = new GrowthTool(
                submitted.toolId(),
                submitted.itemInstanceId(),
                submitted.ownerUuid(),
                submitted.instanceEpoch(),
                submitted.cumulativeProgressUnits(),
                submitted.branch(),
                submitted.status(),
                submitted.deliveryStatus(),
                submitted.storedDamage(),
                submitted.schemaVersion(),
                submitted.displayRevision(),
                lock + 1,
                now
            );
            return Optional.of(tool);
        }
    }

    private static final class FakeTransactions implements WayfarerTransactions {
        private final TransactionResult result;
        private int executeCalls;
        private int inspectCalls;
        private State inspectState = State.UNKNOWN;

        private FakeTransactions(TransactionResult result) {
            this.result = result;
        }

        @Override
        public CompletionStage<TransactionResult> execute(TransactionRequest request) {
            executeCalls++;
            return CompletableFuture.completedFuture(result);
        }

        @Override
        public CompletionStage<TransactionResult> reconcile(UUID transactionId) {
            return CompletableFuture.completedFuture(result);
        }

        @Override
        public CompletionStage<TransactionDetails> inspect(UUID transactionId) {
            inspectCalls++;
            return CompletableFuture.completedFuture(new TransactionDetails(
                transactionId,
                "main-reissue:main-reissue:quote-test",
                "MAIN_TOOL_REISSUE",
                PLAYER,
                "GROWTH_TOOL",
                TOOL.toString(),
                300,
                inspectState,
                null,
                null,
                null,
                null,
                null,
                0
            ));
        }

    }

    private static final class FakeAudit implements WayfarerAudit {
        @Override
        public CompletionStage<Void> record(AuditEvent event) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class DirectTasks implements WayfarerTasks {
        @Override
        public <T> CompletionStage<T> database(Supplier<T> operation) {
            try {
                return CompletableFuture.completedFuture(operation.get());
            } catch (RuntimeException failure) {
                return CompletableFuture.failedFuture(failure);
            }
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
            try {
                operation.run();
                return CompletableFuture.completedFuture(null);
            } catch (RuntimeException failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }
    }
}
