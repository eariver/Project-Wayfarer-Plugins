package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.main.domain.EvolutionPlan;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.domain.ReissueOperation;
import io.github.eariver.wayfarer.main.domain.ReissuePricing;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/** Durable paid reissue saga. Bukkit interaction enters only through ports. */
public final class ReissueCoordinator {
    private static final Duration QUOTE_TTL = Duration.ofSeconds(60);
    private static final String TRANSACTION_TYPE = "MAIN_TOOL_REISSUE";
    private static final String SUBJECT_TYPE = "GROWTH_TOOL";
    private final ReissueOperationRepository operations;
    private final GrowthToolRepository growthTools;
    private final WayfarerTransactions transactions;
    private final WayfarerTasks tasks;
    private final io.github.eariver.wayfarer.api.WayfarerAudit audit;
    private final ReissuePricing pricing;
    private final EvolutionPlan evolutionPlan;
    private final EvolutionPlan.EnchantmentCaps enchantmentCaps;
    private final String configRevision;
    private final ReissueQuoteStore quoteStore;
    private final ReissueEligibilityPort eligibility;
    private final ReissueDeliveryGateway deliveryGateway;
    private final String serverId;
    private final Clock clock;
    private final Set<UUID> confirmInFlight = ConcurrentHashMap.newKeySet();
    private final Set<UUID> resumePaymentInFlight = ConcurrentHashMap.newKeySet();

    public ReissueCoordinator(
        ReissueOperationRepository operations,
        GrowthToolRepository growthTools,
        WayfarerTransactions transactions,
        WayfarerTasks tasks,
        WayfarerAudit audit,
        ReissuePricing pricing,
        EvolutionPlan evolutionPlan,
        String configRevision,
        ReissueQuoteStore quoteStore,
        ReissueEligibilityPort eligibility,
        ReissueDeliveryGateway deliveryGateway,
        String serverId,
        Clock clock
    ) {
        this(
            operations,
            growthTools,
            transactions,
            tasks,
            audit,
            pricing,
            evolutionPlan,
            EvolutionPlan.EnchantmentCaps.defaults(),
            configRevision,
            quoteStore,
            eligibility,
            deliveryGateway,
            serverId,
            clock
        );
    }

