package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

final class GrowthToolDeliveryCoordinatorTest {
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Test
    void marksDeliveredOnlyAfterMainThreadMutationSucceeds() {
        FakeRepository repository = new FakeRepository(tool(GrowthTool.DeliveryStatus.PENDING));
        GrowthToolDeliveryCoordinator coordinator = coordinator(
            repository,
            ignored -> GrowthToolDeliveryCoordinator.Outcome.DELIVERED
        );

        assertEquals(
            GrowthToolDeliveryCoordinator.Outcome.DELIVERED,
            coordinator.onJoin(PLAYER).toCompletableFuture().join()
        );
        assertEquals(1, repository.markCalls);
    }

    @Test
    void keepsInventoryFullDeliveryPendingWithoutDropping() {
        FakeRepository repository = new FakeRepository(tool(GrowthTool.DeliveryStatus.PENDING));
        GrowthToolDeliveryCoordinator coordinator = coordinator(
            repository,
            ignored -> GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL
        );

        assertEquals(
            GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL,
            coordinator.onJoin(PLAYER).toCompletableFuture().join()
        );
        assertEquals(0, repository.markCalls);
    }

    @Test
    void doesNotReissueDeliveredRecord() {
        FakeRepository repository = new FakeRepository(tool(GrowthTool.DeliveryStatus.DELIVERED));
        GrowthToolDeliveryCoordinator coordinator = coordinator(
            repository,
            ignored -> {
                throw new AssertionError("Delivery gateway must not run");
            }
        );

        assertEquals(
            GrowthToolDeliveryCoordinator.Outcome.ALREADY_DELIVERED,
            coordinator.onJoin(PLAYER).toCompletableFuture().join()
        );
    }

    private static GrowthToolDeliveryCoordinator coordinator(
        FakeRepository repository,
        GrowthToolDeliveryCoordinator.DeliveryGateway gateway
    ) {
        WayfarerAudit audit = event -> CompletableFuture.completedFuture(null);
        return new GrowthToolDeliveryCoordinator(
            repository,
            new DirectTasks(),
            audit,
            gateway,
            "main-test",
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private static GrowthTool tool(GrowthTool.DeliveryStatus deliveryStatus) {
        return new GrowthTool(
            UUID.fromString("00000000-0000-0000-0000-000000000011"),
            PLAYER,
            1,
            0,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.ACTIVE,
            deliveryStatus,
            0,
            1,
            1,
            0,
            NOW
        );
    }

    private static final class FakeRepository implements GrowthToolRepository {
        private final GrowthTool tool;
        private int markCalls;

        private FakeRepository(GrowthTool tool) {
            this.tool = tool;
        }

        @Override
        public GrowthTool findOrCreate(UUID ownerUuid, Instant now) {
            return tool;
        }

        @Override
        public Optional<GrowthTool> findByOwner(UUID ownerUuid) {
            return Optional.of(tool);
        }

        @Override
        public boolean markDelivered(UUID toolId, long expectedLockVersion, Instant now) {
            markCalls++;
            return true;
        }

        @Override
        public Optional<GrowthTool> checkpoint(
            GrowthTool updated,
            long expectedLockVersion,
            Instant now
        ) {
            return Optional.of(updated);
        }

        @Override
        public Optional<GrowthTool> replaceAuthority(
            GrowthTool updated,
            long expectedLockVersion,
            Instant now
        ) {
            return Optional.of(updated);
        }
    }

    private static final class DirectTasks implements WayfarerTasks {
        @Override
        public <T> CompletionStage<T> database(Supplier<T> operation) {
            try {
                return CompletableFuture.completedFuture(operation.get());
            } catch (RuntimeException failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        @Override
        public <I, O> CompletionStage<TaskBridgeResult<O>> bridge(
            I immutableRequest,
            Function<? super I, ? extends O> asyncOperation,
            Predicate<? super O> mainThreadRevalidation,
            Consumer<? super O> mainThreadMutation
        ) {
            O value = asyncOperation.apply(immutableRequest);
            boolean applied = mainThreadRevalidation.test(value);
            if (applied) {
                mainThreadMutation.accept(value);
            }
            return CompletableFuture.completedFuture(new TaskBridgeResult<>(value, applied));
        }

        @Override
        public CompletionStage<Void> mainThread(Runnable operation) {
            operation.run();
            return CompletableFuture.completedFuture(null);
        }
    }
}
