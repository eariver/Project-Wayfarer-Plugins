package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation;
import io.github.eariver.wayfarer.core.identity.PlayerIdentitySink;

import java.sql.PreparedStatement;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class IdentityRuntime implements PlayerIdentitySink, AutoCloseable {
    private final InternalDatabase database;
    private final MariaDbPlayerIdentityRepository players;
    private final DefaultWayfarerItemIdentity items;
    private final WayfarerAudit audit;
    private final HealthRegistry health;
    private final Consumer<String> warningSink;
    private final String serverId;
    private final Clock clock;
    private final Object lifecycleMonitor = new Object();
    private LifecycleState lifecycleState = LifecycleState.OPEN;
    private int inFlight;
    private boolean acceptedFailureDuringClose;
    private IdentityCloseStatus quiesceFailure;
    private boolean quiesced;

    IdentityRuntime(
        InternalDatabase database,
        WayfarerAudit audit,
        HealthRegistry health,
        String serverId,
        Clock clock,
        Supplier<UUID> uuidGenerator
    ) {
        this(
            database,
            audit,
            health,
            serverId,
            clock,
            uuidGenerator,
            ignored -> {}
        );
    }

    IdentityRuntime(
        InternalDatabase database,
        WayfarerAudit audit,
        HealthRegistry health,
        String serverId,
        Clock clock,
        Supplier<UUID> uuidGenerator,
        Consumer<String> warningSink
    ) {
        this.database = Objects.requireNonNull(database, "database");
        this.players = new MariaDbPlayerIdentityRepository(database);
        this.audit = Objects.requireNonNull(audit, "audit");
        this.health = Objects.requireNonNull(health, "health");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.items = new DefaultWayfarerItemIdentity(
            new MariaDbItemIdentityRepository(database),
            uuidGenerator
        );
    }

    public CompletionStage<Void> initialize() {
        try {
            return observe(database.read(connection -> {
                try (PreparedStatement playersProbe = connection.prepareStatement(
                    "SELECT player_uuid FROM wf_core_player_identity WHERE 1 = 0"
                );
                     PreparedStatement itemsProbe = connection.prepareStatement(
                         "SELECT item_instance_id FROM wf_core_item_identity WHERE 1 = 0"
                     )) {
                    playersProbe.executeQuery().close();
                    itemsProbe.executeQuery().close();
                    return null;
                }
            }));
        } catch (RuntimeException failure) {
            markDown();
            return CompletableFuture.failedFuture(failure);
        }
    }

    public WayfarerItemIdentity itemIdentity() {
        return items;
    }

    @Override
    public CompletionStage<Void> observe(PlayerIdentityObservation observation) {
        if (observation == null || observation.playerUuid() == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Player identity observation is invalid")
            );
        }
        return accept(
            () -> observeAccepted(observation),
            "Identity is unavailable"
        );
    }

    private CompletionStage<Void> observeAccepted(PlayerIdentityObservation observation) {
        CompletionStage<Void> operation;
        try {
            operation = players.upsert(observation);
        } catch (RuntimeException failure) {
            return playerUpsertFailure(observation.playerUuid());
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        operation.whenComplete((ignored, failure) -> {
            if (failure == null) {
                markUp();
                result.complete(null);
            } else {
                playerUpsertFailure(observation.playerUuid())
                    .whenComplete((auditIgnored, finalFailure) ->
                        result.completeExceptionally(unwrap(finalFailure))
                    );
            }
        });
        return result;
    }

    CompletionStage<Optional<PlayerIdentityRecord>> findPlayer(UUID playerUuid) {
        return accept(
            () -> findPlayerAccepted(playerUuid),
            "Identity is unavailable"
        );
    }

    private CompletionStage<Optional<PlayerIdentityRecord>> findPlayerAccepted(UUID playerUuid) {
        CompletionStage<Optional<PlayerIdentityRecord>> operation;
        try {
            operation = players.find(playerUuid);
        } catch (RuntimeException failure) {
            markDown();
            return CompletableFuture.failedFuture(failure);
        }
        CompletableFuture<Optional<PlayerIdentityRecord>> result = new CompletableFuture<>();
        operation.whenComplete((identity, failure) -> {
            if (failure == null) {
                markUp();
                result.complete(identity);
            } else {
                markDown();
                result.completeExceptionally(unwrap(failure));
            }
        });
        return result;
    }

    private CompletionStage<Void> playerUpsertFailure(UUID playerUuid) {
        markDown();
        warn("Wayfarer player identity upsert failed");
        CompletableFuture<Void> result = new CompletableFuture<>();
        CompletionStage<Void> auditAttempt;
        try {
            auditAttempt = audit.record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                "PLAYER_IDENTITY_UPSERT_FAILED",
                null,
                "PLAYER_IDENTITY",
                playerUuid.toString(),
                serverId,
                "{\"failure_code\":\"PERSISTENCE_OPERATION_FAILED\"}",
                clock.instant()
            ));
        } catch (RuntimeException failure) {
            auditAttempt = CompletableFuture.failedFuture(failure);
        }
        auditAttempt.whenComplete((ignored, auditFailure) -> {
            if (auditFailure == null) {
                result.completeExceptionally(
                    new PersistenceException("Player identity upsert failed")
                );
            } else {
                warn("Wayfarer player identity failure audit failed");
                result.completeExceptionally(
                    new PersistenceException("Player identity upsert and failure audit failed")
                );
            }
        });
        return result;
    }

    private CompletionStage<Void> observe(CompletionStage<Void> operation) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        operation.whenComplete((ignored, failure) -> {
            if (failure == null) {
                markUp();
                result.complete(null);
            } else {
                markDown();
                result.completeExceptionally(unwrap(failure));
            }
        });
        return result;
    }

    private void markUp() {
        synchronized (lifecycleMonitor) {
            if (lifecycleState != LifecycleState.OPEN) {
                return;
            }
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.UP,
                "Identity repositories available"
            );
        }
    }

    private void markDown() {
        synchronized (lifecycleMonitor) {
            if (lifecycleState == LifecycleState.CLOSED) {
                return;
            }
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.DOWN,
                "Identity repository operation failed"
            );
        }
    }

    @Override
    public void close() {
        synchronized (lifecycleMonitor) {
            if (lifecycleState != LifecycleState.OPEN) {
                return;
            }
            lifecycleState = LifecycleState.CLOSING;
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.UNKNOWN,
                "Identity intake closed; accepted operations draining"
            );
        }
    }

    public IdentityCloseStatus finishClosing(
        PersistenceDrainResult persistenceDrain,
        Duration timeout
    ) {
        Objects.requireNonNull(persistenceDrain, "persistenceDrain");
        Objects.requireNonNull(timeout, "timeout");
        IdentityCloseStatus quiesceStatus = quiesce(timeout);
        synchronized (lifecycleMonitor) {
            lifecycleState = LifecycleState.CLOSED;
            if (quiesceStatus != IdentityCloseStatus.CLEAN) {
                return quiesceStatus;
            }
            if (persistenceDrain.status() != PersistenceDrainStatus.DRAINED
                || acceptedFailureDuringClose) {
                health.update(
                    HealthRegistry.IDENTITY,
                    WayfarerHealth.Status.DOWN,
                    "Identity finalization was not clean"
                );
                return IdentityCloseStatus.FAILED;
            }
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.DISABLED,
                "Identity services closed after accepted work drained"
            );
            return IdentityCloseStatus.CLEAN;
        }
    }

    public IdentityCloseStatus quiesce(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        close();
        long remainingNanos = timeout.toNanos();
        long deadline = System.nanoTime() + remainingNanos;
        synchronized (lifecycleMonitor) {
            if (quiesceFailure != null) {
                return quiesceFailure;
            }
            if (quiesced) {
                return IdentityCloseStatus.CLEAN;
            }
            while (inFlight > 0 && remainingNanos > 0) {
                try {
                    long millis = remainingNanos / 1_000_000L;
                    int nanos = (int) (remainingNanos % 1_000_000L);
                    lifecycleMonitor.wait(millis, nanos);
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    quiesceFailure = IdentityCloseStatus.INTERRUPTED;
                    health.update(
                        HealthRegistry.IDENTITY,
                        WayfarerHealth.Status.DOWN,
                        "Identity accepted-work quiescence was interrupted"
                    );
                    warn("Wayfarer identity accepted-work quiescence was interrupted");
                    return quiesceFailure;
                }
                remainingNanos = deadline - System.nanoTime();
            }
            if (inFlight > 0) {
                quiesceFailure = IdentityCloseStatus.TIMED_OUT;
                health.update(
                    HealthRegistry.IDENTITY,
                    WayfarerHealth.Status.DOWN,
                    "Identity accepted-work quiescence timed out"
                );
                warn("Wayfarer identity accepted-work quiescence timed out");
                return quiesceFailure;
            }
            quiesced = true;
            return IdentityCloseStatus.CLEAN;
        }
    }

    LifecycleState lifecycleState() {
        synchronized (lifecycleMonitor) {
            return lifecycleState;
        }
    }

    private <T> CompletionStage<T> accept(
        Supplier<CompletionStage<T>> submission,
        String unavailableMessage
    ) {
        synchronized (lifecycleMonitor) {
            if (lifecycleState != LifecycleState.OPEN) {
                return CompletableFuture.failedFuture(
                    new IllegalStateException(unavailableMessage)
                );
            }
            inFlight++;
        }
        CompletionStage<T> operation;
        try {
            operation = Objects.requireNonNull(submission.get(), "operation");
        } catch (RuntimeException failure) {
            operationCompleted(failure);
            return CompletableFuture.failedFuture(failure);
        }
        CompletableFuture<T> result = new CompletableFuture<>();
        operation.whenComplete((value, failure) -> {
            operationCompleted(failure);
            if (failure == null) {
                result.complete(value);
            } else {
                result.completeExceptionally(unwrap(failure));
            }
        });
        return result;
    }

    private void operationCompleted(Throwable failure) {
        synchronized (lifecycleMonitor) {
            if (failure != null && lifecycleState == LifecycleState.CLOSING) {
                acceptedFailureDuringClose = true;
            }
            inFlight--;
            lifecycleMonitor.notifyAll();
        }
    }

    private void warn(String warning) {
        try {
            warningSink.accept(warning);
        } catch (RuntimeException ignored) {
            // Health and exceptional completion remain authoritative.
        }
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable current = failure;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    enum LifecycleState {
        OPEN,
        CLOSING,
        CLOSED
    }

    public enum IdentityCloseStatus {
        CLEAN,
        FAILED,
        TIMED_OUT,
        INTERRUPTED
    }

    private final class DefaultWayfarerItemIdentity implements WayfarerItemIdentity {
        private final MariaDbItemIdentityRepository repository;
        private final Supplier<UUID> uuidGenerator;

        private DefaultWayfarerItemIdentity(
            MariaDbItemIdentityRepository repository,
            Supplier<UUID> uuidGenerator
        ) {
            this.repository = repository;
            this.uuidGenerator = uuidGenerator;
        }

        @Override
        public CompletionStage<Identity> create(CreateRequest request) {
            return accept(() -> createAccepted(request), "Item identity is unavailable");
        }

        private CompletionStage<Identity> createAccepted(CreateRequest request) {
            try {
                ItemIdentityValidator.validateCreate(request);
            } catch (RuntimeException failure) {
                return CompletableFuture.failedFuture(failure);
            }
            Identity identity;
            try {
                UUID itemId = Objects.requireNonNull(uuidGenerator.get(), "generated UUID");
                java.time.Instant now = clock.instant().truncatedTo(ChronoUnit.MILLIS);
                identity = new Identity(
                    itemId,
                    request.itemType(),
                    request.ownerUuid(),
                    request.instanceEpoch(),
                    request.schemaVersion(),
                    request.displayRevision(),
                    now,
                    now,
                    0
                );
            } catch (RuntimeException failure) {
                return CompletableFuture.failedFuture(failure);
            }
            CompletionStage<MariaDbItemIdentityRepository.InsertResult> operation;
            try {
                operation = repository.create(identity);
            } catch (RuntimeException failure) {
                markDown();
                return CompletableFuture.failedFuture(failure);
            }
            CompletableFuture<Identity> result = new CompletableFuture<>();
            operation.whenComplete((insert, failure) -> {
                if (failure != null) {
                    infrastructureFailure(result, failure);
                } else if (insert == MariaDbItemIdentityRepository.InsertResult.CONFLICT) {
                    result.completeExceptionally(
                        new IllegalStateException("Generated item identity already exists")
                    );
                } else {
                    markUp();
                    result.complete(identity);
                }
            });
            return result;
        }

        @Override
        public CompletionStage<Optional<Identity>> find(UUID itemInstanceId) {
            return accept(() -> findAccepted(itemInstanceId), "Item identity is unavailable");
        }

        private CompletionStage<Optional<Identity>> findAccepted(UUID itemInstanceId) {
            if (itemInstanceId == null) {
                return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Item instance UUID is required")
                );
            }
            CompletionStage<Optional<Identity>> operation;
            try {
                operation = repository.find(itemInstanceId);
            } catch (RuntimeException failure) {
                markDown();
                return CompletableFuture.failedFuture(failure);
            }
            CompletableFuture<Optional<Identity>> result = new CompletableFuture<>();
            operation.whenComplete((identity, failure) -> {
                if (failure != null) {
                    infrastructureFailure(result, failure);
                } else {
                    markUp();
                    result.complete(identity);
                }
            });
            return result;
        }

        @Override
        public CompletionStage<ValidationResult> validate(ValidationRequest request) {
            return accept(() -> validateAccepted(request), "Item identity is unavailable");
        }

        private CompletionStage<ValidationResult> validateAccepted(ValidationRequest request) {
            if (request == null) {
                return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Validation request is required")
                );
            }
            ItemIdentityValidator.ParsedClaim claim;
            try {
                claim = ItemIdentityValidator.parse(request);
            } catch (RuntimeException failure) {
                return CompletableFuture.failedFuture(failure);
            }
            if (claim.failure() != null) {
                return auditInvalid(claim.failure(), claim.itemInstanceId(), claim.itemType());
            }
            CompletionStage<Optional<Identity>> operation;
            try {
                operation = repository.find(claim.itemInstanceId());
            } catch (RuntimeException failure) {
                markDown();
                return CompletableFuture.failedFuture(failure);
            }
            CompletableFuture<ValidationResult> result = new CompletableFuture<>();
            operation.whenComplete((found, failure) -> {
                if (failure != null) {
                    infrastructureFailure(result, failure);
                    return;
                }
                markUp();
                if (found.isEmpty()) {
                    completeAuditedInvalid(
                        result,
                        FailureReason.IDENTITY_NOT_FOUND,
                        claim.itemInstanceId(),
                        claim.itemType()
                    );
                    return;
                }
                FailureReason mismatch = ItemIdentityValidator.mismatch(claim, found.orElseThrow());
                if (mismatch == null) {
                    result.complete(ValidationResult.valid(found.orElseThrow()));
                } else {
                    completeAuditedInvalid(
                        result,
                        mismatch,
                        claim.itemInstanceId(),
                        claim.itemType()
                    );
                }
            });
            return result;
        }

        private CompletionStage<ValidationResult> auditInvalid(
            FailureReason reason,
            UUID itemId,
            String itemType
        ) {
            CompletableFuture<ValidationResult> result = new CompletableFuture<>();
            completeAuditedInvalid(result, reason, itemId, itemType);
            return result;
        }

        private void completeAuditedInvalid(
            CompletableFuture<ValidationResult> result,
            FailureReason reason,
            UUID itemId,
            String itemType
        ) {
            String subjectId = itemId == null ? "unparsed-claim" : itemId.toString();
            StringBuilder details = new StringBuilder()
                .append("{\"failure_reason\":\"")
                .append(reason.name())
                .append('"');
            if (itemId != null) {
                details.append(",\"item_instance_id\":\"").append(itemId).append('"');
            }
            if (itemType != null) {
                details.append(",\"item_type\":\"").append(itemType).append('"');
            }
            details.append('}');
            audit.record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                "ITEM_IDENTITY_VALIDATION_FAILED",
                null,
                "ITEM_IDENTITY",
                subjectId,
                serverId,
                details.toString(),
                clock.instant()
            )).whenComplete((ignored, auditFailure) -> {
                if (auditFailure == null) {
                    markUp();
                    result.complete(ValidationResult.invalid(reason));
                } else {
                    markDown();
                    result.completeExceptionally(unwrap(auditFailure));
                }
            });
        }

        private <T> void infrastructureFailure(
            CompletableFuture<T> result,
            Throwable failure
        ) {
            markDown();
            result.completeExceptionally(unwrap(failure));
        }

        private void markUp() {
            IdentityRuntime.this.markUp();
        }

        private void markDown() {
            IdentityRuntime.this.markDown();
        }
    }
}