    public ReissueCoordinator(
        ReissueOperationRepository operations,
        GrowthToolRepository growthTools,
        WayfarerTransactions transactions,
        WayfarerTasks tasks,
        WayfarerAudit audit,
        ReissuePricing pricing,
        EvolutionPlan evolutionPlan,
        EvolutionPlan.EnchantmentCaps enchantmentCaps,
        String configRevision,
        ReissueQuoteStore quoteStore,
        ReissueEligibilityPort eligibility,
        ReissueDeliveryGateway deliveryGateway,
        String serverId,
        Clock clock
    ) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.growthTools = Objects.requireNonNull(growthTools, "growthTools");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.pricing = Objects.requireNonNull(pricing, "pricing");
        this.evolutionPlan = Objects.requireNonNull(evolutionPlan, "evolutionPlan");
        this.enchantmentCaps = Objects.requireNonNull(enchantmentCaps, "enchantmentCaps");
        this.configRevision = Objects.requireNonNull(configRevision, "configRevision");
        this.quoteStore = Objects.requireNonNull(quoteStore, "quoteStore");
        this.eligibility = Objects.requireNonNull(eligibility, "eligibility");
        this.deliveryGateway = Objects.requireNonNull(deliveryGateway, "deliveryGateway");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (configRevision.isBlank()) {
            throw new IllegalArgumentException("Config revision is required");
        }
    }

    public CompletionStage<QuoteResult> quote(QuoteRequest request) {
        if (request == null) {
            return CompletableFuture.completedFuture(
                QuoteResult.rejected("INVALID_REQUEST")
            );
        }
        SnapshotCapture capture = new SnapshotCapture();
        CompletionStage<Void> snapshotStage;
        try {
            snapshotStage = tasks.mainThread(() ->
                capture.snapshot = eligibility.snapshot(request.playerUuid())
            );
        } catch (RuntimeException failure) {
            return CompletableFuture.completedFuture(
                QuoteResult.unavailable("ELIGIBILITY_UNAVAILABLE")
            );
        }
        return snapshotStage.thenCompose(ignored -> {
            ReissueEligibilitySnapshot snapshot = capture.snapshot;
            String rejection = snapshotRejection(snapshot, request.playerUuid());
            if (rejection != null) {
                return CompletableFuture.completedFuture(QuoteResult.rejected(rejection));
            }
            return tasks.database(() -> evaluateQuote(request.playerUuid()))
                .thenCompose(evaluation -> {
                    if (evaluation.failureCode() != null) {
                        return CompletableFuture.completedFuture(
                            QuoteResult.rejected(evaluation.failureCode())
                        );
                    }
                    QuoteCapture quoteCapture = new QuoteCapture();
                    return tasks.mainThread(() -> {
                        Instant issuedAt = clock.instant();
                        quoteCapture.quote = quoteStore.issue(new ReissueQuote(
                            UUID.randomUUID(),
                            request.playerUuid(),
                            evaluation.tool().toolId(),
                            evaluation.evolutionCount(),
                            configRevision,
                            evaluation.tool().itemInstanceId(),
                            evaluation.tool().instanceEpoch(),
                            evaluation.tool().deliveryStatus(),
                            evaluation.amountWaymark(),
                            issuedAt.plus(QUOTE_TTL),
                            new io.github.eariver.wayfarer.common.SingleUseGate()
                        ));
                    }).thenApply(completed -> QuoteResult.issued(quoteCapture.quote));
                });
        }).exceptionally(ignored -> QuoteResult.unavailable("REISSUE_UNAVAILABLE"));
    }

    public CompletionStage<Result> confirm(ConfirmRequest request) {
        if (request == null) {
            return CompletableFuture.completedFuture(Result.rejected("INVALID_REQUEST"));
        }
        if (!confirmInFlight.add(request.playerUuid())) {
            return CompletableFuture.completedFuture(Result.rejected("IN_FLIGHT"));
        }
        SnapshotCapture capture = new SnapshotCapture();
        CompletionStage<Void> snapshotStage;
        try {
            snapshotStage = tasks.mainThread(() -> {
                capture.snapshot = eligibility.snapshot(request.playerUuid());
                capture.quote = quoteStore.consume(request.playerUuid(), clock.instant())
                    .orElse(null);
            });
        } catch (RuntimeException failure) {
            confirmInFlight.remove(request.playerUuid());
            return CompletableFuture.completedFuture(
                Result.unavailable("ELIGIBILITY_UNAVAILABLE")
            );
        }
        CompletionStage<Result> result = snapshotStage.thenCompose(ignored -> {
            ReissueQuote quote = capture.quote;
            if (quote == null) {
                return CompletableFuture.completedFuture(Result.rejected("QUOTE_EXPIRED"));
            }
            String rejection = snapshotRejection(capture.snapshot, request.playerUuid());
            if (rejection != null) {
                return CompletableFuture.completedFuture(Result.rejected(rejection));
            }
            return tasks.database(() -> prepareFromQuote(request.playerUuid(), quote))
                .thenCompose(prepared -> {
                    if (prepared.failureCode() != null) {
                        return CompletableFuture.completedFuture(
                            Result.rejected(prepared.failureCode())
                        );
                    }
                    PrepareOutcome outcome = prepared.outcome();
                    if (outcome.result() == PrepareResult.IN_FLIGHT) {
                        return CompletableFuture.completedFuture(Result.rejected("IN_FLIGHT"));
                    }
                    return continueOperation(outcome.operation());
                });
        }).exceptionally(ignored -> Result.unavailable("REISSUE_UNAVAILABLE"));
        return result.handle((value, failure) -> {
            confirmInFlight.remove(request.playerUuid());
            return failure == null ? value : Result.unavailable("REISSUE_UNAVAILABLE");
        });
    }

    /** Resumes only the post-payment rotation path; it never calls execute(). */
    public CompletionStage<Result> resumeRotation(UUID reissueId) {
        Objects.requireNonNull(reissueId, "reissueId");
        return tasks.database(() -> operations.find(reissueId)).thenCompose(found -> {
            if (found.isEmpty()) {
                return CompletableFuture.completedFuture(Result.rejected("OPERATION_NOT_FOUND"));
            }
            return switch (found.orElseThrow().state()) {
                case PAYMENT_COMMITTED -> rotateAndCheckpoint(found.orElseThrow());
                case PENDING_DELIVERY -> audited(
                    Result.pending(found.orElseThrow()),
                    "GROWTH_TOOL_REISSUE_PENDING",
                    found.orElseThrow()
                );
                case DELIVERED -> audited(
                    Result.delivered(found.orElseThrow()),
                    "GROWTH_TOOL_REISSUE_DELIVERED",
                    found.orElseThrow()
                );
                case UNKNOWN -> audited(
                    Result.unknown(found.orElseThrow(),
                        found.orElseThrow().failureCode() == null
                            ? "RECONCILE_REQUIRED"
                            : found.orElseThrow().failureCode()),
                    "GROWTH_TOOL_REISSUE_UNKNOWN",
                    found.orElseThrow()
                );
                case FAILED -> audited(
                    Result.failed(found.orElseThrow(), failureCode(found.orElseThrow(), "REISSUE_FAILED")),
                    "GROWTH_TOOL_REISSUE_FAILED",
                    found.orElseThrow()
                );
                case ABANDONED -> audited(
                    Result.failed(found.orElseThrow(), "REISSUE_ABANDONED"),
                    "GROWTH_TOOL_REISSUE_ABANDONED",
                    found.orElseThrow()
                );
                case PREPARED, PAYMENT_PENDING -> CompletableFuture.completedFuture(
                    Result.unknown(found.orElseThrow(), "PAYMENT_RECONCILE_REQUIRED")
                );
            };
        }).exceptionally(ignored -> Result.unavailable("REISSUE_UNAVAILABLE"));
    }

    /**
     * Explicit admin-only recovery for a payment-committed UNKNOWN operation.
     * This path never calls transactions.execute().
     */
    public CompletionStage<Result> resumeRotationFromUnknown(UUID reissueId) {
        Objects.requireNonNull(reissueId, "reissueId");
        return tasks.database(() -> operations.find(reissueId)).thenCompose(found -> {
            if (found.isEmpty()) {
                return CompletableFuture.completedFuture(Result.rejected("OPERATION_NOT_FOUND"));
            }
            ReissueOperation operation = found.orElseThrow();
            if (operation.state() != ReissueOperation.State.UNKNOWN) {
                return durableResult(operation);
            }
            if (operation.transactionId() == null) {
                return CompletableFuture.completedFuture(
                    Result.rejected("RESUME_PAYMENT_REQUIRED")
                );
            }
            if (operation.paymentCommittedAt() == null) {
                return CompletableFuture.completedFuture(
                    Result.rejected("PAYMENT_CONFIRMATION_REQUIRED")
                );
            }
            return tasks.database(() -> operations.reopenToPaymentCommitted(
                operation.reissueId(),
                operation.lockVersion(),
                clock.instant()
            )).thenCompose(reopened -> reopened
                .map(value -> resumeRotation(value.reissueId()))
                .orElseGet(() -> durableAfterCas(
                    operation,
                    "ROTATION_RESUME_CONFLICT"
                )));
        }).exceptionally(ignored -> Result.unavailable("REISSUE_UNAVAILABLE"));
    }

    /** Explicit admin path. It may start a new debit only after explicit resume. */
    public CompletionStage<Result> resumePayment(UUID reissueId) {
        Objects.requireNonNull(reissueId, "reissueId");
        if (!resumePaymentInFlight.add(reissueId)) {
            return CompletableFuture.completedFuture(Result.rejected("IN_FLIGHT"));
        }
        CompletionStage<Result> result = tasks.database(() -> operations.find(reissueId))
            .thenCompose(found -> {
                if (found.isEmpty()) {
                    return CompletableFuture.completedFuture(Result.rejected("OPERATION_NOT_FOUND"));
                }
                ReissueOperation operation = found.orElseThrow();
                if (operation.state() == ReissueOperation.State.UNKNOWN) {
                    if (operation.transactionId() != null
                        || operation.paymentCommittedAt() != null) {
                        return CompletableFuture.completedFuture(
                            Result.rejected("WRONG_PAYMENT_PHASE")
                        );
                    }
                    return tasks.database(() -> operations.reopenPayment(
                        operation.reissueId(),
                        operation.lockVersion(),
                        clock.instant()
                    )).thenCompose(reopened -> reopened
                        .map(this::pay)
                        .orElseGet(() -> CompletableFuture.completedFuture(
                            Result.unknown(operation, "PAYMENT_REOPEN_CONFLICT")
                        )));
                }
                if (operation.state() == ReissueOperation.State.PAYMENT_PENDING
                    && operation.transactionId() == null
                    && operation.paymentCommittedAt() == null) {
                    return pay(operation);
                }
                return CompletableFuture.completedFuture(
                    Result.rejected("WRONG_PAYMENT_PHASE")
                );
            }).exceptionally(ignored -> Result.unavailable("REISSUE_UNAVAILABLE"));
        return result.handle((value, failure) -> {
            resumePaymentInFlight.remove(reissueId);
            return failure == null ? value : Result.unavailable("REISSUE_UNAVAILABLE");
        });
    }

    /** Confirms a saved Core transaction and resumes rotation without execute(). */
    public CompletionStage<Result> confirmPaymentAndResumeRotation(UUID reissueId) {
        Objects.requireNonNull(reissueId, "reissueId");
        return tasks.database(() -> operations.find(reissueId)).thenCompose(found -> {
            if (found.isEmpty()) {
                return CompletableFuture.completedFuture(Result.rejected("OPERATION_NOT_FOUND"));
            }
            ReissueOperation operation = found.orElseThrow();
            if (operation.state() != ReissueOperation.State.UNKNOWN) {
                return resumeRotation(reissueId);
            }
            if (operation.transactionId() == null) {
                return CompletableFuture.completedFuture(
                    Result.rejected("RESUME_PAYMENT_REQUIRED")
                );
            }
            if (operation.paymentCommittedAt() != null) {
                return CompletableFuture.completedFuture(Result.rejected("WRONG_PHASE"));
            }
            CompletionStage<WayfarerTransactions.TransactionDetails> inspected;
            try {
                inspected = transactions.inspect(operation.transactionId());
            } catch (RuntimeException failure) {
                return CompletableFuture.completedFuture(
                    Result.unavailable("CORE_INSPECT_UNAVAILABLE")
                );
            }
            if (inspected == null) {
                return CompletableFuture.completedFuture(
                    Result.unavailable("CORE_INSPECT_UNAVAILABLE")
                );
            }
            return inspected.handle((details, failure) ->
                failure == null ? details : null
            ).thenCompose(details -> confirmInspectedPayment(operation, details));
        }).exceptionally(ignored -> Result.unavailable("CORE_INSPECT_UNAVAILABLE"));
    }

    public CompletionStage<Result> failByAdmin(UUID reissueId, String failureCode) {
        Objects.requireNonNull(reissueId, "reissueId");
        String code = sanitizeFailure(failureCode, "ADMIN_FAILED");
        return tasks.database(() -> operations.find(reissueId)).thenCompose(found -> {
            if (found.isEmpty()) {
                return CompletableFuture.completedFuture(Result.rejected("OPERATION_NOT_FOUND"));
            }
            ReissueOperation operation = found.orElseThrow();
            if (operation.state() != ReissueOperation.State.UNKNOWN
                || operation.paymentCommittedAt() != null) {
                return CompletableFuture.completedFuture(
                    Result.unknown(operation, "WRONG_FAILURE_PHASE")
                );
            }
            return tasks.database(() -> operations.failFromUnknown(
                operation.reissueId(),
                operation.lockVersion(),
                code,
                clock.instant()
            )).thenCompose(saved -> saved
                .map(value -> audited(
                    Result.failed(value, code),
                    "GROWTH_TOOL_REISSUE_FAILED",
                    value
                ))
                .orElseGet(() -> CompletableFuture.completedFuture(
                    Result.unknown(operation, "FAILURE_COMMIT_UNKNOWN")
                )));
        }).exceptionally(ignored -> Result.unavailable("REISSUE_UNAVAILABLE"));
    }

    /** Performs only safe restart actions; it never debits automatically. */
    public CompletionStage<Integer> recoverAfterRestart() {
        return tasks.database(operations::findRecoveryCandidates)
            .thenCompose(this::recoverCandidates);
    }

    private CompletionStage<Integer> recoverCandidates(List<ReissueOperation> candidates) {
        CompletionStage<Integer> chain = CompletableFuture.completedFuture(0);
        for (ReissueOperation operation : candidates) {
            chain = chain.thenCompose(count -> recoverCandidate(operation)
                .thenApply(changed -> count + (changed ? 1 : 0)));
        }
        return chain;
    }

    private CompletionStage<Boolean> recoverCandidate(ReissueOperation operation) {
        return switch (operation.state()) {
            case PREPARED -> tasks.database(() -> operations.abandoned(
                operation.reissueId(),
                operation.lockVersion(),
                clock.instant()
            )).thenCompose(saved -> saved
                .map(value -> audited(
                    Result.failed(value, "REISSUE_ABANDONED"),
                    "GROWTH_TOOL_REISSUE_ABANDONED",
                    value
                ).thenApply(ignored -> true))
                .orElseGet(() -> CompletableFuture.completedFuture(false)));
            case PAYMENT_COMMITTED -> resumeRotation(operation.reissueId())
                .thenApply(result -> result != null
                    && (result.status() == Status.PENDING
                        || result.status() == Status.DELIVERED));
            case PENDING_DELIVERY -> tasks.database(() ->
                growthTools.findByOwner(operation.playerUuid())
            ).thenCompose(tool -> {
                if (tool.isEmpty() || tool.orElseThrow().deliveryStatus()
                    != GrowthTool.DeliveryStatus.DELIVERED) {
                    return CompletableFuture.completedFuture(false);
                }
                return tasks.database(() -> operations.delivered(
                    operation.reissueId(),
                    operation.lockVersion(),
                    clock.instant()
                ));
            });
            case PAYMENT_PENDING, UNKNOWN, DELIVERED, FAILED, ABANDONED ->
                CompletableFuture.completedFuture(false);
        };
    }

    private CompletionStage<Result> continueOperation(ReissueOperation operation) {
        return switch (operation.state()) {
            case PREPARED -> claimAndPay(operation);
            case PAYMENT_PENDING -> CompletableFuture.completedFuture(
                Result.unknown(operation, "PAYMENT_RECONCILE_REQUIRED")
            );
            case PAYMENT_COMMITTED -> resumeRotation(operation.reissueId());
            case PENDING_DELIVERY -> audited(
                Result.pending(operation),
                "GROWTH_TOOL_REISSUE_PENDING",
                operation
            );
            case DELIVERED -> audited(
                Result.delivered(operation),
                "GROWTH_TOOL_REISSUE_DELIVERED",
                operation
            );
            case FAILED -> audited(
                Result.failed(operation, failureCode(operation, "REISSUE_FAILED")),
                "GROWTH_TOOL_REISSUE_FAILED",
                operation
            );
            case ABANDONED -> audited(
                Result.failed(operation, "REISSUE_ABANDONED"),
                "GROWTH_TOOL_REISSUE_ABANDONED",
                operation
            );
            case UNKNOWN -> audited(
                Result.unknown(operation,
                    failureCode(operation, "RECONCILE_REQUIRED")),
                "GROWTH_TOOL_REISSUE_UNKNOWN",
                operation
            );
        };
    }

    private CompletionStage<Result> claimAndPay(ReissueOperation operation) {
        CompletionStage<Optional<ReissueOperation>> claimed;
        try {
            claimed = tasks.database(() -> operations.claimPayment(
                operation.reissueId(),
                operation.lockVersion(),
                clock.instant()
            ));
        } catch (RuntimeException failure) {
            return markUnknown(operation, null, "PAYMENT_CLAIM_UNKNOWN");
        }
        return claimed.thenCompose(value -> value
            .map(this::pay)
            .orElseGet(() -> durableAfterCas(operation, "PAYMENT_ALREADY_CLAIMED"))
        ).exceptionallyCompose(failure -> markUnknown(
            operation,
            null,
            "PAYMENT_CLAIM_UNKNOWN"
        ));
    }

    private CompletionStage<Result> pay(ReissueOperation claimed) {
        WayfarerTransactions.TransactionRequest payment = new WayfarerTransactions.TransactionRequest(
            coreIdempotencyKey(claimed),
            TRANSACTION_TYPE,
            claimed.playerUuid(),
            SUBJECT_TYPE,
            claimed.toolId().toString(),
            claimed.amountWaymark(),
            payloadFor(claimed.reissueId())
        );
        CompletionStage<WayfarerTransactions.TransactionResult> stage;
        try {
            stage = transactions.execute(payment);
        } catch (RuntimeException failure) {
            return markUnknown(claimed, null, "TRANSACTION_UNAVAILABLE");
        }
        if (stage == null) {
            return markUnknown(claimed, null, "TRANSACTION_UNAVAILABLE");
        }
        return stage.handle((result, failure) -> failure == null ? result : null)
            .thenCompose(result -> paymentResult(claimed, result));
    }

    private CompletionStage<Result> paymentResult(
        ReissueOperation claimed,
        WayfarerTransactions.TransactionResult transaction
    ) {
        if (transaction == null || transaction.state() == null) {
            return markUnknown(claimed, null, "PAYMENT_UNKNOWN");
        }
        UUID transactionId = transaction.transactionId();
        if (transaction.state() == WayfarerTransactions.State.COMMITTED
            || transaction.state() == WayfarerTransactions.State.RECONCILED_COMMITTED) {
            if (transactionId == null) {
                return markUnknown(claimed, null, "PAYMENT_COMMIT_UNKNOWN");
            }
            return savePaymentCommitted(claimed, transactionId);
        }
        if (transaction.state() == WayfarerTransactions.State.FAILED
            || transaction.state() == WayfarerTransactions.State.REFUNDED
            || transaction.state() == WayfarerTransactions.State.RECONCILED_REFUNDED) {
            String code = sanitizeFailure(transaction.failureCode(), "PAYMENT_FAILED");
            return saveFailed(claimed, transactionId, code);
        }
        String code = transaction.state() == WayfarerTransactions.State.UNKNOWN
            ? "PAYMENT_UNKNOWN"
            : "PAYMENT_IN_FLIGHT";
        return markUnknown(claimed, transactionId, code);
    }

    private CompletionStage<Result> savePaymentCommitted(
        ReissueOperation claimed,
        UUID transactionId
    ) {
        CompletionStage<Optional<ReissueOperation>> saved;
        try {
            saved = tasks.database(() -> operations.paymentCommitted(
                claimed.reissueId(),
                transactionId,
                claimed.lockVersion(),
                clock.instant()
            ));
        } catch (RuntimeException failure) {
            return markUnknown(claimed, transactionId, "PAYMENT_COMMIT_UNKNOWN");
        }
        return saved.thenCompose(value -> value
            .map(operation -> resumeRotation(operation.reissueId()))
            .orElseGet(() -> resolveCasConflict(
                claimed,
                transactionId,
                "PAYMENT_COMMIT_UNKNOWN"
            ))
        ).exceptionallyCompose(failure -> markUnknown(
            claimed,
            transactionId,
            "PAYMENT_COMMIT_UNKNOWN"
        ));
    }

    private CompletionStage<Result> saveFailed(
        ReissueOperation claimed,
        UUID transactionId,
        String failureCode
    ) {
        CompletionStage<Optional<ReissueOperation>> saved;
        try {
            saved = tasks.database(() -> operations.failed(
                claimed.reissueId(),
                transactionId,
                claimed.lockVersion(),
                failureCode,
                clock.instant()
            ));
        } catch (RuntimeException failure) {
            return markUnknown(claimed, transactionId, "PAYMENT_FAILURE_COMMIT_UNKNOWN");
        }
        return saved.thenCompose(value -> value
            .map(operation -> audited(
                Result.failed(operation, failureCode),
                "GROWTH_TOOL_REISSUE_FAILED",
                operation
            ))
            .orElseGet(() -> resolveCasConflict(
                claimed,
                transactionId,
                "PAYMENT_FAILURE_COMMIT_UNKNOWN"
            ))
        ).exceptionallyCompose(failure -> markUnknown(
            claimed,
            transactionId,
            "PAYMENT_FAILURE_COMMIT_UNKNOWN"
        ));
    }

    private CompletionStage<Result> rotateAndCheckpoint(ReissueOperation operation) {
        CompletionStage<RotationDecision> rotation;
        try {
            rotation = tasks.database(() -> rotate(operation));
        } catch (RuntimeException failure) {
            return markUnknown(operation, operation.transactionId(), "ROTATION_CONFLICT_UNKNOWN");
        }
        return rotation.thenCompose(decision -> {
            if (decision.failureCode() != null) {
                return markUnknown(operation, operation.transactionId(), decision.failureCode());
            }
            return tasks.database(() -> operations.pendingDelivery(
                operation.reissueId(),
                operation.lockVersion(),
                clock.instant()
            )).thenCompose(pending -> pending
                .map(value -> deliver(value, decision.tool()))
                .orElseGet(() -> resolveRotationCheckpoint(operation, decision.tool())
                )
            );
        }).exceptionallyCompose(failure -> markUnknown(
            operation,
            operation.transactionId(),
            "ROTATION_COMMIT_UNKNOWN"
        ));
    }

    private RotationDecision rotate(ReissueOperation operation) {
        Optional<GrowthTool> found = growthTools.findByOwner(operation.playerUuid());
        if (found.isEmpty()) {
            return RotationDecision.failure("TOOL_NOT_FOUND");
        }
        GrowthTool current = found.orElseThrow();
        if (alreadyRotated(current, operation)) {
            return RotationDecision.success(current);
        }
        if (!operation.expectedItemInstanceId().equals(current.itemInstanceId())
            || operation.instanceEpoch() != current.instanceEpoch()
            || current.status() == GrowthTool.Status.REVOKED) {
            return RotationDecision.failure("ROTATION_CONFLICT_UNKNOWN");
        }
        GrowthTool next = current.reissued(operation.newItemInstanceId(), clock.instant());
        Optional<GrowthTool> replaced = growthTools.replaceAuthority(
            next,
            current.lockVersion(),
            clock.instant()
        );
        if (replaced.isPresent()) {
            return RotationDecision.success(replaced.orElseThrow());
        }
        Optional<GrowthTool> afterConflict = growthTools.findByOwner(operation.playerUuid());
        return afterConflict.isPresent() && alreadyRotated(afterConflict.orElseThrow(), operation)
            ? RotationDecision.success(afterConflict.orElseThrow())
            : RotationDecision.failure("ROTATION_CONFLICT_UNKNOWN");
    }

    private CompletionStage<Result> deliver(
        ReissueOperation pending,
        GrowthTool rotated
    ) {
        DeliveryCapture capture = new DeliveryCapture();
        CompletionStage<Void> deliveryStage;
        try {
            deliveryStage = tasks.mainThread(() ->
                capture.outcome = deliveryGateway.deliverReissued(rotated)
            );
        } catch (RuntimeException failure) {
            return pendingResult(pending, "UNAVAILABLE");
        }
        return deliveryStage.handle((ignored, failure) ->
            failure == null && capture.outcome != null
                ? capture.outcome
                : DeliveryOutcome.UNAVAILABLE
        ).thenCompose(outcome -> {
            if (outcome != DeliveryOutcome.DELIVERED
                && outcome != DeliveryOutcome.ALREADY_PRESENT) {
                return pendingResult(pending, outcome.name());
            }
            return tasks.database(() -> growthTools.markDelivered(
                rotated.toolId(),
                rotated.lockVersion(),
                clock.instant()
            )).thenCompose(marked -> {
                if (!marked) {
                    return tasks.database(() -> growthTools.findByOwner(rotated.ownerUuid()))
                        .thenCompose(current -> current.isPresent()
                            && current.orElseThrow().deliveryStatus()
                                == GrowthTool.DeliveryStatus.DELIVERED
                            ? closeDelivered(pending, outcome == DeliveryOutcome.DELIVERED)
                            : pendingResult(pending, "DELIVERY_CONFLICT"));
                }
                return closeDelivered(pending, outcome == DeliveryOutcome.DELIVERED);
            });
        }).exceptionallyCompose(failure -> pendingResult(pending, "UNAVAILABLE"));
    }

    private CompletionStage<Result> closeDelivered(
        ReissueOperation pending,
        boolean physicalInserted
    ) {
        return tasks.database(() -> operations.delivered(
            pending.reissueId(),
            pending.lockVersion(),
            clock.instant()
        )).thenCompose(closed -> {
            CompletionStage<Result> result;
            if (closed) {
                result = audited(
                    Result.delivered(pending),
                    "GROWTH_TOOL_REISSUE_DELIVERED",
                    pending
                );
            } else {
                result = tasks.database(() -> operations.find(pending.reissueId()))
                    .thenCompose(found -> found.isPresent()
                    && found.orElseThrow().state() == ReissueOperation.State.DELIVERED
                    ? audited(
                        Result.delivered(found.orElseThrow()),
                        "GROWTH_TOOL_REISSUE_DELIVERED",
                        found.orElseThrow()
                    )
                    : pendingResult(pending, "DELIVERY_COMMIT_UNKNOWN"));
            }
            if (!physicalInserted) {
                return result;
            }
            return result.thenCompose(value ->
                tasks.mainThread(() ->
                    deliveryGateway.notifyReissueDelivered(pending.playerUuid())
                ).thenApply(ignored -> value)
            );
        });
    }

    private CompletionStage<Result> pendingResult(
        ReissueOperation operation,
        String outcome
    ) {
        return audited(
            Result.pending(operation, sanitizeFailure(outcome, "DELIVERY_PENDING")),
            "GROWTH_TOOL_REISSUE_PENDING",
            operation
        );
    }

    private CompletionStage<Result> resolveRotationCheckpoint(
        ReissueOperation operation,
        GrowthTool rotated
    ) {
        return tasks.database(() -> operations.find(operation.reissueId()))
            .thenCompose(found -> {
                if (found.isEmpty()) {
                    return CompletableFuture.completedFuture(
                        Result.unknown(operation, "ROTATION_COMMIT_UNKNOWN")
                    );
                }
                ReissueOperation current = found.orElseThrow();
                if (current.state() == ReissueOperation.State.PENDING_DELIVERY) {
                    return deliver(current, rotated);
                }
                if (current.state() == ReissueOperation.State.DELIVERED) {
                    return CompletableFuture.completedFuture(Result.delivered(current));
                }
                return markUnknown(current, current.transactionId(), "ROTATION_COMMIT_UNKNOWN");
            });
    }

    private CompletionStage<Result> confirmInspectedPayment(
        ReissueOperation operation,
        WayfarerTransactions.TransactionDetails details
    ) {
        if (details == null) {
            return CompletableFuture.completedFuture(
                Result.unavailable("CORE_INSPECT_UNAVAILABLE")
            );
        }
        if (!transactionDetailsMatch(operation, details)) {
            return CompletableFuture.completedFuture(
                Result.unknown(operation, "TRANSACTION_DETAILS_MISMATCH")
            );
        }
        if (details.state() == WayfarerTransactions.State.COMMITTED
            || details.state() == WayfarerTransactions.State.RECONCILED_COMMITTED) {
            return tasks.database(() -> operations.confirmPaymentCommittedFromUnknown(
                operation.reissueId(),
                operation.transactionId(),
                operation.lockVersion(),
                clock.instant(),
                clock.instant()
            )).thenCompose(saved -> saved
                .map(value -> resumeRotation(value.reissueId()))
                .orElseGet(() -> resolveConfirmationConflict(operation))
            );
        }
        if (details.state() == WayfarerTransactions.State.FAILED
            || details.state() == WayfarerTransactions.State.REFUNDED
            || details.state() == WayfarerTransactions.State.RECONCILED_REFUNDED) {
            return CompletableFuture.completedFuture(
                Result.unknown(operation, "CORE_PAYMENT_FAILED")
            );
        }
        String code = details.state() == WayfarerTransactions.State.UNKNOWN
            ? "PAYMENT_UNKNOWN"
            : "PAYMENT_IN_FLIGHT";
        return CompletableFuture.completedFuture(Result.unknown(operation, code));
    }

    private CompletionStage<Result> resolveConfirmationConflict(ReissueOperation operation) {
        return tasks.database(() -> operations.find(operation.reissueId())).thenCompose(found -> {
            if (found.isEmpty()) {
                return CompletableFuture.completedFuture(
                    Result.unknown(operation, "PAYMENT_CONFIRM_CONFLICT")
                );
            }
            ReissueOperation current = found.orElseThrow();
            if (!Objects.equals(current.transactionId(), operation.transactionId())) {
                return CompletableFuture.completedFuture(
                    Result.unknown(current, "TRANSACTION_ID_CONFLICT")
                );
            }
            return switch (current.state()) {
                case PAYMENT_COMMITTED -> resumeRotation(current.reissueId());
                case PENDING_DELIVERY -> CompletableFuture.completedFuture(Result.pending(current));
                case DELIVERED -> CompletableFuture.completedFuture(Result.delivered(current));
                default -> CompletableFuture.completedFuture(
                    Result.unknown(current, "PAYMENT_CONFIRM_CONFLICT")
                );
            };
        });
    }

    private CompletionStage<Result> markUnknown(
        ReissueOperation operation,
        UUID transactionId,
        String failureCode
    ) {
        String code = sanitizeFailure(failureCode, "REISSUE_UNKNOWN");
        CompletionStage<Optional<ReissueOperation>> saved;
        try {
            saved = tasks.database(() -> operations.unknown(
                operation.reissueId(),
                operation.lockVersion(),
                transactionId,
                code,
                clock.instant()
            ));
        } catch (RuntimeException failure) {
            return audited(
                Result.unknown(operation, code),
                "GROWTH_TOOL_REISSUE_UNKNOWN",
                operation
            );
        }
        return saved.thenCompose(value -> value
            .map(updated -> audited(
                Result.unknown(updated, code),
                "GROWTH_TOOL_REISSUE_UNKNOWN",
                updated
            ))
            .orElseGet(() -> resolveCasConflict(operation, transactionId, code))
        ).exceptionally(ignored -> Result.unknown(operation, code));
    }

    private CompletionStage<Result> resolveCasConflict(
        ReissueOperation operation,
        UUID receivedTransactionId,
        String fallbackCode
    ) {
        return tasks.database(() -> operations.find(operation.reissueId()))
            .thenCompose(found -> {
                if (found.isEmpty()) {
                    return CompletableFuture.completedFuture(
                        Result.unknown(operation, fallbackCode)
                    );
                }
                ReissueOperation current = found.orElseThrow();
                if (receivedTransactionId != null
                    && current.transactionId() != null
                    && !receivedTransactionId.equals(current.transactionId())) {
                    return markTransactionConflict(current);
                }
                return durableResult(current);
            }).exceptionally(ignored -> Result.unknown(operation, fallbackCode));
    }

    private CompletionStage<Result> markTransactionConflict(ReissueOperation operation) {
        return tasks.database(() -> operations.unknown(
            operation.reissueId(),
            operation.lockVersion(),
            null,
            "TRANSACTION_ID_CONFLICT",
            clock.instant()
        )).thenCompose(saved -> saved
            .map(value -> CompletableFuture.completedFuture(
                Result.unknown(value, "TRANSACTION_ID_CONFLICT")
            ))
            .orElseGet(() -> CompletableFuture.completedFuture(
                Result.unknown(operation, "TRANSACTION_ID_CONFLICT")
            ))
        ).exceptionally(ignored -> Result.unknown(
            operation,
            "TRANSACTION_ID_CONFLICT"
        ));
    }

    private CompletionStage<Result> durableAfterCas(
        ReissueOperation operation,
        String fallbackCode
    ) {
        return tasks.database(() -> operations.find(operation.reissueId()))
            .thenCompose(found -> found.map(this::durableResult)
                .orElseGet(() -> CompletableFuture.completedFuture(
                    Result.unknown(operation, fallbackCode)
                )));
    }

    private CompletionStage<Result> durableResult(ReissueOperation operation) {
        return switch (operation.state()) {
            case PAYMENT_COMMITTED -> resumeRotation(operation.reissueId());
            case PENDING_DELIVERY -> CompletableFuture.completedFuture(Result.pending(operation));
            case DELIVERED -> CompletableFuture.completedFuture(Result.delivered(operation));
            case FAILED, ABANDONED -> CompletableFuture.completedFuture(
                Result.failed(operation, failureCode(operation, "REISSUE_FAILED"))
            );
            case UNKNOWN, PREPARED, PAYMENT_PENDING -> CompletableFuture.completedFuture(
                Result.unknown(operation, failureCode(operation, "RECONCILE_REQUIRED"))
            );
        };
    }

    private CompletionStage<Result> audited(
        Result result,
        String eventType,
        ReissueOperation operation
    ) {
        CompletionStage<Void> recorded;
        try {
            recorded = audit.record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                eventType,
                operation.playerUuid(),
                SUBJECT_TYPE,
                operation.toolId().toString(),
                serverId,
                auditDetails(result),
                clock.instant()
            ));
        } catch (RuntimeException failure) {
            return CompletableFuture.completedFuture(result);
        }
        if (recorded == null) {
            return CompletableFuture.completedFuture(result);
        }
        return recorded.handle((ignored, failure) -> result);
    }

    private static String auditDetails(Result result) {
        String failure = result.failureCode();
        String safeFailure = failure == null ? "" : sanitizeFailure(failure, "UNKNOWN");
        return "{\"status\":\"" + result.status().name()
            + "\",\"failure_code\":\"" + safeFailure + "\"}";
    }

    private QuoteEvaluation evaluateQuote(UUID playerUuid) {
        Optional<GrowthTool> found = growthTools.findByOwner(playerUuid);
        if (found.isEmpty()) {
            return QuoteEvaluation.failure("TOOL_NOT_FOUND");
        }
        GrowthTool tool = found.orElseThrow();
        if (tool.status() == GrowthTool.Status.REVOKED) {
            return QuoteEvaluation.failure("TOOL_REVOKED");
        }
        if (tool.deliveryStatus() == GrowthTool.DeliveryStatus.PENDING) {
            return QuoteEvaluation.failure("DELIVERY_PENDING");
        }
        if (operations.findActiveByTool(tool.toolId()).isPresent()) {
            return QuoteEvaluation.failure("IN_FLIGHT");
        }
        int evolutionCount = evolutionCount(tool);
        long amount;
        try {
            RepairPricingQuote quote = new RepairPricingQuote(pricing.quote(evolutionCount));
            amount = quote.amountWaymark();
        } catch (ArithmeticException | IllegalArgumentException failure) {
            return QuoteEvaluation.failure("PRICING_INVALID");
        }
        return QuoteEvaluation.success(tool, evolutionCount, amount);
    }

    private PreparedEvaluation prepareFromQuote(UUID playerUuid, ReissueQuote quote) {
        QuoteEvaluation evaluation = evaluateQuote(playerUuid);
        if (evaluation.failureCode() != null) {
            return PreparedEvaluation.failure(evaluation.failureCode());
        }
        if (!quote.matchesSnapshot(
            evaluation.tool(),
            evaluation.evolutionCount(),
            configRevision,
            evaluation.amountWaymark(),
            clock.instant()
        )) {
            return PreparedEvaluation.failure("QUOTE_CHANGED");
        }
        ReissueOperation operation = new ReissueOperation(
            UUID.randomUUID(),
            "main-reissue:" + quote.quoteId(),
            playerUuid,
            evaluation.tool().toolId(),
            evaluation.tool().itemInstanceId(),
            UUID.randomUUID(),
            evaluation.tool().instanceEpoch(),
            evaluation.evolutionCount(),
            configRevision,
            evaluation.amountWaymark(),
            ReissueOperation.State.PREPARED,
            null,
            null,
            null,
            0
        );
        return PreparedEvaluation.success(operations.prepare(operation, clock.instant()));
    }

    private int evolutionCount(GrowthTool tool) {
        return evolutionPlan.evaluate(
            tool.cumulativeProgressUnits(),
            tool.branch(),
            enchantmentCaps
        ).evolutionCount();
    }

    private static String snapshotRejection(
        ReissueEligibilitySnapshot snapshot,
        UUID playerUuid
    ) {
        if (snapshot == null || !playerUuid.equals(snapshot.playerUuid())) {
            return "ELIGIBILITY_UNAVAILABLE";
        }
        if (!snapshot.playerOnline()) {
            return "PLAYER_OFFLINE";
        }
        if (!snapshot.worldAllowed()
            || !ReissueEligibilityPolicy.isAllowedWorld(snapshot.exactWorldName())) {
            return "WORLD_NOT_ALLOWED";
        }
        if (snapshot.currentItemPresent()) {
            return "CURRENT_ITEM_PRESENT";
        }
        return null;
    }

    private boolean transactionDetailsMatch(
        ReissueOperation operation,
        WayfarerTransactions.TransactionDetails details
    ) {
        return operation.transactionId().equals(details.transactionId())
            && matchesIfPresent(coreIdempotencyKey(operation), details.idempotencyKey())
            && matchesIfPresent(TRANSACTION_TYPE, details.transactionType())
            && matchesIfPresent(operation.playerUuid(), details.actorUuid())
            && matchesIfPresent(SUBJECT_TYPE, details.subjectType())
            && matchesIfPresent(operation.toolId().toString(), details.subjectId())
            && (details.amountWaymark() == 0
                || details.amountWaymark() == operation.amountWaymark());
    }

    private static boolean matchesIfPresent(Object expected, Object actual) {
        return actual == null || expected.equals(actual);
    }

    private static boolean alreadyRotated(
        GrowthTool current,
        ReissueOperation operation
    ) {
        return current.itemInstanceId().equals(operation.newItemInstanceId())
            && current.instanceEpoch() == operation.instanceEpoch() + 1
            && current.status() == GrowthTool.Status.ACTIVE;
    }

    private static String coreIdempotencyKey(ReissueOperation operation) {
        return "main-reissue:" + operation.idempotencyKey();
    }

    private static String payloadFor(UUID reissueId) {
        return "{\"reissue_id\":\"" + reissueId + "\"}";
    }

    private static String failureCode(ReissueOperation operation, String fallback) {
        return operation.failureCode() == null ? fallback : operation.failureCode();
    }

    private static String sanitizeFailure(String failureCode, String fallback) {
        return failureCode != null && failureCode.matches("[A-Z0-9_]{3,96}")
            ? failureCode
            : fallback;
    }

    public record QuoteResult(QuoteStatus status, ReissueQuote quote, String failureCode) {
        private static QuoteResult issued(ReissueQuote quote) {
            return new QuoteResult(QuoteStatus.ISSUED, quote, null);
        }

        private static QuoteResult rejected(String failureCode) {
            return new QuoteResult(QuoteStatus.REJECTED, null, failureCode);
        }

        private static QuoteResult unavailable(String failureCode) {
            return new QuoteResult(QuoteStatus.UNAVAILABLE, null, failureCode);
        }
    }

    public enum QuoteStatus {
        ISSUED,
        REJECTED,
        UNAVAILABLE
    }

    public record Result(
        Status status,
        UUID reissueId,
        UUID transactionId,
        String failureCode
    ) {
        private static Result delivered(ReissueOperation operation) {
            return new Result(Status.DELIVERED, operation.reissueId(), operation.transactionId(), null);
        }

        private static Result pending(ReissueOperation operation) {
            return new Result(Status.PENDING, operation.reissueId(), operation.transactionId(), null);
        }

        private static Result pending(ReissueOperation operation, String failureCode) {
            return new Result(
                Status.PENDING,
                operation.reissueId(),
                operation.transactionId(),
                failureCode
            );
        }

        private static Result failed(ReissueOperation operation, String failureCode) {
            return new Result(
                Status.FAILED,
                operation.reissueId(),
                operation.transactionId(),
                failureCode
            );
        }

        private static Result unknown(ReissueOperation operation, String failureCode) {
            return new Result(
                Status.UNKNOWN,
                operation.reissueId(),
                operation.transactionId(),
                failureCode
            );
        }

        private static Result rejected(String failureCode) {
            return new Result(Status.REJECTED, null, null, failureCode);
        }

        private static Result unavailable(String failureCode) {
            return new Result(Status.UNAVAILABLE, null, null, failureCode);
        }
    }

    public enum Status {
        DELIVERED,
        PENDING,
        FAILED,
        UNKNOWN,
        REJECTED,
        UNAVAILABLE
    }

    private record QuoteEvaluation(
        GrowthTool tool,
        int evolutionCount,
        long amountWaymark,
        String failureCode
    ) {
        private static QuoteEvaluation success(
            GrowthTool tool,
            int evolutionCount,
            long amountWaymark
        ) {
            return new QuoteEvaluation(tool, evolutionCount, amountWaymark, null);
        }

        private static QuoteEvaluation failure(String failureCode) {
            return new QuoteEvaluation(null, 0, 0, failureCode);
        }
    }

    private record PreparedEvaluation(
        PrepareOutcome outcome,
        String failureCode
    ) {
        private static PreparedEvaluation success(PrepareOutcome outcome) {
            return new PreparedEvaluation(outcome, null);
        }

        private static PreparedEvaluation failure(String failureCode) {
            return new PreparedEvaluation(null, failureCode);
        }
    }

    private record RotationDecision(GrowthTool tool, String failureCode) {
        private static RotationDecision success(GrowthTool tool) {
            return new RotationDecision(tool, null);
        }

        private static RotationDecision failure(String failureCode) {
            return new RotationDecision(null, failureCode);
        }
    }

    private static final class SnapshotCapture {
        private ReissueEligibilitySnapshot snapshot;
        private ReissueQuote quote;
    }

    private static final class QuoteCapture {
        private ReissueQuote quote;
    }

    private static final class DeliveryCapture {
        private DeliveryOutcome outcome;
    }

    private record RepairPricingQuote(long amountWaymark) {
        private RepairPricingQuote(io.github.eariver.wayfarer.main.domain.RepairPricing.Quote quote) {
            this(requireAvailable(quote));
        }

        private static long requireAvailable(
            io.github.eariver.wayfarer.main.domain.RepairPricing.Quote quote
        ) {
            if (!quote.available() || quote.amountWaymark() <= 0) {
                throw new ArithmeticException("Reissue quote is unavailable");
            }
            return quote.amountWaymark();
        }
    }
}
