package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private static IdentityRuntime runtime(
        InternalDatabase database,
        WayfarerAudit audit
    ) {
        return new IdentityRuntime(
            database,
            audit,
            healthRegistry(),
            "test-server",
            CLOCK,
            () -> ITEM_ID
        );
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
