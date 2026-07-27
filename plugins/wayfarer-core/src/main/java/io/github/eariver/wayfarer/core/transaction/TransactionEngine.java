package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public final class TransactionEngine implements WayfarerTransactions {
    private static final int TEXT_LIMIT = 191;

    private final TransactionRepository repository;
    private final WayfarerWaymarkProvider provider;
    private final WayfarerAudit audit;
    private final String serverId;
    private final Duration providerTimeout;
    private final Clock clock;

    public TransactionEngine(
        TransactionRepository repository,
        WayfarerWaymarkProvider provider,
        WayfarerAudit audit,
        String serverId,
        Duration providerTimeout,
        Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.provider = Objects.requireNonNull(provider, "provider");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.serverId = safeText(serverId, "serverId", 64);
        this.providerTimeout = Objects.requireNonNull(providerTimeout, "providerTimeout");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CompletionStage<TransactionResult> execute(TransactionRequest request) {
        validate(request);
        UUID transactionId = UUID.randomUUID();
        String operationId = "debit-" + transactionId;
        Instant now = clock.instant();
        return repository.prepare(transactionId, request, operationId, now)
            .thenCompose(record -> {
                if (record.state() != State.PREPARED) {
                    return CompletableFuture.completedFuture(record.result());
                }
                return audit(record, "TRANSACTION_PREPARED", null)
                    .thenCompose(ignored -> claim(
                        record,
                        State.DEBIT_PENDING,
                        null,
                        null,
                        "DEBIT_REQUESTED"
                    )).thenCompose(pending -> pending.isEmpty()
                    ? currentResult(record.transactionId())
                    : debit(pending.orElseThrow()));
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
        return require(transactionId).thenCompose(record -> {
            if (record.state() != State.UNKNOWN) {
                return CompletableFuture.completedFuture(record.result());
            }
            return audit(record, "RECONCILE_REQUESTED", null).thenCompose(ignored -> switch (action) {
                case COMMIT -> reconcileCommitted(record);
                case REFUND -> refund(record, true);
                case FAIL -> transition(
                    record,
                    State.FAILED,
                    null,
                    "MANUAL_RECONCILE_FAILED",
                    "ADMIN_TRANSACTION_RECONCILED"
                ).thenApply(TransactionRecord::result);
                case AUTO -> automaticReconcile(record);
            });
        });
    }

    @Override
    public CompletionStage<TransactionDetails> inspect(UUID transactionId) {
        return require(Objects.requireNonNull(transactionId, "transactionId"))
            .thenCompose(record -> audit(record, "ADMIN_TRANSACTION_INSPECTED", null)
                .thenApply(ignored -> record.details()));
    }

    private CompletionStage<TransactionResult> debit(TransactionRecord pending) {
        CompletionStage<WayfarerWaymarkProvider.EffectResult> call;
        try {
            call = provider.debit(
                pending.actorUuid(),
                pending.amountWaymark(),
                pending.providerOperationId()
            );
        } catch (RuntimeException failure) {
            return unknown(pending, "PROVIDER_CALL_FAILED", null);
        }
        return timed(call).handle((effect, failure) -> failure == null ? effect : null)
            .thenCompose(effect -> {
                if (effect == null
                    || effect.status() == WayfarerWaymarkProvider.EffectStatus.UNKNOWN) {
                    return unknown(
                        pending,
                        "PROVIDER_RESULT_UNKNOWN",
                        effect == null ? null : effect.providerReference()
                    );
                }
                if (effect.status() != WayfarerWaymarkProvider.EffectStatus.SUCCEEDED) {
                    String code = effect.status()
                        == WayfarerWaymarkProvider.EffectStatus.INSUFFICIENT_FUNDS
                        ? "INSUFFICIENT_FUNDS"
                        : safeFailure(effect.failureCode());
                    return transition(
                        pending,
                        State.FAILED,
                        effect.providerReference(),
                        code,
                        "DEBIT_FAILED"
                    ).thenApply(TransactionRecord::result);
                }
                return transition(
                    pending,
                    State.DEBITED,
                    effect.providerReference(),
                    null,
                    "DEBIT_SUCCEEDED"
                ).thenCompose(debited -> transition(
                    debited,
                    State.DOMAIN_COMMIT_PENDING,
                    null,
                    null,
                    "DOMAIN_COMMIT_REQUESTED"
                )).thenCompose(domain -> transition(
                    domain,
                    State.COMMITTED,
                    null,
                    null,
                    "TRANSACTION_COMMITTED"
                )).thenApply(TransactionRecord::result);
            });
    }

    private CompletionStage<TransactionResult> automaticReconcile(TransactionRecord record) {
        CompletionStage<WayfarerWaymarkProvider.EffectResolution> call;
        try {
            call = provider.resolve(record.providerOperationId(), record.providerReference());
        } catch (RuntimeException failure) {
            return CompletableFuture.completedFuture(record.result());
        }
        return timed(call).handle((resolution, failure) ->
            failure == null ? resolution : WayfarerWaymarkProvider.EffectResolution.UNKNOWN
        ).thenCompose(resolution -> switch (resolution) {
            case APPLIED -> reconcileCommitted(record);
            case NOT_APPLIED -> transition(
                record,
                State.FAILED,
                null,
                "RECONCILED_NOT_APPLIED",
                "ADMIN_TRANSACTION_RECONCILED"
            ).thenApply(TransactionRecord::result);
            case UNKNOWN -> CompletableFuture.completedFuture(record.result());
        });
    }

    private CompletionStage<TransactionResult> reconcileCommitted(TransactionRecord record) {
        return transition(
            record,
            State.RECONCILED_COMMITTED,
            null,
            null,
            "RECONCILED_COMMITTED"
        ).thenApply(TransactionRecord::result);
    }

    private CompletionStage<TransactionResult> refund(
        TransactionRecord record,
        boolean reconciled
    ) {
        return claim(
            record,
            State.REFUND_PENDING,
            null,
            null,
            "REFUND_REQUESTED"
        ).thenCompose(claimed -> {
            if (claimed.isEmpty()) {
                return currentResult(record.transactionId());
            }
            TransactionRecord pending = claimed.orElseThrow();
            CompletionStage<WayfarerWaymarkProvider.EffectResult> call;
            try {
                call = provider.refund(
                    pending.actorUuid(),
                    pending.amountWaymark(),
                    "refund-" + pending.transactionId(),
                    pending.providerReference()
                );
            } catch (RuntimeException failure) {
                return unknown(pending, "REFUND_CALL_FAILED", null);
            }
            return timed(call).handle((effect, failure) -> failure == null ? effect : null)
                .thenCompose(effect -> {
                    if (effect == null
                        || effect.status() == WayfarerWaymarkProvider.EffectStatus.UNKNOWN) {
                        return unknown(
                            pending,
                            "REFUND_RESULT_UNKNOWN",
                            effect == null ? null : effect.providerReference()
                        );
                    }
                    if (effect.status() != WayfarerWaymarkProvider.EffectStatus.SUCCEEDED) {
                        return transition(
                            pending,
                            State.FAILED,
                            effect.providerReference(),
                            safeFailure(effect.failureCode()),
                            "REFUND_FAILED"
                        ).thenApply(TransactionRecord::result);
                    }
                    State next = reconciled ? State.RECONCILED_REFUNDED : State.REFUNDED;
                    return transition(
                        pending,
                        next,
                        effect.providerReference(),
                        null,
                        reconciled ? "RECONCILED_REFUNDED" : "REFUND_SUCCEEDED"
                    ).thenApply(TransactionRecord::result);
                });
        });
    }

    private CompletionStage<TransactionResult> unknown(
        TransactionRecord record,
        String failureCode,
        String providerReference
    ) {
        return transition(
            record,
            State.UNKNOWN,
            providerReference,
            failureCode,
            "TRANSACTION_UNKNOWN"
        ).thenApply(TransactionRecord::result);
    }

    private CompletionStage<TransactionRecord> transition(
        TransactionRecord current,
        State next,
        String providerReference,
        String failureCode,
        String auditType
    ) {
        return repository.transition(
            current,
            next,
            safeOptional(providerReference),
            safeOptional(failureCode),
            clock.instant()
        ).thenCompose(updated -> {
            if (updated.isEmpty()) {
                return require(current.transactionId());
            }
            TransactionRecord record = updated.orElseThrow();
            return audit(record, auditType, failureCode).thenApply(ignored -> record);
        });
    }

    private CompletionStage<java.util.Optional<TransactionRecord>> claim(
        TransactionRecord current,
        State next,
        String providerReference,
        String failureCode,
        String auditType
    ) {
        return repository.transition(
            current,
            next,
            safeOptional(providerReference),
            safeOptional(failureCode),
            clock.instant()
        ).thenCompose(updated -> {
            if (updated.isEmpty()) {
                return CompletableFuture.completedFuture(updated);
            }
            TransactionRecord record = updated.orElseThrow();
            return audit(record, auditType, failureCode).thenApply(ignored -> updated);
        });
    }

    private CompletionStage<Void> audit(
        TransactionRecord record,
        String eventType,
        String failureCode
    ) {
        String details = failureCode == null
            ? "{\"state\":\"" + record.state().name() + "\"}"
            : "{\"state\":\"" + record.state().name()
                + "\",\"failure_code\":\"" + safeFailure(failureCode) + "\"}";
        return audit.record(new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            eventType,
            record.actorUuid(),
            "TRANSACTION",
            record.transactionId().toString(),
            serverId,
            details,
            clock.instant()
        ));
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
        Objects.requireNonNull(request.actorUuid(), "actorUuid");
        safeText(request.subjectType(), "subjectType", 64);
        safeText(request.subjectId(), "subjectId", TEXT_LIMIT);
        if (request.amountWaymark() <= 0) {
            throw new IllegalArgumentException("amountWaymark must be positive");
        }
        if (request.payloadJson() != null && request.payloadJson().length() > 16_384) {
            throw new IllegalArgumentException("payloadJson exceeds safe bound");
        }
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
}
