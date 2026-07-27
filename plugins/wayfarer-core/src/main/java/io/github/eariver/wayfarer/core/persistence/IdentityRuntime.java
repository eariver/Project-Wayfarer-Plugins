package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation;
import io.github.eariver.wayfarer.core.identity.PlayerIdentitySink;

import java.sql.PreparedStatement;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class IdentityRuntime implements PlayerIdentitySink, AutoCloseable {
    private final InternalDatabase database;
    private final MariaDbPlayerIdentityRepository players;
    private final DefaultWayfarerItemIdentity items;
    private final HealthRegistry health;
    private final AtomicBoolean open = new AtomicBoolean(true);

    IdentityRuntime(
        InternalDatabase database,
        WayfarerAudit audit,
        HealthRegistry health,
        String serverId,
        Clock clock,
        Supplier<UUID> uuidGenerator
    ) {
        this.database = Objects.requireNonNull(database, "database");
        this.players = new MariaDbPlayerIdentityRepository(database);
        this.items = new DefaultWayfarerItemIdentity(
            new MariaDbItemIdentityRepository(database),
            audit,
            health,
            serverId,
            clock,
            uuidGenerator,
            open::get
        );
        this.health = Objects.requireNonNull(health, "health");
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
        if (!open.get()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Identity is unavailable")
            );
        }
        CompletionStage<Void> operation;
        try {
            operation = players.upsert(observation);
        } catch (RuntimeException failure) {
            markDown();
            return CompletableFuture.failedFuture(failure);
        }
        return observe(operation);
    }

    CompletionStage<Optional<PlayerIdentityRecord>> findPlayer(UUID playerUuid) {
        if (!open.get()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Identity is unavailable")
            );
        }
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
        if (open.get()) {
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.UP,
                "Identity repositories available"
            );
        }
    }

    private void markDown() {
        if (open.get()) {
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.DOWN,
                "Identity repository operation failed"
            );
        }
    }

    @Override
    public void close() {
        if (open.compareAndSet(true, false)) {
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.DISABLED,
                "Identity services closed"
            );
        }
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable current = failure;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static final class DefaultWayfarerItemIdentity implements WayfarerItemIdentity {
        private final MariaDbItemIdentityRepository repository;
        private final WayfarerAudit audit;
        private final HealthRegistry health;
        private final String serverId;
        private final Clock clock;
        private final Supplier<UUID> uuidGenerator;
        private final java.util.function.BooleanSupplier open;

        private DefaultWayfarerItemIdentity(
            MariaDbItemIdentityRepository repository,
            WayfarerAudit audit,
            HealthRegistry health,
            String serverId,
            Clock clock,
            Supplier<UUID> uuidGenerator,
            java.util.function.BooleanSupplier open
        ) {
            this.repository = repository;
            this.audit = Objects.requireNonNull(audit, "audit");
            this.health = health;
            this.serverId = serverId;
            this.clock = clock;
            this.uuidGenerator = uuidGenerator;
            this.open = open;
        }

        @Override
        public CompletionStage<Identity> create(CreateRequest request) {
            if (!open.getAsBoolean()) {
                return unavailable();
            }
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
            if (!open.getAsBoolean()) {
                return unavailable();
            }
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
            if (!open.getAsBoolean()) {
                return unavailable();
            }
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
            if (open.getAsBoolean()) {
                health.update(
                    HealthRegistry.IDENTITY,
                    WayfarerHealth.Status.UP,
                    "Identity repositories available"
                );
            }
        }

        private void markDown() {
            if (open.getAsBoolean()) {
                health.update(
                    HealthRegistry.IDENTITY,
                    WayfarerHealth.Status.DOWN,
                    "Identity operation failed"
                );
            }
        }

        private static <T> CompletionStage<T> unavailable() {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Item identity is unavailable")
            );
        }
    }
}
