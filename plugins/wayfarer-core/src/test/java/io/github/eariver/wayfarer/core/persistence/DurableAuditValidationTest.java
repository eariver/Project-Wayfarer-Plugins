package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;

class DurableAuditValidationTest {
    @Test
    void callerValidationFailureDoesNotReachDatabaseOrMarkAuditDown() {
        InternalDatabase database = mock(InternalDatabase.class);
        HealthRegistry health = new HealthRegistry(
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            () -> WayfarerLifecycleState.ENABLED
        );
        health.update(HealthRegistry.AUDIT, WayfarerHealth.Status.UP, "available");
        DurableAudit audit = new DurableAudit(
            database,
            health,
            ignored -> {},
            "test-server",
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            Duration.ofSeconds(1)
        );

        assertThrows(
            CompletionException.class,
            () -> audit.record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                "invalid-lowercase",
                null,
                "CORE",
                "subject",
                "test-server",
                "{}",
                Instant.EPOCH
            )).toCompletableFuture().join()
        );

        verifyNoInteractions(database);
        assertEquals(
            WayfarerHealth.Status.UP,
            health.snapshot().components().get(HealthRegistry.AUDIT).status()
        );
    }

    @Test
    void infrastructureFailureCanRecoverAndConflictDoesNotLowerHealth() {
        InternalDatabase database = mock(InternalDatabase.class);
        doReturn(
            CompletableFuture.failedFuture(new PersistenceException("failed")),
            CompletableFuture.completedFuture(
                MariaDbAuditRepository.WriteResult.INSERTED
            ),
            CompletableFuture.completedFuture(
                MariaDbAuditRepository.WriteResult.CONFLICT
            )
        ).when(database).transaction(any());
        HealthRegistry health = new HealthRegistry(
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            () -> WayfarerLifecycleState.ENABLED
        );
        DurableAudit audit = new DurableAudit(
            database,
            health,
            ignored -> {},
            "test-server",
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            Duration.ofSeconds(1)
        );

        assertThrows(
            CompletionException.class,
            () -> audit.record(validEvent()).toCompletableFuture().join()
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.AUDIT).status()
        );
        audit.record(validEvent()).toCompletableFuture().join();
        assertEquals(
            WayfarerHealth.Status.UP,
            health.snapshot().components().get(HealthRegistry.AUDIT).status()
        );
        assertThrows(
            CompletionException.class,
            () -> audit.record(validEvent()).toCompletableFuture().join()
        );
        assertEquals(
            WayfarerHealth.Status.UP,
            health.snapshot().components().get(HealthRegistry.AUDIT).status()
        );
    }

    @Test
    void closePersistsDisableEventThenRejectsFurtherRecords() {
        InternalDatabase database = mock(InternalDatabase.class);
        doReturn(CompletableFuture.completedFuture(
            MariaDbAuditRepository.WriteResult.INSERTED
        )).when(database).transaction(any());
        HealthRegistry health = new HealthRegistry(
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            () -> WayfarerLifecycleState.ENABLED
        );
        DurableAudit audit = new DurableAudit(
            database,
            health,
            ignored -> {},
            "test-server",
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            Duration.ofSeconds(1)
        );

        audit.close();

        assertEquals(
            WayfarerHealth.Status.DISABLED,
            health.snapshot().components().get(HealthRegistry.AUDIT).status()
        );
        assertThrows(
            CompletionException.class,
            () -> audit.record(validEvent()).toCompletableFuture().join()
        );
    }

    private static WayfarerAudit.AuditEvent validEvent() {
        return new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            "VALID_EVENT",
            null,
            "CORE",
            "subject",
            "test-server",
            null,
            Instant.EPOCH
        );
    }
}
