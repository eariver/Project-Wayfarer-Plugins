package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionEngineTest {
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-07-28T00:00:00Z"),
        ZoneOffset.UTC
    );

    @Test
    void stateMachineCoversRequiredStatesAndRejectsIllegalTransition() {
        TransactionStateMachine.requireAllowed(
            WayfarerTransactions.State.PREPARED,
            WayfarerTransactions.State.DEBIT_PENDING
        );
        TransactionStateMachine.requireAllowed(
            WayfarerTransactions.State.UNKNOWN,
            WayfarerTransactions.State.REFUND_PENDING
        );
        TransactionStateMachine.requireAllowed(
            WayfarerTransactions.State.REFUND_PENDING,
            WayfarerTransactions.State.RECONCILED_REFUNDED
        );
        assertThrows(
            TransactionException.class,
            () -> TransactionStateMachine.requireAllowed(
                WayfarerTransactions.State.COMMITTED,
                WayfarerTransactions.State.DEBIT_PENDING
            )
        );
        assertEquals(11, WayfarerTransactions.State.values().length);
    }

    @Test
    void successfulFlowCommitsAndIdempotencyPreventsDuplicateDebit() {
        Fixture fixture = fixture(Mode.SUCCESS);
        WayfarerTransactions.TransactionRequest request = request("same-key");

        WayfarerTransactions.TransactionResult first = fixture.engine.execute(request)
            .toCompletableFuture().join();
        WayfarerTransactions.TransactionResult duplicate = fixture.engine.execute(request)
            .toCompletableFuture().join();

        assertEquals(WayfarerTransactions.State.COMMITTED, first.state());
        assertEquals(first, duplicate);
        assertEquals(1, fixture.provider.debits.get());
        assertTrue(fixture.auditTypes.containsAll(List.of(
            "DEBIT_REQUESTED",
            "DEBIT_SUCCEEDED",
            "TRANSACTION_COMMITTED"
        )));
    }

    @Test
    void concurrentIdempotentRequestsProduceOneDebit() {
        Fixture fixture = fixture(Mode.SUCCESS);
        WayfarerTransactions.TransactionRequest request = request("concurrent");
        List<CompletableFuture<WayfarerTransactions.TransactionResult>> calls =
            java.util.stream.IntStream.range(0, 24)
                .mapToObj(ignored -> CompletableFuture.supplyAsync(
                    () -> fixture.engine.execute(request).toCompletableFuture().join()
                ))
                .toList();

        CompletableFuture.allOf(calls.toArray(CompletableFuture[]::new)).join();

        assertEquals(1, fixture.provider.debits.get());
        assertEquals(
            1,
            calls.stream().map(CompletableFuture::join)
                .map(WayfarerTransactions.TransactionResult::transactionId)
                .distinct()
                .count()
        );
    }

    @Test
    void insufficientFundsFailsWithoutRetryingEffect() {
        Fixture fixture = fixture(Mode.INSUFFICIENT);
        WayfarerTransactions.TransactionResult result = fixture.engine.execute(request("funds"))
            .toCompletableFuture().join();

        assertEquals(WayfarerTransactions.State.FAILED, result.state());
        assertEquals("INSUFFICIENT_FUNDS", result.failureCode());
        assertEquals(1, fixture.provider.debits.get());
    }

    @Test
    void unknownEffectIsNotRetriedAndAutomaticReconcileCanCommit() {
        Fixture fixture = fixture(Mode.UNKNOWN_APPLIED);
        WayfarerTransactions.TransactionResult uncertain = fixture.engine
            .execute(request("unknown"))
            .toCompletableFuture().join();
        assertEquals(WayfarerTransactions.State.UNKNOWN, uncertain.state());
        assertEquals(
            "debit-ref",
            fixture.engine.inspect(uncertain.transactionId()).toCompletableFuture().join()
                .providerReference()
        );

        WayfarerTransactions.TransactionResult reconciled = fixture.engine
            .reconcile(uncertain.transactionId())
            .toCompletableFuture().join();
        assertEquals(WayfarerTransactions.State.RECONCILED_COMMITTED, reconciled.state());
        assertEquals(1, fixture.provider.debits.get());
    }

    @Test
    void providerTimeoutBecomesUnknownWithoutExactlyOnceClaim() {
        Fixture fixture = fixture(Mode.TIMEOUT);
        WayfarerTransactions.TransactionResult result = fixture.engine.execute(request("timeout"))
            .toCompletableFuture().join();

        assertEquals(WayfarerTransactions.State.UNKNOWN, result.state());
        assertEquals("PROVIDER_RESULT_UNKNOWN", result.failureCode());
        assertEquals(1, fixture.provider.debits.get());
    }

    @Test
    void manualRefundIsClaimedOnceAcrossDuplicateReconcile() {
        Fixture fixture = fixture(Mode.UNKNOWN_APPLIED);
        UUID id = fixture.engine.execute(request("refund")).toCompletableFuture().join()
            .transactionId();

        WayfarerTransactions.TransactionResult first = fixture.engine.reconcile(
            id,
            WayfarerTransactions.ReconcileAction.REFUND
        ).toCompletableFuture().join();
        WayfarerTransactions.TransactionResult duplicate = fixture.engine.reconcile(
            id,
            WayfarerTransactions.ReconcileAction.REFUND
        ).toCompletableFuture().join();

        assertEquals(WayfarerTransactions.State.RECONCILED_REFUNDED, first.state());
        assertEquals(first, duplicate);
        assertEquals(1, fixture.provider.refunds.get());
    }

    @Test
    void idempotencyKeyCannotBeReusedForDifferentInput() {
        Fixture fixture = fixture(Mode.SUCCESS);
        fixture.engine.execute(request("reuse")).toCompletableFuture().join();
        WayfarerTransactions.TransactionRequest changed = new WayfarerTransactions.TransactionRequest(
            "reuse",
            "SHOP",
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "WAYMARK",
            "other",
            15,
            null
        );
        assertThrows(
            TransactionException.class,
            () -> fixture.engine.execute(changed).toCompletableFuture().join()
        );
    }

    @Test
    void inspectReturnsRedactedStructuredFieldsAndAuditsAccess() {
        Fixture fixture = fixture(Mode.SUCCESS);
        UUID id = fixture.engine.execute(request("inspect")).toCompletableFuture().join()
            .transactionId();

        WayfarerTransactions.TransactionDetails details = fixture.engine.inspect(id)
            .toCompletableFuture().join();

        assertEquals(id, details.transactionId());
        assertEquals("inspect", details.idempotencyKey());
        assertEquals(WayfarerTransactions.State.COMMITTED, details.state());
        assertTrue(fixture.auditTypes.contains("ADMIN_TRANSACTION_INSPECTED"));
    }

    private static Fixture fixture(Mode mode) {
        InMemoryRepository repository = new InMemoryRepository();
        FixtureProvider provider = new FixtureProvider(mode);
        List<String> auditTypes = new ArrayList<>();
        WayfarerAudit audit = event -> {
            auditTypes.add(event.eventType());
            return CompletableFuture.completedFuture(null);
        };
        return new Fixture(
            new TransactionEngine(
                repository,
                provider,
                audit,
                "test-server",
                Duration.ofMillis(25),
                CLOCK
            ),
            provider,
            auditTypes
        );
    }

    private static WayfarerTransactions.TransactionRequest request(String key) {
        return new WayfarerTransactions.TransactionRequest(
            key,
            "SHOP",
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "WAYMARK",
            "subject",
            10,
            "{\"safe\":true}"
        );
    }

    private record Fixture(
        TransactionEngine engine,
        FixtureProvider provider,
        List<String> auditTypes
    ) {}

    private enum Mode {
        SUCCESS,
        INSUFFICIENT,
        UNKNOWN_APPLIED,
        TIMEOUT
    }

    private static final class FixtureProvider implements WayfarerWaymarkProvider {
        private final Mode mode;
        private final AtomicInteger debits = new AtomicInteger();
        private final AtomicInteger refunds = new AtomicInteger();

        private FixtureProvider(Mode mode) {
            this.mode = mode;
        }

        @Override
        public CompletionStage<ProbeResult> probe() {
            return CompletableFuture.completedFuture(new ProbeResult(true, "fixture", null));
        }

        @Override
        public CompletionStage<Long> balance(UUID playerUuid) {
            return CompletableFuture.completedFuture(100L);
        }

        @Override
        public CompletionStage<EffectResult> debit(
            UUID playerUuid,
            long amount,
            String operationId
        ) {
            debits.incrementAndGet();
            return switch (mode) {
                case SUCCESS -> CompletableFuture.completedFuture(
                    new EffectResult(EffectStatus.SUCCEEDED, "debit-ref", null)
                );
                case INSUFFICIENT -> CompletableFuture.completedFuture(
                    new EffectResult(EffectStatus.INSUFFICIENT_FUNDS, null, "NO_FUNDS")
                );
                case UNKNOWN_APPLIED -> CompletableFuture.completedFuture(
                    new EffectResult(EffectStatus.UNKNOWN, "debit-ref", "UNCERTAIN")
                );
                case TIMEOUT -> new CompletableFuture<>();
            };
        }

        @Override
        public CompletionStage<EffectResult> refund(
            UUID playerUuid,
            long amount,
            String operationId,
            String debitProviderReference
        ) {
            refunds.incrementAndGet();
            return CompletableFuture.completedFuture(
                new EffectResult(EffectStatus.SUCCEEDED, "refund-ref", null)
            );
        }

        @Override
        public CompletionStage<EffectResolution> resolve(
            String operationId,
            String providerReference
        ) {
            return CompletableFuture.completedFuture(
                mode == Mode.UNKNOWN_APPLIED
                    ? EffectResolution.APPLIED
                    : EffectResolution.UNKNOWN
            );
        }
    }

    private static final class InMemoryRepository implements TransactionRepository {
        private final Map<UUID, TransactionRecord> records = new HashMap<>();
        private final Map<String, UUID> keys = new HashMap<>();

        @Override
        public synchronized CompletionStage<TransactionRecord> prepare(
            UUID transactionId,
            WayfarerTransactions.TransactionRequest request,
            String providerOperationId,
            Instant now
        ) {
            UUID existingId = keys.get(request.idempotencyKey());
            if (existingId != null) {
                TransactionRecord existing = records.get(existingId);
                if (!existing.transactionType().equals(request.transactionType())
                    || !existing.actorUuid().equals(request.actorUuid())
                    || !existing.subjectId().equals(request.subjectId())
                    || existing.amountWaymark() != request.amountWaymark()) {
                    throw new TransactionException(
                        "Idempotency key was reused with different input"
                    );
                }
                return CompletableFuture.completedFuture(existing);
            }
            TransactionRecord record = new TransactionRecord(
                transactionId,
                request.idempotencyKey(),
                request.transactionType(),
                request.actorUuid(),
                request.subjectType(),
                request.subjectId(),
                request.amountWaymark(),
                WayfarerTransactions.State.PREPARED,
                null,
                providerOperationId,
                null,
                0
            );
            records.put(transactionId, record);
            keys.put(request.idempotencyKey(), transactionId);
            return CompletableFuture.completedFuture(record);
        }

        @Override
        public synchronized CompletionStage<Optional<TransactionRecord>> find(
            UUID transactionId
        ) {
            return CompletableFuture.completedFuture(
                Optional.ofNullable(records.get(transactionId))
            );
        }

        @Override
        public synchronized CompletionStage<Optional<TransactionRecord>> transition(
            TransactionRecord current,
            WayfarerTransactions.State next,
            String providerReference,
            String failureCode,
            Instant now
        ) {
            TransactionStateMachine.requireAllowed(current.state(), next);
            TransactionRecord stored = records.get(current.transactionId());
            if (stored == null || stored.state() != current.state()
                || stored.lockVersion() != current.lockVersion()) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            TransactionRecord updated = new TransactionRecord(
                stored.transactionId(),
                stored.idempotencyKey(),
                stored.transactionType(),
                stored.actorUuid(),
                stored.subjectType(),
                stored.subjectId(),
                stored.amountWaymark(),
                next,
                providerReference == null ? stored.providerReference() : providerReference,
                stored.providerOperationId(),
                failureCode,
                stored.lockVersion() + 1
            );
            records.put(updated.transactionId(), updated);
            return CompletableFuture.completedFuture(Optional.of(updated));
        }
    }
}
