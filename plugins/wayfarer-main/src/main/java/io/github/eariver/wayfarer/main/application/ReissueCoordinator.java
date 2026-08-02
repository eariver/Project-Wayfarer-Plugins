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
             ï¾¸¶‰žËkºwµçl(€€€€€€€€€€€ôì(€€€€€€€ô¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”½µÁ±•Ñ¥½¹MÑ…”ñI•ÍÕ±Ðøµ…É­U¹­¹½Ý¸ (€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°(€€€€€€€UU%ÑÉ…¹Í…Ñ¥½¹%°(€€€€€€€MÑÉ¥¹œ™…¥±ÕÉ•½‘”(€€€€¤ì(€€€€€€€MÑÉ¥¹œ½‘”€ôÍ…¹¥Ñ¥é•…¥±ÕÉ”¡™…¥±ÕÉ•½‘”°€‰I%MMU}U9-9=]8ˆ¤ì(€€€€€€€½µÁ±•Ñ¥½¹MÑ…”ñ=ÁÑ¥½¹…°ñI•¥ÍÍÕ•=Á•É…Ñ¥½¸øøÍ…Ù•ì(€€€€€€€ÑÉäì(€€€€€€€€€€€Í…Ù•€ôÑ…Í­Ì¹‘…Ñ…‰…Í”  ¤€´ø½Á•É…Ñ¥½¹Ì¹Õ¹­¹½Ý¸ (€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹±½­Y•ÉÍ¥½¸ ¤°(€€€€€€€€€€€€€€€ÑÉ…¹Í…Ñ¥½¹%°(€€€€€€€€€€€€€€€½‘”°(€€€€€€€€€€€€€€€±½¬¹¥¹ÍÑ…¹Ð ¤(€€€€€€€€€€€€¤¤ì(€€€€€€€ô…Ñ €¡IÕ¹Ñ¥µ•á•ÁÑ¥½¸™…¥±ÕÉ”¤ì(€€€€€€€€€€€É•ÑÕÉ¸…Õ‘¥Ñ• (€€€€€€€€€€€€€€€I•ÍÕ±Ð¹Õ¹­¹½Ý¸¡½Á•É…Ñ¥½¸°½‘”¤°(€€€€€€€€€€€€€€€€‰I=]Q!}Q==1}I%MMU}U9-9=]8ˆ°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸(€€€€€€€€€€€€¤ì(€€€€€€€ô(€€€€€€€É•ÑÕÉ¸Í…Ù•¹Ñ¡•¹½µÁ½Í”¡Ù…±Õ”€´øÙ…±Õ”(€€€€€€€€€€€€¹µ…À¡ÕÁ‘…Ñ•€´ø…Õ‘¥Ñ• (€€€€€€€€€€€€€€€I•ÍÕ±Ð¹Õ¹­¹½Ý¸¡ÕÁ‘…Ñ•°½‘”¤°(€€€€€€€€€€€€€€€€‰I=]Q!}Q==1}I%MMU}U9-9=]8ˆ°(€€€€€€€€€€€€€€€ÕÁ‘…Ñ•(€€€€€€€€€€€€¤¤(€€€€€€€€€€€€¹½É±Í••Ð  ¤€´øÉ•Í½±Ù•…Í½¹™±¥Ð¡½Á•É…Ñ¥½¸°ÑÉ…¹Í…Ñ¥½¹%°½‘”¤¤(€€€€€€€€¤¹•á•ÁÑ¥½¹…±±ä¡¥¹½É•€´øI•ÍÕ±Ð¹Õ¹­¹½Ý¸¡½Á•É…Ñ¥½¸°½‘”¤¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”½µÁ±•Ñ¥½¹MÑ…”ñI•ÍÕ±ÐøÉ•Í½±Ù•…Í½¹™±¥Ð (€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°(€€€€€€€UU%É••¥Ù•‘QÉ…¹Í…Ñ¥½¹%°(€€€€€€€MÑÉ¥¹œ™…±±‰…­½‘”(€€€€¤ì(€€€€€€€É•ÑÕÉ¸Ñ…Í­Ì¹‘…Ñ…‰…Í”  ¤€´ø½Á•É…Ñ¥½¹Ì¹™¥¹¡½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤¤¤(€€€€€€€€€€€€¹Ñ¡•¹½µÁ½Í”¡™½Õ¹€´øì(€€€€€€€€€€€€€€€¥˜€¡™½Õ¹¹¥ÍµÁÑä ¤¤ì(€€€€€€€€€€€€€€€€€€€É•ÑÕÉ¸½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ” (€€€€€€€€€€€€€€€€€€€€€€€I•ÍÕ±Ð¹Õ¹­¹½Ý¸¡½Á•É…Ñ¥½¸°™…±±‰…­½‘”¤(€€€€€€€€€€€€€€€€€€€€¤ì(€€€€€€€€€€€€€€€ô(€€€€€€€€€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸ÕÉÉ•¹Ð€ô™½Õ¹¹½É±Í•Q¡É½Ü ¤ì(€€€€€€€€€€€€€€€¥˜€¡É••¥Ù•‘QÉ…¹Í…Ñ¥½¹%€„ô¹Õ±°(€€€€€€€€€€€€€€€€€€€€˜˜ÕÉÉ•¹Ð¹ÑÉ…¹Í…Ñ¥½¹% ¤€„ô¹Õ±°(€€€€€€€€€€€€€€€€€€€€˜˜€…É••¥Ù•‘QÉ…¹Í…Ñ¥½¹%¹•ÅÕ…±Ì¡ÕÉÉ•¹Ð¹ÑÉ…¹Í…Ñ¥½¹% ¤¤¤ì(€€€€€€€€€€€€€€€€€€€É•ÑÕÉ¸µ…É­QÉ…¹Í…Ñ¥½¹½¹™±¥Ð¡ÕÉÉ•¹Ð¤ì(€€€€€€€€€€€€€€€ô(€€€€€€€€€€€€€€€É•ÑÕÉ¸‘ÕÉ…‰±•I•ÍÕ±Ð¡ÕÉÉ•¹Ð¤ì(€€€€€€€€€€€ô¤¹•á•ÁÑ¥½¹…±±ä¡¥¹½É•€´øI•ÍÕ±Ð¹Õ¹­¹½Ý¸¡½Á•É…Ñ¥½¸°™…±±‰…­½‘”¤¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”½µÁ±•Ñ¥½¹MÑ…”ñI•ÍÕ±Ðøµ…É­QÉ…¹Í…Ñ¥½¹½¹™±¥Ð¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸¤ì(€€€€€€€É•ÑÕÉ¸Ñ…Í­Ì¹‘…Ñ…‰…Í”  ¤€´ø½Á•É…Ñ¥½¹Ì¹Õ¹­¹½Ý¸ (€€€€€€€€€€€½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤°(€€€€€€€€€€€½Á•É…Ñ¥½¸¹±½­Y•ÉÍ¥½¸ ¤°(€€€€€€€€€€€¹Õ±°°(€€€€€€€€€€€€‰QI9MQ%=9}%}=91%Pˆ°(€€€€€€€€€€€±½¬¹¥¹ÍÑ…¹Ð ¤(€€€€€€€€¤¤¹Ñ¡•¹½µÁ½Í”¡Í…Ù•€´øÍ…Ù•(€€€€€€€€€€€€¹µ…À¡Ù…±Õ”€´ø½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ” (€€€€€€€€€€€€€€€I•ÍÕ±Ð¹Õ¹­¹½Ý¸¡Ù…±Õ”°€‰QI9MQ%=9}%}=91%Pˆ¤(€€€€€€€€€€€€¤¤(€€€€€€€€€€€€¹½É±Í••Ð  ¤€´ø½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ” (€€€€€€€€€€€€€€€I•ÍÕ±Ð¹Õ¹­¹½Ý¸¡½Á•É…Ñ¥½¸°€‰QI9MQ%=9}%}=91%Pˆ¤(€€€€€€€€€€€€¤¤(€€€€€€€€¤¹•á•ÁÑ¥½¹…±±ä¡¥¹½É•€´øI•ÍÕ±Ð¹Õ¹­¹½Ý¸ (€€€€€€€€€€€½Á•É…Ñ¥½¸°(€€€€€€€€€€€€‰QI9MQ%=9}%}=91%Pˆ(€€€€€€€€¤¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”½µÁ±•Ñ¥½¹MÑ…”ñI•ÍÕ±Ðø‘ÕÉ…‰±•™Ñ•É…Ì (€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°(€€€€€€€MÑÉ¥¹œ™…±±‰…­½‘”(€€€€¤ì(€€€€€€€É•ÑÕÉ¸Ñ…Í­Ì¹‘…Ñ…‰…Í”  ¤€´ø½Á•É…Ñ¥½¹Ì¹™¥¹¡½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤¤¤(€€€€€€€€€€€€¹Ñ¡•¹½µÁ½Í”¡™½Õ¹€´ø™½Õ¹¹µ…À¡Ñ¡¥Ìèé‘ÕÉ…‰±•I•ÍÕ±Ð¤(€€€€€€€€€€€€€€€€¹½É±Í••Ð  ¤€´ø½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ” (€€€€€€€€€€€€€€€€€€€I•ÍÕ±Ð¹Õ¹­¹½Ý¸¡½Á•É…Ñ¥½¸°™…±±‰…­½‘”¤(€€€€€€€€€€€€€€€€¤¤¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”½µÁ±•Ñ¥½¹MÑ…”ñI•ÍÕ±Ðø‘ÕÉ…‰±•I•ÍÕ±Ð¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸¤ì(€€€€€€€É•ÑÕÉ¸ÍÝ¥Ñ €¡½Á•É…Ñ¥½¸¹ÍÑ…Ñ” ¤¤ì(€€€€€€€€€€€…Í”Ae59Q}=55%QQ€´øÉ•ÍÕµ•I½Ñ…Ñ¥½¸¡½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤¤ì(€€€€€€€€€€€…Í”A9%9}1%YId€´ø½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ”¡I•ÍÕ±Ð¹Á•¹‘¥¹œ¡½Á•É…Ñ¥½¸¤¤ì(€€€€€€€€€€€…Í”1%YI€´ø½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ”¡I•ÍÕ±Ð¹‘•±¥Ù•É•¡½Á•É…Ñ¥½¸¤¤ì(€€€€€€€€€€€…Í”%1°	9=9€´ø½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ” (€€€€€€€€€€€€€€€I•ÍÕ±Ð¹™…¥±•¡½Á•É…Ñ¥½¸°™…¥±ÕÉ•½‘”¡½Á•É…Ñ¥½¸°€‰I%MMU}%1ˆ¤¤(€€€€€€€€€€€€¤ì(€€€€€€€€€€€…Í”U9-9=]8°AIAI°Ae59Q}A9%9€´ø½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ” (€€€€€€€€€€€€€€€I•ÍÕ±Ð¹Õ¹­¹½Ý¸¡½Á•É…Ñ¥½¸°™…¥±ÕÉ•½‘”¡½Á•É…Ñ¥½¸°€‰I=9%1}IEU%Iˆ¤¤(€€€€€€€€€€€€¤ì(€€€€€€€ôì(€€€ô((€€€ÁÉ¥Ù…Ñ”½µÁ±•Ñ¥½¹MÑ…”ñI•ÍÕ±Ðø…Õ‘¥Ñ• (€€€€€€€I•ÍÕ±ÐÉ•ÍÕ±Ð°(€€€€€€€MÑÉ¥¹œ•Ù•¹ÑQåÁ”°(€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸(€€€€¤ì(€€€€€€€½µÁ±•Ñ¥½¹MÑ…”ñY½¥øÉ•½É‘•ì(€€€€€€€ÑÉäì(€€€€€€€€€€€É•½É‘•€ô…Õ‘¥Ð¹É•½É¡¹•Ü]…å™…É•ÉÕ‘¥Ð¹Õ‘¥ÑÙ•¹Ð (€€€€€€€€€€€€€€€UU%¹É…¹‘½µUU% ¤°(€€€€€€€€€€€€€€€•Ù•¹ÑQåÁ”°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹Á±…å•ÉUÕ¥ ¤°(€€€€€€€€€€€€€€€MU	)Q}QeA°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹Ñ½½±% ¤¹Ñ½MÑÉ¥¹œ ¤°(€€€€€€€€€€€€€€€Í•ÉÙ•É%°(€€€€€€€€€€€€€€€…Õ‘¥Ñ•Ñ…¥±Ì¡É•ÍÕ±Ð¤°(€€€€€€€€€€€€€€€±½¬¹¥¹ÍÑ…¹Ð ¤(€€€€€€€€€€€€¤¤ì(€€€€€€€ô…Ñ €¡IÕ¹Ñ¥µ•á•ÁÑ¥½¸™…¥±ÕÉ”¤ì(€€€€€€€€€€€É•ÑÕÉ¸½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ”¡É•ÍÕ±Ð¤ì(€€€€€€€ô(€€€€€€€¥˜€¡É•½É‘•€ôô¹Õ±°¤ì(€€€€€€€€€€€É•ÑÕÉ¸½µÁ±•Ñ…‰±•ÕÑÕÉ”¹½µÁ±•Ñ•‘ÕÑÕÉ”¡É•ÍÕ±Ð¤ì(€€€€€€€ô(€€€€€€€É•ÑÕÉ¸É•½É‘•¹¡…¹‘±” ¡¥¹½É•°™…¥±ÕÉ”¤€´øÉ•ÍÕ±Ð¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒMÑÉ¥¹œ…Õ‘¥Ñ•Ñ…¥±Ì¡I•ÍÕ±ÐÉ•ÍÕ±Ð¤ì(€€€€€€€MÑÉ¥¹œ™…¥±ÕÉ”€ôÉ•ÍÕ±Ð¹™…¥±ÕÉ•½‘” ¤ì(€€€€€€€MÑÉ¥¹œÍ…™•…¥±ÕÉ”€ô™…¥±ÕÉ”€ôô¹Õ±°€ü€ˆˆ€èÍ…¹¥Ñ¥é•…¥±ÕÉ”¡™…¥±ÕÉ”°€‰U9-9=]8ˆ¤ì(€€€€€€€É•ÑÕÉ¸€‰íp‰ÍÑ…ÑÕÍpˆépˆˆ€¬É•ÍÕ±Ð¹ÍÑ…ÑÕÌ ¤¹¹…µ” ¤(€€€€€€€€€€€€¬€‰pˆ±p‰™…¥±ÕÉ•}½‘•pˆépˆˆ€¬Í…™•…¥±ÕÉ”€¬€‰p‰ôˆì(€€€ô((€€€ÁÉ¥Ù…Ñ”EÕ½Ñ•Ù…±Õ…Ñ¥½¸•Ù…±Õ…Ñ•EÕ½Ñ”¡UU%Á±…å•ÉUÕ¥¤ì(€€€€€€€=ÁÑ¥½¹…°ñÉ½ÝÑ¡Q½½°ø™½Õ¹€ôÉ½ÝÑ¡Q½½±Ì¹™¥¹‘	å=Ý¹•È¡Á±…å•ÉUÕ¥¤ì(€€€€€€€¥˜€¡™½Õ¹¹¥ÍµÁÑä ¤¤ì(€€€€€€€€€€€É•ÑÕÉ¸EÕ½Ñ•Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ” ‰Q==1}9=Q}=U9ˆ¤ì(€€€€€€€ô(€€€€€€€É½ÝÑ¡Q½½°Ñ½½°€ô™½Õ¹¹½É±Í•Q¡É½Ü ¤ì(€€€€€€€¥˜€¡Ñ½½°¹ÍÑ…ÑÕÌ ¤€ôôÉ½ÝÑ¡Q½½°¹MÑ…ÑÕÌ¹IY=-¤ì(€€€€€€€€€€€É•ÑÕÉ¸EÕ½Ñ•Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ” ‰Q==1}IY=-ˆ¤ì(€€€€€€€ô(€€€€€€€¥˜€¡Ñ½½°¹‘•±¥Ù•ÉåMÑ…ÑÕÌ ¤€ôôÉ½ÝÑ¡Q½½°¹•±¥Ù•ÉåMÑ…ÑÕÌ¹A9%9¤ì(€€€€€€€€€€€É•ÑÕÉ¸EÕ½Ñ•Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ” ‰1%YIe}A9%9ˆ¤ì(€€€€€€€ô(€€€€€€€¥˜€¡½Á•É…Ñ¥½¹Ì¹™¥¹‘Ñ¥Ù•	åQ½½°¡Ñ½½°¹Ñ½½±% ¤¤¹¥ÍAÉ•Í•¹Ð ¤¤ì(€€€€€€€€€€€É•ÑÕÉ¸EÕ½Ñ•Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ” ‰%9}1%!Pˆ¤ì(€€€€€€€ô(€€€€€€€¥¹Ð•Ù½±ÕÑ¥½¹½Õ¹Ð€ô•Ù½±ÕÑ¥½¹½Õ¹Ð¡Ñ½½°¤ì(€€€€€€€±½¹œ…µ½Õ¹Ðì(€€€€€€€ÑÉäì(€€€€€€€€€€€I•Á…¥ÉAÉ¥¥¹EÕ½Ñ”ÅÕ½Ñ”€ô¹•ÜI•Á…¥ÉAÉ¥¥¹EÕ½Ñ”¡ÁÉ¥¥¹œ¹ÅÕ½Ñ”¡•Ù½±ÕÑ¥½¹½Õ¹Ð¤¤ì(€€€€€€€€€€€…µ½Õ¹Ð€ôÅÕ½Ñ”¹…µ½Õ¹Ñ]…åµ…É¬ ¤ì(€€€€€€€ô…Ñ €¡É¥Ñ¡µ•Ñ¥á•ÁÑ¥½¸ð%±±•…±ÉÕµ•¹Ñá•ÁÑ¥½¸™…¥±ÕÉ”¤ì(€€€€€€€€€€€É•ÑÕÉ¸EÕ½Ñ•Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ” ‰AI%%9}%9Y1%ˆ¤ì(€€€€€€€ô(€€€€€€€É•ÑÕÉ¸EÕ½Ñ•Ù…±Õ…Ñ¥½¸¹ÍÕ•ÍÌ¡Ñ½½°°•Ù½±ÕÑ¥½¹½Õ¹Ð°…µ½Õ¹Ð¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”AÉ•Á…É•‘Ù…±Õ…Ñ¥½¸ÁÉ•Á…É•É½µEÕ½Ñ”¡UU%Á±…å•ÉUÕ¥°I•¥ÍÍÕ•EÕ½Ñ”ÅÕ½Ñ”¤ì(€€€€€€€EÕ½Ñ•Ù…±Õ…Ñ¥½¸•Ù…±Õ…Ñ¥½¸€ô•Ù…±Õ…Ñ•EÕ½Ñ”¡Á±…å•ÉUÕ¥¤ì(€€€€€€€¥˜€¡•Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ•½‘” ¤€„ô¹Õ±°¤ì(€€€€€€€€€€€É•ÑÕÉ¸AÉ•Á…É•‘Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ”¡•Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ•½‘” ¤¤ì(€€€€€€€ô(€€€€€€€¥˜€ …ÅÕ½Ñ”¹µ…Ñ¡•ÍM¹…ÁÍ¡½Ð (€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹Ñ½½° ¤°(€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹•Ù½±ÕÑ¥½¹½Õ¹Ð ¤°(€€€€€€€€€€€½¹™¥I•Ù¥Í¥½¸°(€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹…µ½Õ¹Ñ]…åµ…É¬ ¤°(€€€€€€€€€€€±½¬¹¥¹ÍÑ…¹Ð ¤(€€€€€€€€¤¤ì(€€€€€€€€€€€É•ÑÕÉ¸AÉ•Á…É•‘Ù…±Õ…Ñ¥½¸¹™…¥±ÕÉ” ‰EU=Q}!9ˆ¤ì(€€€€€€€ô(€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸€ô¹•ÜI•¥ÍÍÕ•=Á•É…Ñ¥½¸ (€€€€€€€€€€€UU%¹É…¹‘½µUU% ¤°(€€€€€€€€€€€€‰µ…¥¸µÉ•¥ÍÍÕ”èˆ€¬ÅÕ½Ñ”¹ÅÕ½Ñ•% ¤°(€€€€€€€€€€€Á±…å•ÉUÕ¥°(€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹Ñ½½° ¤¹Ñ½½±% ¤°(€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹Ñ½½° ¤¹¥Ñ•µ%¹ÍÑ…¹•% ¤°(€€€€€€€€€€€UU%¹É…¹‘½µUU% ¤°(€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹Ñ½½° ¤¹¥¹ÍÑ…¹•Á½  ¤°(€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹•Ù½±ÕÑ¥½¹½Õ¹Ð ¤°(€€€€€€€€€€€½¹™¥I•Ù¥Í¥½¸°(€€€€€€€€€€€•Ù…±Õ…Ñ¥½¸¹…µ½Õ¹Ñ]…åµ…É¬ ¤°(€€€€€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸¹MÑ…Ñ”¹AIAI°(€€€€€€€€€€€¹Õ±°°(€€€€€€€€€€€¹Õ±°°(€€€€€€€€€€€¹Õ±°°(€€€€€€€€€€€€À(€€€€€€€€¤ì(€€€€€€€É•ÑÕÉ¸AÉ•Á…É•‘Ù…±Õ…Ñ¥½¸¹ÍÕ•ÍÌ¡½Á•É…Ñ¥½¹Ì¹ÁÉ•Á…É”¡½Á•É…Ñ¥½¸°±½¬¹¥¹ÍÑ…¹Ð ¤¤¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”¥¹Ð•Ù½±ÕÑ¥½¹½Õ¹Ð¡É½ÝÑ¡Q½½°Ñ½½°¤ì(€€€€€€€É•ÑÕÉ¸•Ù½±ÕÑ¥½¹A±…¸¹•Ù…±Õ…Ñ” (€€€€€€€€€€€Ñ½½°¹ÕµÕ±…Ñ¥Ù•AÉ½É•ÍÍU¹¥ÑÌ ¤°(€€€€€€€€€€€Ñ½½°¹‰É…¹  ¤°(€€€€€€€€€€€•¹¡…¹Ñµ•¹Ñ…ÁÌ(€€€€€€€€¤¹•Ù½±ÕÑ¥½¹½Õ¹Ð ¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒMÑÉ¥¹œÍ¹…ÁÍ¡½ÑI•©•Ñ¥½¸ (€€€€€€€I•¥ÍÍÕ•±¥¥‰¥±¥ÑåM¹…ÁÍ¡½ÐÍ¹…ÁÍ¡½Ð°(€€€€€€€UU%Á±…å•ÉUÕ¥(€€€€¤ì(€€€€€€€¥˜€¡Í¹…ÁÍ¡½Ð€ôô¹Õ±°ñð€…Á±…å•ÉUÕ¥¹•ÅÕ…±Ì¡Í¹…ÁÍ¡½Ð¹Á±…å•ÉUÕ¥ ¤¤¤ì(€€€€€€€€€€€É•ÑÕÉ¸€‰1%%	%1%Qe}U9Y%1	1ˆì(€€€€€€€ô(€€€€€€€¥˜€ …Í¹…ÁÍ¡½Ð¹Á±…å•É=¹±¥¹” ¤¤ì(€€€€€€€€€€€É•ÑÕÉ¸€‰A1eI}=1%9ˆì(€€€€€€€ô(€€€€€€€¥˜€ …Í¹…ÁÍ¡½Ð¹Ý½É±‘±±½Ý• ¤(€€€€€€€€€€€ñð€…I•¥ÍÍÕ•±¥¥‰¥±¥ÑåA½±¥ä¹¥Í±±½Ý•‘]½É±¡Í¹…ÁÍ¡½Ð¹•á…Ñ]½É±‘9…µ” ¤¤¤ì(€€€€€€€€€€€É•ÑÕÉ¸€‰]=I1}9=Q}11=]ˆì(€€€€€€€ô(€€€€€€€¥˜€¡Í¹…ÁÍ¡½Ð¹ÕÉÉ•¹Ñ%Ñ•µAÉ•Í•¹Ð ¤¤ì(€€€€€€€€€€€É•ÑÕÉ¸€‰UII9Q}%Q5}AIM9Pˆì(€€€€€€€ô(€€€€€€€É•ÑÕÉ¸¹Õ±°ì(€€€ô((€€€ÁÉ¥Ù…Ñ”‰½½±•…¸ÑÉ…¹Í…Ñ¥½¹•Ñ…¥±Í5…Ñ  (€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°(€€€€€€€]…å™…É•ÉQÉ…¹Í…Ñ¥½¹Ì¹QÉ…¹Í…Ñ¥½¹•Ñ…¥±Ì‘•Ñ…¥±Ì(€€€€¤ì(€€€€€€€É•ÑÕÉ¸½Á•É…Ñ¥½¸¹ÑÉ…¹Í…Ñ¥½¹% ¤¹•ÅÕ…±Ì¡‘•Ñ…¥±Ì¹ÑÉ…¹Í…Ñ¥½¹% ¤¤(€€€€€€€€€€€€˜˜µ…Ñ¡•Í%™AÉ•Í•¹Ð¡½É•%‘•µÁ½Ñ•¹å-•ä¡½Á•É…Ñ¥½¸¤°‘•Ñ…¥±Ì¹¥‘•µÁ½Ñ•¹å-•ä ¤¤(€€€€€€€€€€€€˜˜µ…Ñ¡•Í%™AÉ•Í•¹Ð¡QI9MQ%=9}QeA°‘•Ñ…¥±Ì¹ÑÉ…¹Í…Ñ¥½¹QåÁ” ¤¤(€€€€€€€€€€€€˜˜µ…Ñ¡•Í%™AÉ•Í•¹Ð¡½Á•É…Ñ¥½¸¹Á±…å•ÉUÕ¥ ¤°‘•Ñ…¥±Ì¹…Ñ½ÉUÕ¥ ¤¤(€€€€€€€€€€€€˜˜µ…Ñ¡•Í%™AÉ•Í•¹Ð¡MU	)Q}QeA°‘•Ñ…¥±Ì¹ÍÕ‰©•ÑQåÁ” ¤¤(€€€€€€€€€€€€˜˜µ…Ñ¡•Í%™AÉ•Í•¹Ð¡½Á•É…Ñ¥½¸¹Ñ½½±% ¤¹Ñ½MÑÉ¥¹œ ¤°‘•Ñ…¥±Ì¹ÍÕ‰©•Ñ% ¤¤(€€€€€€€€€€€€˜˜€¡‘•Ñ…¥±Ì¹…µ½Õ¹Ñ]…åµ…É¬ ¤€ôô€À(€€€€€€€€€€€€€€€ñð‘•Ñ…¥±Ì¹…µ½Õ¹Ñ]…åµ…É¬ ¤€ôô½Á•É…Ñ¥½¸¹…µ½Õ¹Ñ]…åµ…É¬ ¤¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥Œ‰½½±•…¸µ…Ñ¡•Í%™AÉ•Í•¹Ð¡=‰©•Ð•áÁ•Ñ•°=‰©•Ð…ÑÕ…°¤ì(€€€€€€€É•ÑÕÉ¸…ÑÕ…°€ôô¹Õ±°ñð•áÁ•Ñ•¹•ÅÕ…±Ì¡…ÑÕ…°¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥Œ‰½½±•…¸…±É•…‘åI½Ñ…Ñ• (€€€€€€€É½ÝÑ¡Q½½°ÕÉÉ•¹Ð°(€€€€€€€I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸(€€€€¤ì(€€€€€€€É•ÑÕÉ¸ÕÉÉ•¹Ð¹¥Ñ•µ%¹ÍÑ…¹•% ¤¹•ÅÕ…±Ì¡½Á•É…Ñ¥½¸¹¹•Ý%Ñ•µ%¹ÍÑ…¹•% ¤¤(€€€€€€€€€€€€˜˜ÕÉÉ•¹Ð¹¥¹ÍÑ…¹•Á½  ¤€ôô½Á•É…Ñ¥½¸¹¥¹ÍÑ…¹•Á½  ¤€¬€Ä(€€€€€€€€€€€€˜˜ÕÉÉ•¹Ð¹ÍÑ…ÑÕÌ ¤€ôôÉ½ÝÑ¡Q½½°¹MÑ…ÑÕÌ¹Q%Yì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒMÑÉ¥¹œ½É•%‘•µÁ½Ñ•¹å-•ä¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸¤ì(€€€€€€€É•ÑÕÉ¸€‰µ…¥¸µÉ•¥ÍÍÕ”èˆ€¬½Á•É…Ñ¥½¸¹¥‘•µÁ½Ñ•¹å-•ä ¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒMÑÉ¥¹œÁ…å±½…‘½È¡UU%É•¥ÍÍÕ•%¤ì(€€€€€€€É•ÑÕÉ¸€‰íp‰É•¥ÍÍÕ•}¥‘pˆépˆˆ€¬É•¥ÍÍÕ•%€¬€‰p‰ôˆì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒMÑÉ¥¹œ™…¥±ÕÉ•½‘”¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°MÑÉ¥¹œ™…±±‰…¬¤ì(€€€€€€€É•ÑÕÉ¸½Á•É…Ñ¥½¸¹™…¥±ÕÉ•½‘” ¤€ôô¹Õ±°€ü™…±±‰…¬€è½Á•É…Ñ¥½¸¹™…¥±ÕÉ•½‘” ¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒMÑÉ¥¹œÍ…¹¥Ñ¥é•…¥±ÕÉ”¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”°MÑÉ¥¹œ™…±±‰…¬¤ì(€€€€€€€É•ÑÕÉ¸™…¥±ÕÉ•½‘”€„ô¹Õ±°€˜˜™…¥±ÕÉ•½‘”¹µ…Ñ¡•Ì ‰mµhÀ´å}uìÌ°äÙôˆ¤(€€€€€€€€€€€€ü™…¥±ÕÉ•½‘”(€€€€€€€€€€€€è™…±±‰…¬ì(€€€ô((€€€ÁÕ‰±¥ŒÉ•½ÉEÕ½Ñ•I•ÍÕ±Ð¡EÕ½Ñ•MÑ…ÑÕÌÍÑ…ÑÕÌ°I•¥ÍÍÕ•EÕ½Ñ”ÅÕ½Ñ”°MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒEÕ½Ñ•I•ÍÕ±Ð¥ÍÍÕ•¡I•¥ÍÍÕ•EÕ½Ñ”ÅÕ½Ñ”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜEÕ½Ñ•I•ÍÕ±Ð¡EÕ½Ñ•MÑ…ÑÕÌ¹%MMU°ÅÕ½Ñ”°¹Õ±°¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒEÕ½Ñ•I•ÍÕ±ÐÉ•©•Ñ•¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜEÕ½Ñ•I•ÍÕ±Ð¡EÕ½Ñ•MÑ…ÑÕÌ¹I)Q°¹Õ±°°™…¥±ÕÉ•½‘”¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒEÕ½Ñ•I•ÍÕ±ÐÕ¹…Ù…¥±…‰±”¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜEÕ½Ñ•I•ÍÕ±Ð¡EÕ½Ñ•MÑ…ÑÕÌ¹U9Y%1	1°¹Õ±°°™…¥±ÕÉ•½‘”¤ì(€€€€€€€ô(€€€ô((€€€ÁÕ‰±¥Œ•¹Õ´EÕ½Ñ•MÑ…ÑÕÌì(€€€€€€€%MMU°(€€€€€€€I)Q°(€€€€€€€U9Y%1	1(€€€ô((€€€ÁÕ‰±¥ŒÉ•½ÉI•ÍÕ±Ð (€€€€€€€MÑ…ÑÕÌÍÑ…ÑÕÌ°(€€€€€€€UU%É•¥ÍÍÕ•%°(€€€€€€€UU%ÑÉ…¹Í…Ñ¥½¹%°(€€€€€€€MÑÉ¥¹œ™…¥±ÕÉ•½‘”(€€€€¤ì(€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI•ÍÕ±Ð‘•±¥Ù•É•¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI•ÍÕ±Ð¡MÑ…ÑÕÌ¹1%YI°½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤°½Á•É…Ñ¥½¸¹ÑÉ…¹Í…Ñ¥½¹% ¤°¹Õ±°¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI•ÍÕ±ÐÁ•¹‘¥¹œ¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI•ÍÕ±Ð¡MÑ…ÑÕÌ¹A9%9°½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤°½Á•É…Ñ¥½¸¹ÑÉ…¹Í…Ñ¥½¹% ¤°¹Õ±°¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI•ÍÕ±ÐÁ•¹‘¥¹œ¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI•ÍÕ±Ð (€€€€€€€€€€€€€€€MÑ…ÑÕÌ¹A9%9°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹ÑÉ…¹Í…Ñ¥½¹% ¤°(€€€€€€€€€€€€€€€™…¥±ÕÉ•½‘”(€€€€€€€€€€€€¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI•ÍÕ±Ð™…¥±•¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI•ÍÕ±Ð (€€€€€€€€€€€€€€€MÑ…ÑÕÌ¹%1°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹ÑÉ…¹Í…Ñ¥½¹% ¤°(€€€€€€€€€€€€€€€™…¥±ÕÉ•½‘”(€€€€€€€€€€€€¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI•ÍÕ±ÐÕ¹­¹½Ý¸¡I•¥ÍÍÕ•=Á•É…Ñ¥½¸½Á•É…Ñ¥½¸°MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI•ÍÕ±Ð (€€€€€€€€€€€€€€€MÑ…ÑÕÌ¹U9-9=]8°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹É•¥ÍÍÕ•% ¤°(€€€€€€€€€€€€€€€½Á•É…Ñ¥½¸¹ÑÉ…¹Í…Ñ¥½¹% ¤°(€€€€€€€€€€€€€€€™…¥±ÕÉ•½‘”(€€€€€€€€€€€€¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI•ÍÕ±ÐÉ•©•Ñ•¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI•ÍÕ±Ð¡MÑ…ÑÕÌ¹I)Q°¹Õ±°°¹Õ±°°™…¥±ÕÉ•½‘”¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI•ÍÕ±ÐÕ¹…Ù…¥±…‰±”¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI•ÍÕ±Ð¡MÑ…ÑÕÌ¹U9Y%1	1°¹Õ±°°¹Õ±°°™…¥±ÕÉ•½‘”¤ì(€€€€€€€ô(€€€ô((€€€ÁÕ‰±¥Œ•¹Õ´MÑ…ÑÕÌì(€€€€€€€1%YI°(€€€€€€€A9%9°(€€€€€€€%1°(€€€€€€€U9-9=]8°(€€€€€€€I)Q°(€€€€€€€U9Y%1	1(€€€ô((€€€ÁÉ¥Ù…Ñ”É•½ÉEÕ½Ñ•Ù…±Õ…Ñ¥½¸ (€€€€€€€É½ÝÑ¡Q½½°Ñ½½°°(€€€€€€€¥¹Ð•Ù½±ÕÑ¥½¹½Õ¹Ð°(€€€€€€€±½¹œ…µ½Õ¹Ñ]…åµ…É¬°(€€€€€€€MÑÉ¥¹œ™…¥±ÕÉ•½‘”(€€€€¤ì(€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒEÕ½Ñ•Ù…±Õ…Ñ¥½¸ÍÕ•ÍÌ (€€€€€€€€€€€É½ÝÑ¡Q½½°Ñ½½°°(€€€€€€€€€€€¥¹Ð•Ù½±ÕÑ¥½¹½Õ¹Ð°(€€€€€€€€€€€±½¹œ…µ½Õ¹Ñ]…åµ…É¬(€€€€€€€€¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜEÕ½Ñ•Ù…±Õ…Ñ¥½¸¡Ñ½½°°•Ù½±ÕÑ¥½¹½Õ¹Ð°…µ½Õ¹Ñ]…åµ…É¬°¹Õ±°¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒEÕ½Ñ•Ù…±Õ…Ñ¥½¸™…¥±ÕÉ”¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜEÕ½Ñ•Ù…±Õ…Ñ¥½¸¡¹Õ±°°€À°€À°™…¥±ÕÉ•½‘”¤ì(€€€€€€€ô(€€€ô((€€€ÁÉ¥Ù…Ñ”É•½ÉAÉ•Á…É•‘Ù…±Õ…Ñ¥½¸ (€€€€€€€AÉ•Á…É•=ÕÑ½µ”½ÕÑ½µ”°(€€€€€€€MÑÉ¥¹œ™…¥±ÕÉ•½‘”(€€€€¤ì(€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒAÉ•Á…É•‘Ù…±Õ…Ñ¥½¸ÍÕ•ÍÌ¡AÉ•Á…É•=ÕÑ½µ”½ÕÑ½µ”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜAÉ•Á…É•‘Ù…±Õ…Ñ¥½¸¡½ÕÑ½µ”°¹Õ±°¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒAÉ•Á…É•‘Ù…±Õ…Ñ¥½¸™…¥±ÕÉ”¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜAÉ•Á…É•‘Ù…±Õ…Ñ¥½¸¡¹Õ±°°™…¥±ÕÉ•½‘”¤ì(€€€€€€€ô(€€€ô((€€€ÁÉ¥Ù…Ñ”É•½ÉI½Ñ…Ñ¥½¹•¥Í¥½¸¡É½ÝÑ¡Q½½°Ñ½½°°MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI½Ñ…Ñ¥½¹•¥Í¥½¸ÍÕ•ÍÌ¡É½ÝÑ¡Q½½°Ñ½½°¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI½Ñ…Ñ¥½¹•¥Í¥½¸¡Ñ½½°°¹Õ±°¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒI½Ñ…Ñ¥½¹•¥Í¥½¸™…¥±ÕÉ”¡MÑÉ¥¹œ™…¥±ÕÉ•½‘”¤ì(€€€€€€€€€€€É•ÑÕÉ¸¹•ÜI½Ñ…Ñ¥½¹•¥Í¥½¸¡¹Õ±°°™…¥±ÕÉ•½‘”¤ì(€€€€€€€ô(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥Œ™¥¹…°±…ÍÌM¹…ÁÍ¡½Ñ…ÁÑÕÉ”ì(€€€€€€€ÁÉ¥Ù…Ñ”I•¥ÍÍÕ•±¥¥‰¥±¥ÑåM¹…ÁÍ¡½ÐÍ¹…ÁÍ¡½Ðì(€€€€€€€ÁÉ¥Ù…Ñ”I•¥ÍÍÕ•EÕ½Ñ”ÅÕ½Ñ”ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥Œ™¥¹…°±…ÍÌEÕ½Ñ•…ÁÑÕÉ”ì(€€€€€€€ÁÉ¥Ù…Ñ”I•¥ÍÍÕ•EÕ½Ñ”ÅÕ½Ñ”ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥Œ™¥¹…°±…ÍÌ•±¥Ù•Éå…ÁÑÕÉ”ì(€€€€€€€ÁÉ¥Ù…Ñ”•±¥Ù•Éå=ÕÑ½µ”½ÕÑ½µ”ì(€€€ô((€€€ÁÉ¥Ù…Ñ”É•½ÉI•Á…¥ÉAÉ¥¥¹EÕ½Ñ”¡±½¹œ…µ½Õ¹Ñ]…åµ…É¬¤ì(€€€€€€€ÁÉ¥Ù…Ñ”I•Á…¥ÉAÉ¥¥¹EÕ½Ñ”¡¥¼¹¥Ñ¡Õˆ¹•…É¥Ù•È¹Ý…å™…É•È¹µ…¥¸¹‘½µ…¥¸¹I•Á…¥ÉAÉ¥¥¹œ¹EÕ½Ñ”ÅÕ½Ñ”¤ì(€€€€€€€€€€€Ñ¡¥Ì¡É•ÅÕ¥É•Ù…¥±…‰±”¡ÅÕ½Ñ”¤¤ì(€€€€€€€ô((€€€€€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥Œ±½¹œÉ•ÅÕ¥É•Ù…¥±…‰±” (€€€€€€€€€€€¥¼¹¥Ñ¡Õˆ¹•…É¥Ù•È¹Ý…å™…É•È¹µ…¥¸¹‘½µ…¥¸¹I•Á…¥ÉAÉ¥¥¹œ¹EÕ½Ñ”ÅÕ½Ñ”(€€€€€€€€¤ì(€€€€€€€€€€€¥˜€ …ÅÕ½Ñ”¹…Ù…¥±…‰±” ¤ñðÅÕ½Ñ”¹…µ½Õ¹Ñ]…åµ…É¬ ¤€ðô€À¤ì(€€€€€€€€€€€€€€€Ñ¡É½Ü¹•ÜÉ¥Ñ¡µ•Ñ¥á•ÁÑ¥½¸ ‰I•¥ÍÍÕ”ÅÕ½Ñ”¥ÌÕ¹…Ù…¥±…‰±”ˆ¤ì(€€€€€€€€€€€ô(€€€€€€€€€€€É•ÑÕÉ¸ÅÕ½Ñ”¹…µ½Õ¹Ñ]…åµ…É¬ ¤ì(€€€€€€€ô(€€€ô)ô(