package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class IdentityRuntimeTest {
    private static final UUID ITEM_ID = UUID.fromString(
        "11111111-1111-1111-1111-111111111111"
    );
    private static final UUID OWNER_ID = UUID.fromString(
        "22222222-2222-2222-2222-222222222222"
    );
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-07-27T12:00:00.123456Z"),
        ZoneOffset.UTC
    );

    @Test
    void createCompletesOnlyAfterRepositoryInsertAndUsesInjectedUuid() {
        InternalDatabase database = mock(InternalDatabase.class);
        CompletableFuture<MariaDbItemIdentityRepository.InsertResult> insert =
            new CompletableFuture<>();
        doReturn(insert).when(database).transaction(any());
        IdentityRuntime runtime = runtime(database, completedAudit());

        var result = runtime.itemIdentity().create(
            new WayfarerItemIdentity.CreateRequest("type", OWNER_ID, 3, 1, 0)
        ).toCompletableFuture();

        assertFalse(result.isDone());
        insert.complete(MariaDbItemIdentityRepository.InsertResult.INSERTED);
        WayfarerItemIdentity.Identity identity = result.join();
        assertEquals(ITEM_ID, identity.itemInstanceId());
        assertEquals(Instant.parse("2026-07-27T12:00:00.123Z"), identity.createdAt());
    }

    @Test
    void requiredIdentityFailureAuditFailureMakesValidationExceptional() {
        InternalDatabase database = mock(InternalDatabase.class);
        WayfarerItemIdentity.Identity persisted = identity();
        doReturn(CompletableFuture.completedFuture(Optional.of(persisted)))
            .when(database).read(any());
        WayfarerAudit failingAudit = ignored -> CompletableFuture.failedFuture(
            new IllegalStateException("audit unavailable")
        );
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = new IdentityRuntime(
            database,
            failingAudit,
            health,
            "test-server",
            CLOCK,
            () -> ITEM_ID
        );

        var request = new WayfarerItemIdentity.ValidationRequest(
            new WayfarerItemIdentity.RawClaim(
                ITEM_ID.toString(),
                "type",
                OWNER_ID.toString(),
                3L,
                1,
                0
            ),
            Set.of("type"),
            Set.of(1),
            OWNER_ID,
            OptionalLong.of(4)
        );
        assertThrows(
            CompletionException.class,
            () -> runtime.itemIdentity().validate(request).toCompletableFuture().join()
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
    }

    @Test
    void repositoryFailureCanRecoverIdentityHealth() {
        InternalDatabase database = mock(InternalDatabase.class);
        doReturn(
            CompletableFuture.failedFuture(new PersistenceException("failed")),
            CompletableFuture.completedFuture(Optional.empty())
        ).when(database).read(any());
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = new IdentityRuntime(
            database,
            completedAudit(),
            health,
            "test-server",
            CLOCK,
            () -> ITEM_ID
        );

        assertThrows(
            CompletionException.class,
            () -> runtime.itemIdentity().find(ITEM_ID).toCompletableFuture().join()
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
        assertEquals(
            Optional.empty(),
            runtime.itemIdentity().find(ITEM_ID).toCompletableFuture().join()
        );
        assertEquals(
            WayfarerHealth.Status.UP,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
    }

    @Test
    void playerUpsertFailureIsAuditedWithOnlySafeIdentityFields() {
        InternalDatabase database = mock(InternalDatabase.class);
        doReturn(CompletableFuture.failedFuture(new PersistenceException("database failed")))
            .when(database).transaction(any());
        List<WayfarerAudit.AuditEvent> events = new ArrayList<>();
        WayfarerAudit recordingAudit = event -> {
            events.add(event);
            return CompletableFuture.completedFuture(null);
        };
        List<String> warnings = new ArrayList<>();
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = new IdentityRuntime(
            database,
            recordingAudit,
            health,
            "test-server",
            CLOCK,
            () -> ITEM_ID,
            warnings::add
        );

        assertThrows(
            CompletionException.class,
            () -> runtime.observe(observation()).toCompletableFuture().join()
        );

        assertEquals(1, events.size());
        WayfarerAudit.AuditEvent event = events.get(0);
        assertEquals("PLAYER_IDENTITY_UPSERT_FAILED", event.eventType());
        assertEquals("PLAYER_IDENTITY", event.subjectType());
        assertEquals(OWNER_ID.toString(), event.subjectId());
        assertEquals("test-server", event.serverId());
        assertEquals(
            "{\"failure_code\":\"PERSISTENCE_OPERATION_FAILED\"}",
            event.detailsJson()
        );
        assertFalse(event.detailsJson().contains("PlayerName"));
        assertEquals(List.of("Wayfarer player identity upsert failed"), warnings);
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
    }

    @Test
    void playerUpsertAndAuditFailureRemainExceptionalAndSanitized() {
        InternalDatabase database = mock(InternalDatabase.class);
        doReturn(CompletableFuture.failedFuture(new PersistenceException("database failed")))
            .when(database).transaction(any());
        WayfarerAudit failingAudit = ignored -> CompletableFuture.failedFuture(
            new PersistenceException("audit database failed")
        );
        List<String> warnings = new ArrayList<>();
        IdentityRuntime runtime = new IdentityRuntime(
            database,
            failingAudit,
            healthRegistry(),
            "test-server",
            CLOCK,
            () -> ITEM_ID,
            warnings::add
        );

        CompletionException failure = assertThrows(
            CompletionException.class,
            () -> runtime.observe(observation()).toCompletableFuture().join()
        );

        assertEquals(
            "Player identity upsert and failure audit failed",
            failure.getCause().getMessage()
        );
        assertEquals(
            List.of(
                "Wayfarer player identity upsert failed",
                "Wayfarer player identity failure audit failed"
            ),
            warnings
        );
    }

    @Test
    void acceptedSuccessRemainsObservableDuringClosingAndFinalizesCleanly() {
        InternalDatabase database = mock(InternalDatabase.class);
        CompletableFuture<Optional<WayfarerItemIdentity.Identity>> pending =
            new CompletableFuture<>();
        doReturn(pending).when(database).read(any());
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = runtime(database, completedAudit(), health);

        CompletableFuture<Optional<WayfarerItemIdentity.Identity>> accepted =
            runtime.itemIdentity().find(ITEM_ID).toCompletableFuture();
        runtime.close();

        assertEquals(IdentityRuntime.LifecycleState.CLOSING, runtime.lifecycleState());
        assertThrows(
            CompletionException.class,
            () -> runtime.itemIdentity().find(ITEM_ID).toCompletableFuture().join()
        );
        pending.complete(Optional.empty());
        assertEquals(Optional.empty(), accepted.join());
        assertEquals(
            IdentityRuntime.IdentityCloseStatus.CLEAN,
            runtime.quiesce(Duration.ofSeconds(1))
        );
        assertEquals(
            WayfarerHealth.Status.UNKNOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
        assertEquals(
            IdentityRuntime.IdentityCloseStatus.CLEAN,
            runtime.finishClosing(drained(), Duration.ofSeconds(1))
        );
        assertEquals(
            WayfarerHealth.Status.DISABLED,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
    }

    @Test
    void acceptedFailureDuringClosingKeepsFinalHealthDown() {
        InternalDatabase database = mock(InternalDatabase.class);
        CompletableFuture<Optional<WayfarerItemIdentity.Identity>> pending =
            new CompletableFuture<>();
        doReturn(pending).when(database).read(any());
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = runtime(database, completedAudit(), health);

        CompletableFuture<Optional<WayfarerItemIdentity.Identity>> accepted =
            runtime.itemIdentity().find(ITEM_ID).toCompletableFuture();
        runtime.close();
        pending.completeExceptionally(new PersistenceException("database failed"));

        assertThrows(CompletionException.class, accepted::join);
        assertEquals(
            IdentityRuntime.IdentityCloseStatus.FAILED,
            runtime.finishClosing(drained(), Duration.ofSeconds(1))
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
    }

    @Test
    void finalizationTimeoutRejectsFalseCleanShutdown() {
        InternalDatabase database = mock(InternalDatabase.class);
        CompletableFuture<Optional<WayfarerItemIdentity.Identity>> pending =
            new CompletableFuture<>();
        doReturn(pending).when(database).read(any());
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = runtime(database, completedAudit(), health);
        runtime.itemIdentity().find(ITEM_ID);
        runtime.close();

        assertEquals(
            IdentityRuntime.IdentityCloseStatus.TIMED_OUT,
            runtime.finishClosing(drained(), Duration.ofMillis(1))
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
        pending.complete(Optional.empty());
    }

    @Test
    void interruptedFinalizationRestoresInterruptAndStaysDown() {
        InternalDatabase database = mock(InternalDatabase.class);
        CompletableFuture<Optional<WayfarerItemIdentity.Identity>> pending =
            new CompletableFuture<>();
        doReturn(pending).when(database).read(any());
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = runtime(database, completedAudit(), health);
        runtime.itemIdentity().find(ITEM_ID);
        runtime.close();

        Thread.currentThread().interrupt();
        try {
            assertEquals(
                IdentityRuntime.IdentityCloseStatus.INTERRUPTED,
                runtime.finishClosing(drained(), Duration.ofSeconds(1))
            );
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
            pending.complete(Optional.empty());
        }
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
    }

    @Test
    void nonDrainedDatabaseResultCannotFinalizeIdentityCleanly() {
        HealthRegistry health = healthRegistry();
        IdentityRuntime runtime = runtime(
            mock(InternalDatabase.class),
            completedAudit(),
            health
        );
        runtime.close();

        assertEquals(
            IdentityRuntime.IdentityCloseStatus.FAILED,
            runtime.finishClosing(
                new PersistenceDrainResult(PersistenceDrainStatus.INTERRUPTED, 1),
                Duration.ofSeconds(1)
            )
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            health.snapshot().components().get(HealthRegistry.IDENTITY).status()
        );
    }

    private static IdentityRuntime runtime(
        InternalDatabase database,
        WayfarerAudit audit
    ) {
        return runtime(database, audit, healthRegistry());
    }

    private static IdentityRuntime runtime(
        InternalDatabase database,
        WayfarerAudit audit,
        HealthRegistry health
    ) {
        return new IdentityRuntime(
            database,
            audit,
            health,
            "test-server",
            CLOCK,
            () -> ITEM_ID
        );
    }

    private static PlayerIdentityObservation observation() {
        return new PlayerIdentityObservation(
            OWNER_ID,
            "PlayerName",
            "test-server",
            CLOCK.instant()
        );
    }

    private static PersistenceDrainResult drained() {
        return new PersistenceDrainResult(PersistenceDrainStatus.DRAINED, 0);
    }

    private static HealthRegistry healthRegistry() {
        return new HealthRegistry(CLOCK, () -> WayfarerLifecycleState.ENABLED);
    }

    private static WayfarerAudit completedAudit() {
        return ignored -> CompletableFuture.completedFuture(null);
    }

    private static WayfarerItemIdentity.Identity identity() {
        Instant now = CLOCK.instant();
        return new WayfarerItemIdentity.Identity(
            ITEM_ID,
            "type",
            OWNER_ID,
            3,
            1,
            0,
            now,
            now,
            0
        );
    }
}
