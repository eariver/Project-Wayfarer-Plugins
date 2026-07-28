package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class TransactionEngine implements WayfarerTransactions {
    private static final int TEXT_LIMIT = 191;
    private static final String MIRROR_WARNING =
        "Wayfarer transaction audit mirror failed";

    private final TransactionRepository repository;
    private final WayfarerWaymarkProvider provider;
    private final WayfarerAudit audit;
    private final String serverId;
    private final Duration providerTimeout;
    private final Clock clock;
    private final Consumer<String> mirrorFailureObserver;
    private final FailureInjector failureInjector;

    public TransactionEngine(
        TransactionRepository repository,
        WayfarerWaymarkProvider provider,
        WayfarerAudit audit,
        String serverId,
        Duration providerTimeout,
        Clock clock
    ) {
        this(
            repository,
            provider,
            audit,
            serverId,
            providerTimeout,
            clock,
            ignored -> {},
            FailureInjector.none()
        );
    }

    public TransactionEngine(
        TransactionRepository repository,
        WayfarerWaymarkProvider provider,
        WayfarerAudit audit,
        String serverId,
        Duration providerTimeout,
        Clock clock,
        Consumer<String> mirrorFailureObserver
    ) {
        this(
            repository,
            provider,
            audit,
            serverId,
            providerTimeout,
            clock,
            mirrorFailureObserver,
            FailureInjector.none()
        );
    }

    TransactionEngine(
        TransactionRepository repository,
        WayfarerWaymarkProvider provider,
        WayfarerAudit audit,
        String serverId,
        Duration providerTimeout,
        Clock clock,
        Consumer<String> mirrorFailureObserver,
        FailureInjector failureInjector
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.provider = Objects.requireNonNull(provider, "provider");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.serverId = safeText(serverId, "serverId", 64);
        this.providerTimeout = Objects.requireNonNull(providerTimeout, "providerTimeout");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.mirrorFailureObserver = Objects.requireNonNull(
            mirrorFailureObserver,
            "mirrorFailureObserver"
        );
        this.failureInjector = Objects.requireNonNull(failureInjector, "failureInjector");
    }

    @Override
    public CompletionStage<TransactionResult> execute(TransactionRequest request) {
        validate(request);
        UUID transactionId = UUID.randomUUID();
        String operationId = "debit-" + transactionId;
        return repository.prepare(transactionId, request, operationId, clock.instant())
            .thenCompose(record -> {
                if (record.state() != State.PREPARED) {
                    return CompletableFuture.completedFuture(record.result());
                }
                failureInjector.at(FailurePoint.AFTER_PREPARED_COMMIT, record);
                return mirror(record, "TRANSACTION_PREPARED", null)
                    .thenCompose(ignored -> apply(
                        record,
                        TransactionUpdate.to(State.DEBIT_PENDING, null),
                        "DEBIT_REQUESTED"
                    )).thenCompose(claimed -> {
                        if (claimed.isEmpty()) {
                            return currentResult(record.transactionId());
                        }
                        TransactionRecord pending = claimed.orElseThrow();
                        failureInjector.at(
                            FailurePoint.AFTER_DEBIT_PENDING_COMMIT,
                            pending
                        );
                        return debit(pending);
                    });
            });
    }

    @Override
    public CompletionStage<TransactionResult> reconcile(UUID transactionId) {
        return reconcile(transactionId, ReconcileAction.AUTO);
    }

    @Override
    public CompletionStage<TransactionResult> reconcile(
        UUID transactionId,
        ReconcileAction action
    ) {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(action, "action");
        return require(transactionId).thenCompose(record ->
            criticalAudit(record, "RECONCILE_REQUESTED", null)
                .thenCompose(ignored -> reconcile(record, action))
        );
    }

    @Override
    public CompletionStage<TransactionDetails> inspect(UUID transactionId) {
        return require(Objects.requireNonNull(transactionId, "transactionId"))
            .thenCompose(record -> criticalAudit(
                record,
                "ADMIN_TRANSACTION_INSPECTED",
                null
            ).thenApply(ignored -> record.details()));
    }

    public CompletionStage<Integer> recoverPending(int batchLimit) {
        return repository.findRecoverable(batchLimit).thenCompose(records -> {
            CompletionStage<Integer> recovered = CompletableFuture.completedFuture(0);
            for (TransactionRecord record : records) {
                recovered = recovered.thenCompose(count -> recover(record)
                    .thenApply(ignored -> count + 1));
            }
            return recovered;
        });
    }

    private CompletionStage<TransactionResult> reconcile(
        TransactionRecord record,
        ReconcileAction action
    ) {
        if (TransactionStateMachine.terminal(record.state())) {
            return CompletableFuture.completedFuture(record.result());
        }
        return switch (action) {
            case AUTO -> recover(record);
            case COMMIT -> manualCommit(record);
            case REFUND -> manualRefund(record);
            case FAIL -> manualFail(record);
        };
    }

    private CompletionStage<TransactionResult> manualCommit(TransactionRecord record) {
        return switch (record.state()) {
            case UNKNOWN -> transitionResult(
                record,
                TransactionUpdate.to(State.RECONCILED_COMMITTED, null),
                "RECONCILED_COMMITTED"
            );
            case DEBITED, DOMAIN_COMMIT_PENDING -> recover(record);
            default -> failed("Manual commit is unsafe for the current transaction state");
        };
    }

    private CompletionStage<TransactionResult> manualRefund(TransactionRecord record) {
        return switch (record.state()) {
            case REFUND_PENDING -> recover(record);
            case UNKNOWN -> record.refundOperationId() == null
                ? refund(record, State.RECONCILED_REFUNDED)
                : recover(record);
            case DEBITED, DOMAIN_COMMIT_PENDING ->
                refund(record, State.RECONCILED_REFUNDED);
            default -> failed("Manual refund is unsafe for the current transaction state");
        };
    }

    private CompletionStage<TransactionResult> manualFail(TransactionRecord record) {
        if (record.state() != State.UNKNOWN && record.state() != State.PREPARED) {
            return failed("Manual failure is unsafe for the current transaction state");
        }
        return transitionResult(
            record,
            TransactionUpdate.to(State.FAILED, "MANUAL_RECONCILE_FAILED"),
            "ADMIN_TRANSACTION_RECONCILED"
        );
    }

    private CompletionStage<TransactionResult> recover(TransactionRecord record) {
        return switch (record.state()) {
            case PREPARED -> apply(
                record,
                TransactionUpdate.to(State.DEBIT_PENDING, null),
                "DEBIT_REQUESTED"
            ).thenCompose(claimed -> {
                if (claimed.isEmpty()) {
                    return currentResult(record.transactionId());
                }
                TransactionRecord pending = claimed.orElseThrow();
                failureInjector.at(FailurePoint.AFTER_DEBIT_PENDING_COMMIT, pending);
                return debit(pending);
            });
            case DEBIT_PENDING -> resolveDebit(record);
            case DEBITED, DOMAIN_COMMIT_PENDING -> completeDomainCommit(record);
            case REFUND_PENDING -> resolveRefund(record);
            case UNKNOWN -> record.refundOperationId() == null
                ? resolveDebit(record)
                : resolveRefund(record);
            default -> CompletableFuture.completedFuture(record.result());
        };
    }

    private CompletionStage<TransactionResult> debit(TransactionRecord pending) {
        CompletionStage<WayfarerWaymarkProvider.EffectResult> call;
        try {
            call = provider.debit(
                pending.actorUuid(),
                pending.amountWaymark(),
                pending.debitOperationId()
            );
        } catch (RuntimeException failure) {
            return unknown(pending, EffectKind.DEBIT, "PROVIDER_CALL_FAILED", null);
        }
        return timed(call).handle((effect, failure) -> failure == null ? effect : null)
            .thenCompose(effect -> {
                if (effect == null || effect.status() == EffectStatus.UNKNOWN) {
                    return unknown(
                        pending,
                        EffectKind.DEBIT,
                        "PROVIDER_RESULT_UNKNOWN",
                        effect == null ? null : effect.providerReference()
                    );
                }
                failureInjector.at(FailurePoint.AFTER_DEBIT_EFFECT, pending);
                if (effect.status() != EffectStatus.SUCCEEDED) {
                    String code = effect.status() == EffectStatus.INSUFFICIENT_FUNDS
                        ? "INSUFFICIENT_FUNDS"
                        : safeFailure(effect.failureCode());
                    return transitionResult(
                        pending,
                        new TransactionUpdate(
                            State.FAILED,
                            safeOptional(effect.providerReference()),
                            null,
                            null,
                            null,
                            code
                        ),
                        "DEBIT_FAILED"
                    );
                }
                return apply(
                    pending,
                    new TransactionUpdate(
                        State.DEBITED,
                        safeOptional(effect.providerReference()),
                        null,
                        null,
                        null,
                        null
                    ),
                    "DEBIT_SUCCEEDED"
                ).thenCompose(debited -> {
                    if (debited.isEmpty()) {
                        return currentResult(pending.transactionId());
                    }
                    TransactionRecord applied = debited.orElseThrow();
                    failureInjector.at(FailurePoint.AFTER_DEBITED_COMMIT, applied);
                    return completeDomainCommit(applied);
                });
            });
    }

    private CompletionStage<TransactionResult> completeDomainCommit(
        TransactionRecord record
    ) {
        CompletionStage<Optional<TransactionRecord>> domain;
        if (record.state() == State.DEBITED) {
            domain = apply(
                record,
                TransactionUpdate.to(State.DOMAIN_COMMIT_PENDING, null),
                "DOMAIN_COMMIT_REQUESTED"
            );
        } else if (record.state() == State.DOMAIN_COMMIT_PENDING) {
            domain = CompletableFuture.completedFuture(Optional.of(record));
        } else {
            return currentResult(record.transactionId());
        }
        return domain.thenCompose(claimed -> {
            if (claimed.isEmpty()) {
                return currentResult(record.transactionId());
            }
            TransactionRecord pending = claimed.orElseThrow();
            failureInjector.at(FailurePoint.AFTER_DOMAIN_COMMIT_PENDING, pending);
            return apply(
                pending,
                TransactionUpdate.to(State.COMMITTED, null),
                "TRANSACTION_COMMITTED"
            ).thenCompose(committed -> {
                if (committed.isEmpty()) {
                    return currentResult(record.transactionId());
                }
                TransactionRecord completed = committed.orElseThrow();
                failureInjector.at(FailurePoint.AFTER_COMMITTED_COMMIT, completed);
                return CompletableFuture.completedFuture(completed.result());
            });
        });
    }

    private CompletionStage<TransactionResult> refund(
        TransactionRecord record,
        State terminalState
    ) {
        String operationId = record.refundOperationId() == null
            ? "refund-" + record.transactionId()
            : record.refundOperationId();
        return apply(
            record,
            new TransactionUpdate(
                State.REFUND_PENDING,
                null,
                operationId,
                null,
                terminalState,
                null
            ),
            "REFUND_REQUESTED"
        ).thenCompose(claimed -> {
            if (claimed.isEmpty()) {
                return currentResult(record.transactionId());
            }
            TransactionRecord pending = claimed.orElseThrow();
            failureInjector.at(FailurePoint.AFTER_REFUND_PENDING_COMMIT, pending);
            return callRefund(pending);
        });
    }

    private CompletionStage<TransactionResult> callRefund(TransactionRecord pending) {
        CompletionStage<WayfarerWaymarkProvider.EffectResult> call;
        try {
            call = provider.refund(
                pending.actorUuid(),
                pending.amountWaymark(),
                pending.refundOperationId(),
                pending.debitProviderReference()
            );
        } catch (RuntimeException failure) {
            return unknown(pending, EffectKind.REFUND, "REFUND_CALL_FAILED", null);
        }
        return timed(call).handle((effect, failure) -> failure == null ? effect : null)
            .thenCompose(effect -> {
                if (effect == null || effect.status() == EffectStatus.UNKNOWN) {
                    return unknown(
                        pending,
                        EffectKind.REFUND,
                        "REFUND_RESULT_UNKNOWN",
                        effect == null ? null : effect.providerReference()
                    );
                }
                failureInjector.at(FailurePoint.AFTER_REFUND_EFFECT, pending);
                if (effect.status() != EffectStatus.SUCCEEDED) {
                    return unknown(
                        pending,
                        EffectKind.REFUND,
                        safeFailure(effect.failureCode()),
                        effect.providerReference()
                    );
                }
                State next = pending.refundTerminalState() == null
                    ? State.RECONCILED_REFUNDED
                    : pending.refundTerminalState();
                return transitionResult(
                    pending,
                    new TransactionUpdate(
                        next,
                        null,
                        null,
                        safeOptional(effect.providerReference()),
                        null,
                        null
                    ),
                    next == State.REFUNDED
                        ? "REFUND_SUCCEEDED"
                        : "RECONCILED_REFUNDED"
                );
            });
    }

    private CompletionStage<TransactionResult> resolveDebit(TransactionRecord record) {
        return claimRecovery(record).thenCompose(claimed -> {
            if (claimed.isEmpty()) {
                return currentResult(record.transactionId());
            }
            TransactionRecord owned = claimed.orElseThrow();
            return resolve(
                WayfarerWaymarkProvider.EffectKind.DEBIT,
                owned.debitOperationId(),
                owned.debitProviderReference()
            ).thenCompose(result -> switch (result.status()) {
                case APPLIED -> debitApplied(owned, result.providerReference());
                case NOT_APPLIED -> transitionResult(
                    owned,
                    new TransactionUpdate(
                        State.FAILED,
                        safeOptional(result.providerReference()),
                        null,
                        null,
                        null,
                        safeResolutionFailure(result, "DEBIT_NOT_APPLIED")
                    ),
                    "DEBIT_FAILED"
                );
                case UNKNOWN -> unknown(
                    owned,
                    EffectKind.DEBIT,
                    safeResolutionFailure(result, "DEBIT_RESOLUTION_UNKNOWN"),
                    result.providerReference()
                );
            });
        });
    }

    private CompletionStage<TransactionResult> debitApplied(
        TransactionRecord record,
        String providerReference
    ) {
        if (record.state() == State.UNKNOWN) {
            return transitionResult(
                record,
                new TransactionUpdate(
                    State.RECONCILED_COMMITTED,
                    safeOptional(providerReference),
                    null,
                    null,
                    null,
                    null
                ),
                "RECONCILED_COMMITTED"
            );
        }
        return apply(
            record,
            new TransactionUpdate(
                State.DEBITED,
                safeOptional(providerReference),
                null,
                null,
                null,
                null
            ),
            "DEBIT_SUCCEEDED"
        ).thenCompose(debited -> {
            if (debited.isEmpty()) {
                return currentResult(record.transactionId());
            }
            return completeDomainCommit(debited.orElseThrow());
        });
    }

    private CompletionStage<TransactionResult> resolveRefund(TransactionRecord record) {
        return claimRecovery(record).thenCompose(claimed -> {
            if (claimed.isEmpty()) {
                return currentResult(record.transactionId());
            }
            TransactionRecord owned = claimed.orElseThrow();
            return resolve(
                WayfarerWaymarkProvider.EffectKind.REFUND,
                owned.refundOperationId(),
                owned.refundProviderReference()
            ).thenCompose(result -> switch (result.status()) {
                case APPLIED -> {
                    State next = owned.refundTerminalState() == null
                        ? State.RECONCILED_REFUNDED
                        : owned.refundTerminalState();
                    yield transitionResult(
                        owned,
                        new TransactionUpdate(
                            next,
                            null,
                            null,
                            safeOptional(result.providerReference()),
                            null,
                            null
                        ),
                        next == State.REFUNDED
                            ? "REFUND_SUCCEEDED"
                            : "RECONCILED_REFUNDED"
                    );
                }
                case NOT_APPLIED -> unknown(
                    owned,
                    EffectKind.REFUND,
                    safeResolutionFailure(result, "REFUND_NOT_APPLIED"),
                    result.providerReference()
                );
                case UNKNOWN -> unknown(
                    owned,
                    EffectKind.REFUND,
                    safeResolutionFailure(result, "REFUND_RESOLUTION_UNKNOWN"),
                    result.providerReference()
                );
            });
        });
    }

    private CompletionStage<WayfarerWaymarkProvider.ResolutionResult> resolve(
        WayfarerWaymarkProvider.EffectKind kind,
        String operationId,
        String providerReference
    ) {
        if (operationId == null) {
            return CompletableFuture.completedFuture(
                new WayfarerWaymarkProvider.ResolutionResult(
                    WayfarerWaymarkProvider.ResolutionStatus.UNKNOWN,
                    null,
                    "MISSING_OPERATION_ID"
                )
            );
        }
        CompletionStage<WayfarerWaymarkProvider.ResolutionResult> call;
        try {
            call = provider.resolve(kind, operationId, providerReference);
        } catch (RuntimeException failure) {
            return CompletableFuture.completedFuture(unknownResolution());
        }
        return timed(call).handle((resolution, failure) ->
            failure == null && resolution != null ? resolution : unknownResolution()
        );
    }

    private CompletionStage<Optional<TransactionRecord>> claimRecovery(
        TransactionRecord record
    ) {
        Instant now = clock.instant();
        Instant claimUntil = now.plus(providerTimeout.multipliedBy(2));
        return repository.claimRecovery(
            record,
            UUID.randomUUID().toString(),
            claimUntil,
            now
        ).thenCompose(claimed -> {
            if (claimed.isEmpty()) {
                return CompletableFuture.completedFuture(claimed);
            }
            TransactionRecord owned = claimed.orElseThrow();
            return mirror(owned, "TRANSACTION_RECOVERY_CLAIMED", null)
                .thenApply(ignored -> claimed);
        });
    }

    private CompletionStage<TransactionResult> unknown(
        TransactionRecord record,
        EffectKind kind,
        String failureCode,
        String providerReference
    ) {
        TransactionUpdate update = kind == EffectKind.DEBIT
            ? new TransactionUpdate(
                State.UNKNOWN,
                safeOptional(providerReference),
                null,
                null,
                null,
                safeFailure(failureCode)
            )
            : new TransactionUpdate(
                State.UNKNOWN,
                null,
                null,
                safeOptional(providerReference),
                null,
                safeFailure(failureCode)
            );
        return transitionResult(record, update, "TRANSACTION_UNKNOWN");
    }

    private CompletionStage<TransactionResult> transitionResult(
        TransactionRecord current,
        TransactionUpdate update,
        String auditType
    ) {
        return apply(current, update, auditType).thenCompose(updated ->
            updated.isEmpty()
                ? currentResult(current.transactionId())
                : CompletableFuture.completedFuture(updated.orElseThrow().result())
        );
    }

    private CompletionStage<Optional<TransactionRecord>> apply(
        TransactionRecord current,
        TransactionUpdate update,
        String auditType
    ) {
        TransactionUpdate safeUpdate = new TransactionUpdate(
            update.nextState(),
            safeOptional(update.debitProviderReference()),
            safeOptional(update.refundOperationId()),
            safeOptional(update.refundProviderReference()),
            update.refundTerminalState(),
            update.failureCode() == null ? null : safeFailure(update.failureCode())
        );
        return repository.transition(current, safeUpdate, clock.instant())
            .thenCompose(updated -> {
                if (updated.isEmpty()) {
                    return CompletableFuture.completedFuture(updated);
                }
                TransactionRecord record = updated.orElseThrow();
                return mirror(record, auditType, safeUpdate.failureCode())
                    .thenApply(ignored -> updated);
            });
    }

    private CompletionStage<Void> mirror(
        TransactionRecord record,
        String eventType,
        String failureCode
    ) {
        CompletionStage<Void> completion;
        try {
            completion = auditRecord(record, eventType, failureCode);
        } catch (RuntimeException failure) {
            observeMirrorFailure();
            return CompletableFuture.completedFuture(null);
        }
        return completion.handle((ignored, failure) -> {
            if (failure != null) {
                observeMirrorFailure();
            }
            return null;
        });
    }

    private CompletionStage<Void> criticalAudit(
        TransactionRecord record,
        String eventType,
        String failureCode
    ) {
        try {
            return auditRecord(record, eventType, failureCode);
        } catch (RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
    }

    private CompletionStage<Void> auditRecord(
        TransactionRecord record,
        String eventType,
        String failureCode
    ) {
        String details = failureCode == null
            ? "{\"state\":\"" + record.state().name() + "\"}"
            : "{\"state\":\"" + record.state().name()
                + "\",\"failure_code\":\"" + safeFailure(failureCode) + "\"}";
        return Objects.requireNonNull(
            audit.record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                eventType,
                record.actorUuid(),
                "TRANSACTION",
                record.transactionId().toString(),
                serverId,
                details,
                clock.instant()
            )),
            "audit completion"
        );
    }

    private void observeMirrorFailure() {
        try {
            mirrorFailureObserver.accept(MIRROR_WARNING);
        } catch (RuntimeException ignored) {
            // Durable transaction history remains authoritative.
        }
    }

    private CompletionStage<TransactionRecord> require(UUID transactionId) {
        return repository.find(transactionId).thenApply(record -> record.orElseThrow(
            () -> new TransactionException("Transaction was not found")
        ));
    }

    private CompletionStage<TransactionResult> currentResult(UUID transactionId) {
        return require(transactionId).thenApply(TransactionRecord::result);
    }

    private <T> CompletionStage<T> timed(CompletionStage<T> stage) {
        return Objects.requireNonNull(stage, "provider completion")
            .toCompletableFuture()
            .orTimeout(providerTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static void validate(TransactionRequest request) {
        Objects.requireNonNull(request, "request");
        safeText(request.idempotencyKey(), "idempotencyKey", TEXT_LIMIT);
        safeText(request.transactionType(), "transactionType", 64);
        safeText(request.subjectType(), "subjectType", 64);
        safeText(request.subjectId(), "subjectId", TEXT_LIMIT);
        if (request.amountWaymark() <= 0) {
            throw new IllegalArgumentException("amountWaymark must be positive");
        }
        if (request.payloadJson() != null && request.payloadJson().length() > 16_384) {
            throw new IllegalArgumentException("payloadJson exceeds safe bound");
        }
    }

    private static String safeResolutionFailure(
        WayfarerWaymarkProvider.ResolutionResult result,
        String fallback
    ) {
        return result.failureCode() == null || result.failureCode().isBlank()
            ? fallback
            : safeFailure(result.failureCode());
    }

    private static String safeFailure(String value) {
        return value == null || value.isBlank()
            ? "PROVIDER_FAILURE"
            : safeText(value, "failureCode", 96);
    }

    private static String safeOptional(String value) {
        return value == null ? null : safeText(value, "provider value", TEXT_LIMIT);
    }

    private static String safeText(String value, String field, int maximum) {
        Objects.requireNonNull(value, field);
        if (value.isBlank() || value.length() > maximum
            || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return value;
    }

    private static <T> CompletionStage<T> failed(String message) {
        return CompletableFuture.failedFuture(new TransactionException(message));
    }

    private static WayfarerWaymarkProvider.ResolutionResult unknownResolution() {
        return new WayfarerWaymarkProvider.ResolutionResult(
            WayfarerWaymarkProvider.ResolutionStatus.UNKNOWN,
            null,
            "PROVIDER_RESOLUTION_UNKNOWN"
        );
    }

    private enum EffectKind {
        DEBIT,
        REFUND
    }

    enum FailurePoint {
        AFTER_PREPARED_COMMIT,
        AFTER_DEBIT_PENDING_COMMIT,
        AFTER_DEBIT_EFFECT,
        AFTER_DEBITED_COMMIT,
        AFTER_DOMAIN_COMMIT_PENDING,
        AFTER_COMMITTED_COMMIT,
        AFTER_REFUND_PENDING_COMMIT,
        AFTER_REFUND_EFFECT
    }

    @FunctionalInterface
    interface FailureInjector {
        void at(FailurePoint point, TransactionRecord record);

        static FailureInjector none() {
            return (point, record) -> {};
        }
    }
}
