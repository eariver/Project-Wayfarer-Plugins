package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerTransactions.ReconcileAction;
import io.github.eariver.wayfarer.api.WayfarerTransactions.State;
import io.github.eariver.wayfarer.api.WayfarerTransactions.TransactionDetails;
import io.github.eariver.wayfarer.api.WayfarerTransactions.TransactionRequest;
import io.github.eariver.wayfarer.api.WayfarerTransactions.TransactionResult;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectKind;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectResult;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectStatus;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.ProbeResult;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.ResolutionResult;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.ResolutionStatus;
import io.github.eariver.wayfarer.core.transaction.TransactionEngine.FailureInjector;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionEngineTest {
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-07-28T00:00:00Z"),
        ZoneOffset.UTC
    );

    @Test
    void stateMachineCoversRequiredStatesAndRejectsUnsafeRefundFailure() {
        TransactionStateMachine.requireAllowed(State.PREPARED, State.DEBIT_PENDING);
        TransactionStateMachine.requireAllowed(State.UNKNOWN, State.UNKNOWN);
        TransactionStateMachine.requireAllowed(
            State.REFUND_PENDING,
            State.RECONCILED_REFUNDED
        );
        assertThrows(
            TransactionException.class,
            () -> TransactionStateMachine.requireAllowed(
                State.REFUND_PENDING,
                State.FAILED
            )
        );
        assertEquals(11, State.values().length);
    }

    @Test
    void exactDuplicateIncludingPayloadReturnsSameTransactionAndOneDebit() {
        Fixture fixture = fixture(Mode.SUCCESS);
        TransactionRequest request = request("same-key");

        TransactionResult first = fixture.engine.execute(request)
            .toCompletableFuture().join();
        TransactionResult duplicate = fixture.engine.execute(request)
            .toCompletableFuture().join();

        assertEquals(State.COMMITTED, first.state());
        assertEquals(first, duplicate);
        assertEquals(1, fixture.provider.debitCalls.get());
        assertEquals(1, fixture.provider.debitEffects.get());
    }

    @Test
    void concurrentExactDuplicatesProduceOneDebit() {
        Fixture fixture = fixture(Mode.SUCCESS);
        TransactionRequest request = request("concurrent");
        List<CompletableFuture<TransactionResult>> calls =
            java.util.stream.IntStream.range(0, 24)
                .mapToObj(ignored -> CompletableFuture.supplyAsync(
                    () -> fixture.engine.execute(request).toCompletableFuture().join()
                ))
                .toList();

        CompletableFuture.allOf(calls.toArray(CompletableFuture[]::new)).join();

        assertEquals(1, fixture.provider.debitCalls.get());
        assertEquals(1, fixture.provider.debitEffects.get());
        assertEquals(
            1,
            calls.stream().map(CompletableFuture::join)
                .map(TransactionResult::transactionId)
                .distinct()
                .count()
        );
    }

    @Test
    void idempotencyRequiresExactPayloadSubjectTypeAndActorNullability() {
        assertMismatch(
            "payload",
            request("payload"),
            copy(request("payload"), null, null, "{\"safe\": false}")
        );
        assertMismatch(
            "subject-type",
            request("subject-type"),
            copy(request("subject-type"), null, "OTHER", null)
        );
        TransactionRequest nullableActor = copyWithNullableActor(
            request("nullable-actor")
        );
        TransactionRequest changedActor = new TransactionRequest(
            nullableActor.idempotencyKey(),
            nullableActor.transactionType(),
            UUID.fromString("00000000-0000-0000-0000-000000000009"),
            nullableActor.subjectType(),
            nullableActor.subjectId(),
            nullableActor.amountWaymark(),
            nullableActor.payloadJson()
        );
        assertMismatch("nullable-actor", nullableActor, changedActor);
    }

    @Test
    void restartPayloadMismatchFailsClosedBeforeAdditionalProviderCall() {
        Fixture first = fixture(Mode.SUCCESS);
        first.engine.execute(request("restart-mismatch")).toCompletableFuture().join();
        TransactionEngine restarted = engine(
            first.repository,
            first.provider,
            event -> CompletableFuture.completedFuture(null),
            ignored -> {},
            TransactionEngine.FailureInjector.none()
        );
        TransactionRequest changed = copy(
            request("restart-mismatch"),
            null,
            null,
            "{\"different\":true}"
        );

        assertThrows(TransactionException.class, () -> restarted.execute(changed));
        assertEquals(1, first.provider.debitCalls.get());
    }

    @Test
    void timeoutAfterEffectRecoversByPersistedDebitOperationWithoutSecondDebit() {
        Fixture fixture = fixture(Mode.TIMEOUT_AFTER);
        TransactionResult uncertain = fixture.engine.execute(request("timeout-after"))
            .toCompletableFuture().join();
        TransactionDetails before = fixture.engine.inspect(uncertain.transactionId())
            .toCompletableFuture().join();

        assertEquals(State.UNKNOWN, uncertain.state());
        assertNotNull(before.debitOperationId());
        assertNull(before.refundOperationId());

        TransactionResult recovered = fixture.engine.reconcile(uncertain.transactionId())
            .toCompletableFuture().join();
        TransactionDetails after = fixture.engine.inspect(uncertain.transactionId())
            .toCompletableFuture().join();

        assertEquals(State.RECONCILED_COMMITTED, recovered.state());
        assertEquals(before.debitOperationId(), after.debitOperationId());
        assertEquals("debit-ref", after.debitProviderReference());
        assertEquals(1, fixture.provider.debitCalls.get());
        assertEquals(1, fixture.provider.debitEffects.get());
    }

    @Test
    void timeoutBeforeEffectResolvesNotAppliedWithoutRetryingDebit() {
        Fixture fixture = fixture(Mode.TIMEOUT_BEFORE);
        TransactionResult uncertain = fixture.engine.execute(request("timeout-before"))
            .toCompletableFuture().join();

        TransactionResult recovered = fixture.engine.reconcile(uncertain.transactionId())
            .toCompletableFuture().join();

        assertEquals(State.FAILED, recovered.state());
        assertEquals(1, fixture.provider.debitCalls.get());
        assertEquals(0, fixture.provider.debitEffects.get());
    }

    @Test
    void insufficientFundsAndKnownFailureAreDurableKnownFailures() {
        for (Mode mode : List.of(Mode.INSUFFICIENT, Mode.KNOWN_FAILURE)) {
            Fixture fixture = fixture(mode);
            TransactionResult result = fixture.engine.execute(request("known-" + mode))
                .toCompletableFuture().join();

            assertEquals(State.FAILED, result.state(), mode.name());
            assertEquals(1, fixture.provider.debitCalls.get(), mode.name());
            assertEquals(0, fixture.provider.debitEffects.get(), mode.name());
        }
    }

    @Test
    void providerOutageAndUnknownResolutionRemainFailClosed() {
        Fixture outage = fixture(Mode.OUTAGE);
        TransactionResult unavailable = outage.engine.execute(request("outage"))
            .toCompletableFuture().join();
        assertEquals(State.UNKNOWN, unavailable.state());
        assertEquals(
            State.FAILED,
            outage.engine.reconcile(unavailable.transactionId())
                .toCompletableFuture().join().state()
        );
        assertEquals(1, outage.provider.debitCalls.get());

        Fixture unresolved = fixture(Mode.RESOLUTION_UNKNOWN);
        TransactionResult unknown = unresolved.engine.execute(request("resolution-unknown"))
            .toCompletableFuture().join();
        TransactionResult stillUnknown = unresolved.engine.reconcile(unknown.transactionId())
            .toCompletableFuture().join();
        assertEquals(State.UNKNOWN, stillUnknown.state());
        assertEquals(1, unresolved.provider.debitCalls.get());
        assertEquals(1, unresolved.provider.resolveCalls.get());
    }

    @Test
    void crashAfterPreparedCommitResumesSameTransaction() {
        Fixture fixture = fixture(
            Mode.SUCCESS,
            crashAt(TransactionEngine.FailurePoint.AFTER_PREPARED_COMMIT)
        );
        TransactionRequest request = request("crash-prepared");
        assertCrash(() -> fixture.engine.execute(request));
        TransactionRecord prepared = fixture.repository.only();

        TransactionEngine restarted = restart(fixture);
        TransactionResult result = restarted.execute(request).toCompletableFuture().join();

        assertEquals(prepared.transactionId(), result.transactionId());
        assertEquals(State.COMMITTED, result.state());
        assertEquals(1, fixture.provider.debitEffects.get());
    }

    @Test
    void crashAfterDebitPendingResolvesNotAppliedWithoutCallingDebit() {
        Fixture fixture = fixture(
            Mode.SUCCESS,
            crashAt(TransactionEngine.FailurePoint.AFTER_DEBIT_PENDING_COMMIT)
        );
        assertCrash(() -> fixture.engine.execute(request("crash-pending")));

        restart(fixture).recoverPending(100).toCompletableFuture().join();
        TransactionRecord recovered = fixture.repository.only();

        assertEquals(State.FAILED, recovered.state());
        assertEquals(0, fixture.provider.debitCalls.get());
        assertEquals(1, fixture.provider.resolveCalls.get());
    }

    @Test
    void crashAfterDebitEffectRecoversAppliedWithoutDuplicateDebit() {
        Fixture fixture = fixture(
            Mode.SUCCESS,
            crashAt(TransactionEngine.FailurePoint.AFTER_DEBIT_EFFECT)
        );
        assertCrash(() -> fixture.engine.execute(request("crash-effect")));
        String operation = fixture.repository.only().debitOperationId();

        restart(fixture).recoverPending(100).toCompletableFuture().join();
        TransactionRecord recovered = fixture.repository.only();

        assertEquals(State.COMMITTED, recovered.state());
        assertEquals(operation, recovered.debitOperationId());
        assertEquals("debit-ref", recovered.debitProviderReference());
        assertEquals(1, fixture.provider.debitCalls.get());
        assertEquals(1, fixture.provider.debitEffects.get());
    }

    @Test
    void postDebitCrashPointsRecoverWithoutProviderReexecution() {
        for (TransactionEngine.FailurePoint point : List.of(
            TransactionEngine.FailurePoint.AFTER_DEBITED_COMMIT,
            TransactionEngine.FailurePoint.AFTER_DOMAIN_COMMIT_PENDING,
            TransactionEngine.FailurePoint.AFTER_COMMITTED_COMMIT
        )) {
            Fixture fixture = fixture(Mode.SUCCESS, crashAt(point));
            assertCrash(() -> fixture.engine.execute(request("post-" + point)));

            restart(fixture).recoverPending(100).toCompletableFuture().join();

            assertEquals(State.COMMITTED, fixture.repository.only().state(), point.name());
            assertEquals(1, fixture.provider.debitCalls.get(), point.name());
            assertEquals(1, fixture.provider.debitEffects.get(), point.name());
        }
    }

    @Test
    void refundPendingCrashUsesStableOperationAndNotAppliedReturnsUnknown() {
        Fixture fixture = fixture(Mode.UNKNOWN_AFTER);
        UUID id = fixture.engine.execute(request("refund-pending")).toCompletableFuture()
            .join().transactionId();
        TransactionEngine crashing = engine(
            fixture.repository,
            fixture.provider,
            fixture.audit,
            fixture.warnings::add,
            crashAt(TransactionEngine.FailurePoint.AFTER_REFUND_PENDING_COMMIT)
        );
        assertCrash(() -> crashing.reconcile(id, ReconcileAction.REFUND));
        String operation = fixture.repository.only().refundOperationId();

        restart(fixture).recoverPending(100).toCompletableFuture().join();
        TransactionRecord recovered = fixture.repository.only();

        assertEquals(State.UNKNOWN, recovered.state());
        assertEquals(operation, recovered.refundOperationId());
        assertEquals("debit-ref", recovered.debitProviderReference());
        assertEquals(0, fixture.provider.refundCalls.get());
    }

    @Test
    void refundEffectCrashResolvesAppliedWithoutDuplicateRefund() {
        Fixture fixture = fixture(Mode.UNKNOWN_AFTER);
        UUID id = fixture.engine.execute(request("refund-effect")).toCompletableFuture()
            .join().transactionId();
        TransactionEngine crashing = engine(
            fixture.repository,
            fixture.provider,
            fixture.audit,
            fixture.warnings::add,
            crashAt(TransactionEngine.FailurePoint.AFTER_REFUND_EFFECT)
        );
        assertCrash(() -> crashing.reconcile(id, ReconcileAction.REFUND));
        TransactionRecord pending = fixture.repository.only();

        restart(fixture).recoverPending(100).toCompletableFuture().join();
        TransactionRecord recovered = fixture.repository.only();

        assertEquals(State.RECONCILED_REFUNDED, recovered.state());
        assertEquals(pending.refundOperationId(), recovered.refundOperationId());
        assertEquals("refund-ref", recovered.refundProviderReference());
        assertEquals("debit-ref", recovered.debitProviderReference());
        assertEquals(1, fixture.provider.refundCalls.get());
        assertEquals(1, fixture.provider.refundEffects.get());
    }

    @Test
    void concurrentRecoveryWorkersUseOneWinningResolutionClaim() {
        Fixture fixture = fixture(
            Mode.SUCCESS,
            crashAt(TransactionEngine.FailurePoint.AFTER_DEBIT_PENDING_COMMIT)
        );
        assertCrash(() -> fixture.engine.execute(request("recovery-race")));
        fixture.provider.resolutionGate = new CompletableFuture<>();
        TransactionEngine workerOne = restart(fixture);
        TransactionEngine workerTwo = restart(fixture);

        CompletableFuture<Integer> first = workerOne.recoverPending(100)
            .toCompletableFuture();
        CompletableFuture<Integer> second = workerTwo.recoverPending(100)
            .toCompletableFuture();

        assertEquals(1, fixture.provider.resolveCalls.get());
        assertEquals(0, second.join());
        fixture.provider.resolutionGate.complete(new ResolutionResult(
            ResolutionStatus.NOT_APPLIED,
            null,
            "NOT_APPLIED"
        ));
        assertEquals(1, first.join());
        assertEquals(State.FAILED, fixture.repository.only().state());
    }

    @Test
    void concurrentManualRefundRequestsProduceOneRefundEffect() {
        Fixture fixture = fixture(Mode.UNKNOWN_AFTER);
        UUID id = fixture.engine.execute(request("manual-race"))
            .toCompletableFuture().join().transactionId();
        List<CompletableFuture<TransactionResult>> calls =
            java.util.stream.IntStream.range(0, 12)
                .mapToObj(ignored -> CompletableFuture.supplyAsync(
                    () -> fixture.engine.reconcile(id, ReconcileAction.REFUND)
                        .toCompletableFuture().join()
                ))
                .toList();

        CompletableFuture.allOf(calls.toArray(CompletableFuture[]::new)).join();

        assertEquals(1, fixture.provider.refundCalls.get());
        assertEquals(1, fixture.provider.refundEffects.get());
        assertEquals(State.RECONCILED_REFUNDED, fixture.repository.only().state());
    }

    @Test
    void startupRecoveryAndAdminAutoReconcileShareOneResolutionClaim() {
        Fixture fixture = fixture(
            Mode.SUCCESS,
            crashAt(TransactionEngine.FailurePoint.AFTER_DEBIT_PENDING_COMMIT)
        );
        assertCrash(() -> fixture.engine.execute(request("startup-admin-race")));
        fixture.provider.resolutionGate = new CompletableFuture<>();
        TransactionEngine startup = restart(fixture);
        TransactionEngine admin = restart(fixture);

        CompletableFuture<Integer> startupResult = startup.recoverPending(100)
            .toCompletableFuture();
        CompletableFuture<TransactionResult> adminResult = admin.reconcile(
            fixture.repository.only().transactionId()
        ).toCompletableFuture();

        assertEquals(1, fixture.provider.resolveCalls.get());
        assertEquals(State.DEBIT_PENDING, adminResult.join().state());
        fixture.provider.resolutionGate.complete(new ResolutionResult(
            ResolutionStatus.NOT_APPLIED,
            null,
            "NOT_APPLIED"
        ));
        assertEquals(1, startupResult.join());
        assertEquals(State.FAILED, fixture.repository.only().state());
    }

    @Test
    void durableHistoryProgressesWhenEveryTransitionMirrorFails() {
        Predicate<String> failMirrors = eventType ->
            !"RECONCILE_REQUESTED".equals(eventType)
                && !"ADMIN_TRANSACTION_INSPECTED".equals(eventType);
        Fixture committed = fixture(Mode.SUCCESS, FailureInjector.none(), failMirrors);
        TransactionResult result = committed.engine.execute(request("audit-commit"))
            .toCompletableFuture().join();
        assertEquals(State.COMMITTED, result.state());
        assertEquals(
            List.of(
                State.PREPARED,
                State.DEBIT_PENDING,
                State.DEBITED,
                State.DOMAIN_COMMIT_PENDING,
                State.COMMITTED
            ),
            committed.repository.historyStates()
        );

        Fixture refunded = fixture(Mode.UNKNOWN_AFTER, FailureInjector.none(), failMirrors);
        UUID refundId = refunded.engine.execute(request("audit-refund"))
            .toCompletableFuture().join().transactionId();
        TransactionResult refund = refunded.engine.reconcile(
            refundId,
            ReconcileAction.REFUND
        ).toCompletableFuture().join();
        assertEquals(State.RECONCILED_REFUNDED, refund.state());
        assertTrue(refunded.repository.historyStates().contains(State.UNKNOWN));
        assertTrue(refunded.repository.historyStates().contains(State.REFUND_PENDING));
        assertTrue(refunded.repository.historyStates().contains(State.RECONCILED_REFUNDED));
        assertEquals(1, refunded.provider.debitEffects.get());
        assertEquals(1, refunded.provider.refundEffects.get());

        assertTrue(committed.warnings.size() >= 5);
        assertTrue(refunded.warnings.size() >= 4);
        assertTrue(
            java.util.stream.Stream.concat(
                committed.warnings.stream(),
                refunded.warnings.stream()
            ).allMatch("Wayfarer transaction audit mirror failed"::equals)
        );
    }

    @Test
    void inspectSeparatesDebitAndRefundEffectIdentityWithoutPayload() {
        Fixture fixture = fixture(Mode.UNKNOWN_AFTER);
        UUID id = fixture.engine.execute(request("inspect")).toCompletableFuture()
            .join().transactionId();
        fixture.engine.reconcile(id, ReconcileAction.REFUND).toCompletableFuture().join();

        TransactionDetails details = fixture.engine.inspect(id)
            .toCompletableFuture().join();

        assertNotNull(details.debitOperationId());
        assertEquals("debit-ref", details.debitProviderReference());
        assertNotNull(details.refundOperationId());
        assertEquals("refund-ref", details.refundProviderReference());
    }

    private static void assertMismatch(
        String key,
        TransactionRequest first,
        TransactionRequest changed
    ) {
        Fixture fixture = fixture(Mode.SUCCESS);
        fixture.engine.execute(first).toCompletableFuture().join();
        assertThrows(TransactionException.class, () -> fixture.engine.execute(changed), key);
        assertEquals(1, fixture.provider.debitCalls.get(), key);
    }

    private static TransactionRequest copy(
        TransactionRequest source,
        UUID actorOverride,
        String subjectTypeOverride,
        String payloadOverride
    ) {
        return new TransactionRequest(
            source.idempotencyKey(),
            source.transactionType(),
            actorOverride == null ? source.actorUuid() : actorOverride,
            subjectTypeOverride == null ? source.subjectType() : subjectTypeOverride,
            source.subjectId(),
            source.amountWaymark(),
            payloadOverride == null ? source.payloadJson() : payloadOverride
        );
    }

    private static Fixture fixture(Mode mode) {
        return fixture(mode, FailureInjector.none());
    }

    private static Fixture fixture(Mode mode, FailureInjector injector) {
        return fixture(mode, injector, ignored -> false);
    }

    private static Fixture fixture(
        Mode mode,
        FailureInjector injector,
        Predicate<String> failAudit
    ) {
        InMemoryRepository repository = new InMemoryRepository();
        FixtureProvider provider = new FixtureProvider(mode);
        List<String> auditTypes = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        WayfarerAudit audit = event -> {
            auditTypes.add(event.eventType());
            return failAudit.test(event.eventType())
                ? CompletableFuture.failedFuture(
                    new IllegalStateException("sanitized audit fixture failure")
                )
                : CompletableFuture.completedFuture(null);
        };
        return new Fixture(
            engine(repository, provider, audit, warnings::add, injector),
            repository,
            provider,
            audit,
            auditTypes,
            warnings
        );
    }

    private static TransactionEngine restart(Fixture fixture) {
        return engine(
            fixture.repository,
            fixture.provider,
            fixture.audit,
            fixture.warnings::add,
            FailureInjector.none()
        );
    }

    private static TransactionEngine engine(
        TransactionRepository repository,
        WayfarerWaymarkProvider provider,
        WayfarerAudit audit,
        java.util.function.Consumer<String> warnings,
        FailureInjector injector
    ) {
        return new TransactionEngine(
            repository,
            provider,
            audit,
            "test-server",
            Duration.ofMillis(20),
            CLOCK,
            warnings,
            injector
        );
    }

    private static FailureInjector crashAt(TransactionEngine.FailurePoint expected) {
        AtomicInteger remaining = new AtomicInteger(1);
        return (point, record) -> {
            if (point == expected && remaining.getAndDecrement() > 0) {
                throw new SimulatedCrash();
            }
        };
    }

    private static void assertCrash(
        java.util.function.Supplier<CompletionStage<TransactionResult>> operation
    ) {
        CompletionException failure = assertThrows(
            CompletionException.class,
            () -> operation.get().toCompletableFuture().join()
        );
        assertTrue(failure.getCause() instanceof SimulatedCrash);
    }

    private static TransactionRequest request(String key) {
        return new TransactionRequest(
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
        InMemoryRepository repository,
        FixtureProvider provider,
        WayfarerAudit audit,
        List<String> auditTypes,
        List<String> warnings
    ) {}

    private enum Mode {
        SUCCESS,
        INSUFFICIENT,
        KNOWN_FAILURE,
        TIMEOUT_BEFORE,
        TIMEOUT_AFTER,
        UNKNOWN_AFTER,
        RESOLUTION_UNKNOWN,
        OUTAGE
    }

    private static final class SimulatedCrash extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static final class FixtureProvider implements WayfarerWaymarkProvider {
        private final Mode mode;
        private final AtomicInteger debitCalls = new AtomicInteger();
        private final AtomicInteger debitEffects = new AtomicInteger();
        private final AtomicInteger refundCalls = new AtomicInteger();
        private final AtomicInteger refundEffects = new AtomicInteger();
        private final AtomicInteger resolveCalls = new AtomicInteger();
        private final Set<String> appliedDebit = new HashSet<>();
        private final Set<String> appliedRefund = new HashSet<>();
        private CompletableFuture<ResolutionResult> resolutionGate;

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
        public synchronized CompletionStage<EffectResult> debit(
            UUID playerUuid,
            long amount,
            String operationId
        ) {
            debitCalls.incrementAndGet();
            return switch (mode) {
                case SUCCESS -> {
                    apply(appliedDebit, operationId, debitEffects);
                    yield CompletableFuture.completedFuture(
                        new EffectResult(EffectStatus.SUCCEEDED, "debit-ref", null)
                    );
                }
                case INSUFFICIENT -> CompletableFuture.completedFuture(
                    new EffectResult(EffectStatus.INSUFFICIENT_FUNDS, null, "NO_FUNDS")
                );
                case KNOWN_FAILURE -> CompletableFuture.completedFuture(
                    new EffectResult(EffectStatus.KNOWN_FAILURE, null, "KNOWN_FAILURE")
                );
                case TIMEOUT_BEFORE -> new CompletableFuture<>();
                case TIMEOUT_AFTER -> {
                    apply(appliedDebit, operationId, debitEffects);
                    yield new CompletableFuture<>();
                }
                case UNKNOWN_AFTER -> {
                    apply(appliedDebit, operationId, debitEffects);
                    yield CompletableFuture.completedFuture(
                        new EffectResult(EffectStatus.UNKNOWN, "debit-ref", "UNCERTAIN")
                    );
                }
                case RESOLUTION_UNKNOWN -> CompletableFuture.completedFuture(
                    new EffectResult(EffectStatus.UNKNOWN, null, "UNCERTAIN")
                );
                case OUTAGE -> CompletableFuture.failedFuture(
                    new IllegalStateException("fixture outage")
                );
            };
        }

        @Override
        public synchronized CompletionStage<EffectResult> refund(
            UUID playerUuid,
            long amount,
            String operationId,
            String debitProviderReference
        ) {
            refundCalls.incrementAndGet();
            apply(appliedRefund, operationId, refundEffects);
            return CompletableFuture.completedFuture(
                new EffectResult(EffectStatus.SUCCEEDED, "refund-ref", null)
            );
        }

        @Override
        public synchronized CompletionStage<ResolutionResult> resolve(
            EffectKind effectKind,
            String operationId,
            String providerReference
        ) {
            resolveCalls.incrementAndGet();
            if (resolutionGate != null) {
                return resolutionGate;
            }
            if (mode == Mode.RESOLUTION_UNKNOWN) {
                return CompletableFuture.completedFuture(new ResolutionResult(
                    ResolutionStatus.UNKNOWN,
                    null,
                    "STILL_UNKNOWN"
                ));
            }
            boolean applied = (effectKind == EffectKind.DEBIT
                ? appliedDebit
                : appliedRefund).contains(operationId);
            return CompletableFuture.completedFuture(new ResolutionResult(
                applied ? ResolutionStatus.APPLIED : ResolutionStatus.NOT_APPLIED,
                applied
                    ? effectKind == EffectKind.DEBIT ? "debit-ref" : "refund-ref"
                    : null,
                applied ? null : "NOT_APPLIED"
            ));
        }

        private static void apply(
            Set<String> operations,
            String operationId,
            AtomicInteger effects
        ) {
            if (operations.add(operationId)) {
                effects.incrementAndGet();
            }
        }
    }

    private static final class InMemoryRepository implements TransactionRepository {
        private final Map<UUID, TransactionRecord> records = new HashMap<>();
        private final Map<String, UUID> keys = new HashMap<>();
        private final List<TransactionRecord> history = new ArrayList<>();

        @Override
        public synchronized CompletionStage<TransactionRecord> prepare(
            UUID transactionId,
            TransactionRequest request,
            String debitOperationId,
            Instant now
        ) {
            UUID existingId = keys.get(request.idempotencyKey());
            if (existingId != null) {
                TransactionRecord existing = records.get(existingId);
                if (!existing.sameRequest(request)) {
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
                request.payloadJson(),
                State.PREPARED,
                debitOperationId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0
            );
            records.put(transactionId, record);
            keys.put(request.idempotencyKey(), transactionId);
            history.add(record);
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
            TransactionUpdate update,
            Instant now
        ) {
            TransactionStateMachine.requireAllowed(current.state(), update.nextState());
            TransactionRecord stored = records.get(current.transactionId());
            if (!matches(stored, current)) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            TransactionRecord changed = new TransactionRecord(
                stored.transactionId(),
                stored.idempotencyKey(),
                stored.transactionType(),
                stored.actorUuid(),
                stored.subjectType(),
                stored.subjectId(),
                stored.amountWaymark(),
                stored.payloadJson(),
                update.nextState(),
                stored.debitOperationId(),
                coalesce(update.debitProviderReference(), stored.debitProviderReference()),
                coalesce(update.refundOperationId(), stored.refundOperationId()),
                coalesce(update.refundProviderReference(), stored.refundProviderReference()),
                update.refundTerminalState() == null
                    ? stored.refundTerminalState()
                    : update.refundTerminalState(),
                null,
                null,
                update.failureCode(),
                stored.lockVersion() + 1
            );
            records.put(changed.transactionId(), changed);
            history.add(changed);
            return CompletableFuture.completedFuture(Optional.of(changed));
        }

        @Override
        public synchronized CompletionStage<Optional<TransactionRecord>> claimRecovery(
            TransactionRecord current,
            String claimId,
            Instant claimUntil,
            Instant now
        ) {
            TransactionRecord stored = records.get(current.transactionId());
            if (!matches(stored, current)
                || stored.recoveryClaimUntil() != null
                && stored.recoveryClaimUntil().isAfter(now)) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            TransactionRecord claimed = new TransactionRecord(
                stored.transactionId(),
                stored.idempotencyKey(),
                stored.transactionType(),
                stored.actorUuid(),
                stored.subjectType(),
                stored.subjectId(),
                stored.amountWaymark(),
                stored.payloadJson(),
                stored.state(),
                stored.debitOperationId(),
                stored.debitProviderReference(),
                stored.refundOperationId(),
                stored.refundProviderReference(),
                stored.refundTerminalState(),
                claimId,
                claimUntil,
                stored.failureCode(),
                stored.lockVersion() + 1
            );
            records.put(claimed.transactionId(), claimed);
            history.add(claimed);
            return CompletableFuture.completedFuture(Optional.of(claimed));
        }

        @Override
        public synchronized CompletionStage<List<TransactionRecord>> findRecoverable(
            int limit
        ) {
            Set<State> states = EnumSet.of(
                State.PREPARED,
                State.DEBIT_PENDING,
                State.DEBITED,
                State.DOMAIN_COMMIT_PENDING,
                State.REFUND_PENDING,
                State.UNKNOWN
            );
            Instant now = CLOCK.instant();
            return CompletableFuture.completedFuture(records.values().stream()
                .filter(record -> states.contains(record.state()))
                .filter(record -> record.recoveryClaimUntil() == null
                    || !record.recoveryClaimUntil().isAfter(now))
                .limit(limit)
                .toList());
        }

        private synchronized TransactionRecord only() {
            assertEquals(1, records.size());
            return records.values().iterator().next();
        }

        private synchronized List<State> historyStates() {
            return history.stream().map(TransactionRecord::state).distinct().toList();
        }

        private static boolean matches(
            TransactionRecord stored,
            TransactionRecord expected
        ) {
            return stored != null
                && stored.state() == expected.state()
                && stored.lockVersion() == expected.lockVersion();
        }

        private static String coalesce(String candidate, String current) {
            return candidate == null ? current : candidate;
        }
    }

    private static TransactionRequest copyWithNullableActor(
        TransactionRequest source
    ) {
        return new TransactionRequest(
            source.idempotencyKey(),
            source.transactionType(),
            null,
            source.subjectType(),
            source.subjectId(),
            source.amountWaymark(),
            source.payloadJson()
        );
    }
}
