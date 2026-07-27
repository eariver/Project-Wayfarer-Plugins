package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import io.github.eariver.wayfarer.core.health.HealthRegistry;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class DurableAudit implements WayfarerAudit, AutoCloseable {
    private final MariaDbAuditRepository repository;
    private final AuditEventValidator validator;
    private final HealthRegistry health;
    private final Consumer<String> warningSink;
    private final String serverId;
    private final Clock clock;
    private final Duration closeTimeout;
    private final AtomicBoolean open = new AtomicBoolean(true);

    DurableAudit(
        InternalDatabase database,
        HealthRegistry health,
        Consumer<String> warningSink,
        String serverId,
        Clock clock,
        Duration closeTimeout,
        SecretValue... secrets
    ) {
        this.repository = new MariaDbAuditRepository(database);
        this.validator = new AuditEventValidator(secrets);
        this.health = Objects.requireNonNull(health, "health");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.closeTimeout = Objects.requireNonNull(closeTimeout, "closeTimeout");
    }

    public CompletionStage<Void> initialize() {
        try {
            return observeInfrastructure(repository.probe())
                .thenCompose(ignored -> recordLifecycle("CORE_AUDIT_ENABLED"))
                .thenCompose(ignored -> recordLifecycle("CORE_MIGRATION_READY"));
        } catch (RuntimeException failure) {
            infrastructureFailure();
            return CompletableFuture.failedFuture(failure);
        }
    }

    @Override
    public CompletionStage<Void> record(AuditEvent event) {
        if (!open.get()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Audit is unavailable")
            );
        }
        return validateAndRecord(event);
    }

    private CompletionStage<Void> validateAndRecord(AuditEvent event) {
        AuditRecord validated;
        try {
            validated = validator.validate(event);
        } catch (RuntimeException failure) {
            if (failure instanceof AuditValidationException
                && failure.getMessage() != null
                && failure.getMessage().contains("sensitive")) {
                warn("Wayfarer audit rejected sensitive event content");
            }
            return CompletableFuture.failedFuture(failure);
        }
        try {
            return observeWrite(repository.record(validated));
        } catch (RuntimeException failure) {
            infrastructureFailure();
            return CompletableFuture.failedFuture(failure);
        }
    }

    private CompletionStage<Void> observeWrite(
        CompletionStage<MariaDbAuditRepository.WriteResult> operation
    ) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        operation.whenComplete((write, failure) -> {
            if (failure != null) {
                infrastructureFailure();
                result.completeExceptionally(unwrap(failure));
            } else if (write == MariaDbAuditRepository.WriteResult.CONFLICT) {
                result.completeExceptionally(
                    new IllegalStateException("Audit event ID conflicts with persisted content")
                );
            } else {
                if (open.get()) {
                    health.update(
                        HealthRegistry.AUDIT,
                        WayfarerHealth.Status.UP,
                        "Durable audit available"
                    );
                }
                result.complete(null);
            }
        });
        return result;
    }

    private CompletionStage<Void> observeInfrastructure(CompletionStage<Void> operation) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        operation.whenComplete((ignored, failure) -> {
            if (failure == null) {
                health.update(
                    HealthRegistry.AUDIT,
                    WayfarerHealth.Status.UP,
                    "Durable audit available"
                );
                result.complete(null);
            } else {
                infrastructureFailure();
                result.completeExceptionally(unwrap(failure));
            }
        });
        return result;
    }

    private CompletionStage<Void> recordLifecycle(String eventType) {
        return validateAndRecord(new AuditEvent(
            UUID.randomUUID(),
            eventType,
            null,
            "CORE",
            serverId,
            serverId,
            null,
            clock.instant()
        ));
    }

    @Override
    public void close() {
        if (!open.compareAndSet(true, false)) {
            return;
        }
        try {
            recordLifecycle("CORE_DISABLE_STARTED")
                .toCompletableFuture()
                .get(closeTimeout.toNanos(), TimeUnit.NANOSECONDS);
            health.update(
                HealthRegistry.AUDIT,
                WayfarerHealth.Status.DISABLED,
                "Durable audit closed"
            );
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            closeFailure("Wayfarer audit close was interrupted");
        } catch (Exception failure) {
            closeFailure("Wayfarer audit close did not complete");
        }
    }

    private void infrastructureFailure() {
        health.update(
            HealthRegistry.AUDIT,
            WayfarerHealth.Status.DOWN,
            "Durable audit operation failed"
        );
        warn("Wayfarer durable audit operation failed");
    }

    private void closeFailure(String warning) {
        health.update(
            HealthRegistry.AUDIT,
            WayfarerHealth.Status.DOWN,
            "Durable audit close failed"
        );
        warn(warning);
    }

    private void warn(String warning) {
        try {
            warningSink.accept(warning);
        } catch (RuntimeException ignored) {
            // Audit completion and health remain authoritative.
        }
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable current = failure;
        while ((current instanceof CompletionException
            || current instanceof java.util.concurrent.ExecutionException)
            && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
