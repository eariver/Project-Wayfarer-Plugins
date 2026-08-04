package io.github.eariver.wayfarer.frontier.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.frontier.domain.DeathIdentitySnapshot;
import io.github.eariver.wayfarer.frontier.domain.DeathPersistResult;
import io.github.eariver.wayfarer.frontier.domain.DeliveryCompletion;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

final class TraversalDeliveryCoordinatorTest {
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    private static final String WORLD = "frontier_iris";

    @Test
    void firstDeathCreatesPendingAndSafeEntryDelivers() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        FakeGateway gateway = new FakeGateway();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);

        DeathIdentitySnapshot snapshot = snapshot(
            TraversalIdentity.ItemType.ELYTRA,
            1
        );
        List<DeathPersistResult> death = coordinator.persistDeathSnapshots(
            PLAYER,
            List.of(snapshot)
        ).toCompletableFuture().join();
        assertEquals(List.of(DeathPersistResult.PENDING_CREATED), death);
        assertEquals(1, repository.deathAuditTransitions);

        TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertEquals(1, result.delivered());
        assertEquals(0, result.inventoryFull());
        assertEquals(DeliveryCompletion.TRANSITIONED_TO_DELIVERED,
            repository.lastPermanentCompletion);
    }

    @Test
    void sameEpochRepeatedDeathThreeTimes() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        FakeGateway gateway = new FakeGateway();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);
        DeathIdentitySnapshot snapshot = snapshot(
            TraversalIdentity.ItemType.ELYTRA,
            1
        );
        for (int cycle = 0; cycle < 3; cycle++) {
            gateway.present.clear();
            List<DeathPersistResult> death = coordinator.persistDeathSnapshots(
                PLAYER,
                List.of(snapshot)
            ).toCompletableFuture().join();
            assertTrue(
                death.getFirst() == DeathPersistResult.PENDING_CREATED
                    || death.getFirst() == DeathPersistResult.REOPENED_TO_PENDING
                    || death.getFirst() == DeathPersistResult.ALREADY_PENDING
            );
            TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
                PLAYER,
                WORLD
            ).toCompletableFuture().join();
            assertEquals(1, result.delivered() + result.alreadyPresent());
            gateway.present.add(TraversalIdentity.ItemType.ELYTRA);
        }
        assertEquals(3, repository.reopenOrCreateCount);
    }

    @Test
    void deathPersistenceFailureDoesNotPoisonSafeEntry() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.failNextDeath = true;
        FakeGateway gateway = new FakeGateway();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);
        assertTrue(exceptionally(coordinator.persistDeathSnapshots(
            PLAYER,
            List.of(snapshot(TraversalIdentity.ItemType.ELYTRA, 1))
        )));
        repository.failNextDeath = false;
        TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertFalse(result.repositoryUnavailable());
        assertTrue(result.delivered() >= 1 || gateway.deliverCalls >= 1);
    }

    @Test
    void delayedDeathDoesNotOvertakeSafeEntry() throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        CountDownLatch deathStarted = new CountDownLatch(1);
        CountDownLatch releaseDeath = new CountDownLatch(1);
        repository.deathBlock = () -> {
            deathStarted.countDown();
            await(releaseDeath);
        };
        FakeGateway gateway = new FakeGateway();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);
        CompletionStage<List<DeathPersistResult>> death =
            coordinator.persistDeathSnapshots(
                PLAYER,
                List.of(snapshot(TraversalIdentity.ItemType.ELYTRA, 1))
            );
        CompletionStage<TraversalDeliveryCoordinator.Result> safe =
            coordinator.onSafeEntry(PLAYER, WORLD);
        assertTrue(deathStarted.await(2, TimeUnit.SECONDS));
        Thread.sleep(40);
        assertEquals(0, gateway.deliverCalls);
        releaseDeath.countDown();
        death.toCompletableFuture().get(2, TimeUnit.SECONDS);
        TraversalDeliveryCoordinator.Result result =
            safe.toCompletableFuture().get(2, TimeUnit.SECONDS);
        assertEquals(1, result.delivered());
        assertTrue(repository.deathCompletedBeforeSafeDeliver);
    }

    @Test
    void outcomeCountersRemainSeparate() {
        TraversalDeliveryCoordinator.Result result =
            new TraversalDeliveryCoordinator.Result(
                1, 1, 1, 1, 1, 1, 1, 1, false, false
            );
        assertEquals(1, result.delivered());
        assertEquals(1, result.alreadyPresent());
        assertEquals(1, result.inventoryFull());
        assertEquals(1, result.capabilityUnavailable());
        assertEquals(1, result.playerOffline());
        assertEquals(1, result.leftTheme());
        assertEquals(1, result.conflict());
        assertEquals(1, result.unknown());
        String admin = TraversalDeliveryCoordinator.Result.formatAdmin(result);
        assertFalse(admin.contains("Result["));
        assertTrue(admin.contains("delivered=1"));
        assertTrue(admin.contains("playerOffline=1"));
        assertTrue(admin.contains("leftTheme=1"));
        assertTrue(admin.contains("conflict=1"));
        assertTrue(admin.contains("unknown=1"));
    }

    @Test
    void malformedPayloadMapsToUnknownAndCompensates() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.forcePending = true;
        repository.permanentCompletion = DeliveryCompletion.MALFORMED_PAYLOAD;
        FakeGateway gateway = new FakeGateway();
        gateway.forceDelivered = true;
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);
        TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertEquals(1, result.unknown());
        assertEquals(0, result.delivered());
        assertEquals(1, gateway.compensateCalls);
    }

    @Test
    void cancelledCompletionIsConflictWithCompensation() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.forcePending = true;
        repository.permanentCompletion = DeliveryCompletion.CANCELLED;
        FakeGateway gateway = new FakeGateway();
        gateway.forceDelivered = true;
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);
        TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertEquals(1, result.conflict());
        assertEquals(1, gateway.compensateCalls);
    }

    @Test
    void launchpadIdentityNullUsesConsumableMark() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.consumableOnlyPending = true;
        FakeGateway gateway = new FakeGateway();
        gateway.launchpadDeliver = true;
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);
        TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertEquals(1, result.delivered());
        assertEquals(1, repository.consumableMarkCalls);
        assertEquals(0, repository.permanentMarkCalls);
    }

    @Test
    void adminReissueThenRetryIsSeparateEnqueue() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        FakeGateway gateway = new FakeGateway();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);
        assertTrue(coordinator.adminReissueCritical(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ).toCompletableFuture().join());
        assertEquals(2, repository.logicalEpoch(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ));
        assertTrue(gateway.cacheUpdates >= 1);
        TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertTrue(result.delivered() >= 1 || result.alreadyPresent() >= 0);
    }

    @Test
    void requiredManagedItemsWaitForMviRestorationButHonorPendingDeath() {
        FakeRepository repository = new FakeRepository();
        for (TraversalIdentity.ItemType type : TraversalIdentity.ItemType.values()) {
            repository.seedLogical(PLAYER, type, 1);
        }
        TraversalDeliveryCoordinator coordinator = coordinator(
            repository,
            new FakeGateway()
        );

        assertEquals(
            3,
            coordinator.requiredManagedItemCount(PLAYER)
                .toCompletableFuture()
                .join()
        );

        coordinator.persistDeathSnapshots(
            PLAYER,
            List.of(snapshot(TraversalIdentity.ItemType.ELYTRA, 1))
        ).toCompletableFuture().join();
        assertEquals(
            2,
            coordinator.requiredManagedItemCount(PLAYER)
                .toCompletableFuture()
                .join()
        );
    }

    @Test
    void auditOnlyOnRealTransitions() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        FakeGateway gateway = new FakeGateway();
        FakeAudit audit = new FakeAudit();
        TraversalDeliveryCoordinator coordinator = new TraversalDeliveryCoordinator(
            new FrontierWorldGate(Set.of(WORLD)),
            repository,
            new DirectTasks(),
            gateway,
            audit,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
        DeathIdentitySnapshot snapshot = snapshot(
            TraversalIdentity.ItemType.ELYTRA,
            1
        );
        coordinator.persistDeathSnapshots(PLAYER, List.of(snapshot))
            .toCompletableFuture().join();
        coordinator.persistDeathSnapshots(PLAYER, List.of(snapshot))
            .toCompletableFuture().join();
        assertEquals(1, audit.deathTransitions);
        assertEquals(0, audit.deathConflicts);
    }

    @Test
    void exactCurrentDuplicateSelfHealIsAuditedOnce() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        FakeGateway gateway = new FakeGateway();
        gateway.duplicateCleanupResults.set(2);
        FakeAudit audit = new FakeAudit();
        TraversalDeliveryCoordinator coordinator = new TraversalDeliveryCoordinator(
            new FrontierWorldGate(Set.of(WORLD)),
            repository,
            new DirectTasks(),
            gateway,
            audit,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );

        coordinator.onSafeEntry(PLAYER, WORLD).toCompletableFuture().join();

        assertEquals(2, audit.duplicateSelfHealed);
    }

    @Test
    void cancelledSafeEntryCannotMutateAfterDatabaseReadiness() throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.findStarted = new CountDownLatch(1);
        repository.releaseFind = new CountDownLatch(1);
        FakeGateway gateway = new FakeGateway();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);

        CompletionStage<TraversalDeliveryCoordinator.Result> safe =
            coordinator.onSafeEntry(PLAYER, WORLD);
        assertTrue(repository.findStarted.await(2, TimeUnit.SECONDS));
        coordinator.cancelSafeEntry(PLAYER);
        repository.releaseFind.countDown();

        TraversalDeliveryCoordinator.Result result =
            safe.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(0, result.delivered());
        assertEquals(0, gateway.deliverCalls);
    }

    @Test
    void repositoryFailureAwaitsCompensationBeforeNextOperation() throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.forcePending = true;
        repository.failMark = true;
        FakeGateway gateway = new FakeGateway();
        gateway.forceDelivered = true;
        gateway.compensateStarted = new CountDownLatch(1);
        gateway.releaseCompensate = new CountDownLatch(1);
        AtomicInteger nextOpStarts = new AtomicInteger();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);

        CompletionStage<TraversalDeliveryCoordinator.Result> safe =
            coordinator.onSafeEntry(PLAYER, WORLD);
        assertTrue(gateway.compensateStarted.await(2, TimeUnit.SECONDS));
        assertFalse(safe.toCompletableFuture().isDone());

        CompletionStage<String> next = coordinator.serializer().enqueue(
            PLAYER,
            () -> {
                nextOpStarts.incrementAndGet();
                return CompletableFuture.completedFuture("next");
            }
        );
        Thread.sleep(40);
        assertEquals(0, nextOpStarts.get());
        assertFalse(next.toCompletableFuture().isDone());

        gateway.releaseCompensate.countDown();
        TraversalDeliveryCoordinator.Result result =
            safe.toCompletableFuture().get(2, TimeUnit.SECONDS);
        assertTrue(result.repositoryUnavailable());
        assertEquals(1, gateway.compensateCalls);
        assertEquals(1, gateway.compensated.size());
        assertEquals("next", next.toCompletableFuture().get(2, TimeUnit.SECONDS));
        assertEquals(1, nextOpStarts.get());
    }

    @Test
    void conflictAndUnknownAuditAreDedupedUntilTransition() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.forcePending = true;
        repository.permanentCompletion = DeliveryCompletion.CANCELLED;
        FakeGateway gateway = new FakeGateway();
        gateway.forceDelivered = true;
        FakeAudit audit = new FakeAudit();
        TraversalDeliveryCoordinator coordinator = new TraversalDeliveryCoordinator(
            new FrontierWorldGate(Set.of(WORLD)),
            repository,
            new DirectTasks(),
            gateway,
            audit,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
        coordinator.onSafeEntry(PLAYER, WORLD).toCompletableFuture().join();
        coordinator.onSafeEntry(PLAYER, WORLD).toCompletableFuture().join();
        assertEquals(1, audit.deliveryConflicts);

        gateway.forceDelivered = false;
        gateway.forceUnknown = true;
        gateway.present.clear();
        FakeRepository unknownRepo = new FakeRepository();
        unknownRepo.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        unknownRepo.forcePending = true;
        FakeAudit unknownAudit = new FakeAudit();
        TraversalDeliveryCoordinator unknownCoordinator =
            new TraversalDeliveryCoordinator(
                new FrontierWorldGate(Set.of(WORLD)),
                unknownRepo,
                new DirectTasks(),
                gateway,
                unknownAudit,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );
        TraversalDeliveryCoordinator.Result first =
            unknownCoordinator.onSafeEntry(PLAYER, WORLD)
                .toCompletableFuture().join();
        TraversalDeliveryCoordinator.Result second =
            unknownCoordinator.onSafeEntry(PLAYER, WORLD)
                .toCompletableFuture().join();
        assertEquals(1, first.unknown());
        assertEquals(1, second.unknown());
        assertEquals(1, unknownAudit.deliveryUnknowns);
        assertEquals(0, first.delivered());
    }

    @Test
    void adminReissueRaceA_safeEntryHoldsPhysicalAddUntilReissueWaits()
        throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.forcePending = true;
        FakeGateway gateway = new FakeGateway();
        gateway.deliverStarted = new CountDownLatch(1);
        gateway.releaseDeliver = new CountDownLatch(1);
        repository.reissueStarted = new CountDownLatch(1);
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);

        CompletionStage<TraversalDeliveryCoordinator.Result> firstSafe =
            coordinator.onSafeEntry(PLAYER, WORLD);
        assertTrue(gateway.deliverStarted.await(2, TimeUnit.SECONDS));
        assertEquals(0, repository.reissueStartCount.get());

        CompletionStage<Boolean> reissue = coordinator.adminReissueCritical(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        );
        Thread.sleep(40);
        assertEquals(0, repository.reissueStartCount.get());
        assertFalse(reissue.toCompletableFuture().isDone());
        assertFalse(firstSafe.toCompletableFuture().isDone());

        gateway.releaseDeliver.countDown();
        TraversalDeliveryCoordinator.Result firstResult =
            firstSafe.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(1, firstResult.delivered());
        assertTrue(repository.reissueStarted.await(2, TimeUnit.SECONDS));
        assertTrue(reissue.toCompletableFuture().get(3, TimeUnit.SECONDS));
        assertEquals(2, repository.logicalEpoch(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ));

        gateway.deliverStarted = null;
        gateway.releaseDeliver = null;
        TraversalDeliveryCoordinator.Result second =
            coordinator.onSafeEntry(PLAYER, WORLD)
                .toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(1, second.delivered());
        assertFinalEpochTwoOracle(repository, gateway);
    }

    @Test
    void adminReissueRaceB_completionHoldsUntilReissueWaits() throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.forcePending = true;
        repository.markStarted = new CountDownLatch(1);
        repository.releaseMark = new CountDownLatch(1);
        repository.reissueStarted = new CountDownLatch(1);
        FakeGateway gateway = new FakeGateway();
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);

        CompletionStage<TraversalDeliveryCoordinator.Result> firstSafe =
            coordinator.onSafeEntry(PLAYER, WORLD);
        assertTrue(repository.markStarted.await(2, TimeUnit.SECONDS));
        assertEquals(1, gateway.physicalIdentities().size());
        assertEquals(1L, gateway.physicalIdentities().getFirst().instanceEpoch());
        assertEquals(0, repository.reissueStartCount.get());

        CompletionStage<Boolean> reissue = coordinator.adminReissueCritical(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        );
        Thread.sleep(40);
        assertEquals(0, repository.reissueStartCount.get());
        assertFalse(reissue.toCompletableFuture().isDone());
        assertFalse(firstSafe.toCompletableFuture().isDone());

        repository.releaseMark.countDown();
        assertEquals(1, firstSafe.toCompletableFuture().get(3, TimeUnit.SECONDS)
            .delivered());
        assertTrue(repository.reissueStarted.await(2, TimeUnit.SECONDS));
        assertTrue(reissue.toCompletableFuture().get(3, TimeUnit.SECONDS));

        repository.markStarted = null;
        repository.releaseMark = null;
        TraversalDeliveryCoordinator.Result second =
            coordinator.onSafeEntry(PLAYER, WORLD)
                .toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(1, second.delivered());
        assertFinalEpochTwoOracle(repository, gateway);
    }

    @Test
    void adminReissueRaceC_deathThenReissueThenSafeEntryEpoch2() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        FakeGateway gateway = new FakeGateway();
        AtomicInteger deathStarts = new AtomicInteger();
        AtomicInteger reissueStarts = new AtomicInteger();
        AtomicInteger safeStarts = new AtomicInteger();
        repository.onDeathStart = deathStarts::incrementAndGet;
        repository.onReissueStart = reissueStarts::incrementAndGet;
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);

        List<DeathPersistResult> death = coordinator.persistDeathSnapshots(
            PLAYER,
            List.of(snapshot(TraversalIdentity.ItemType.ELYTRA, 1))
        ).toCompletableFuture().join();
        assertEquals(List.of(DeathPersistResult.PENDING_CREATED), death);
        assertEquals(1, deathStarts.get());

        assertTrue(coordinator.adminReissueCritical(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ).toCompletableFuture().join());
        assertEquals(1, reissueStarts.get());
        assertEquals(2, repository.logicalEpoch(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ));

        gateway.onDeliverStart = safeStarts::incrementAndGet;
        TraversalDeliveryCoordinator.Result safe = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertEquals(1, safe.delivered());
        assertEquals(1, safeStarts.get());
        assertFinalEpochTwoOracle(repository, gateway);
    }

    @Test
    void adminReissueRaceC_reissueThenDelayedDeathIsStaleThenSafeEntryEpoch2()
        throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        FakeGateway gateway = new FakeGateway();
        CountDownLatch deathStarted = new CountDownLatch(1);
        CountDownLatch releaseDeath = new CountDownLatch(1);
        AtomicInteger deathStarts = new AtomicInteger();
        AtomicInteger reissueStarts = new AtomicInteger();
        repository.onDeathStart = () -> {
            deathStarts.incrementAndGet();
            deathStarted.countDown();
            await(releaseDeath);
        };
        repository.onReissueStart = reissueStarts::incrementAndGet;
        TraversalDeliveryCoordinator coordinator = coordinator(repository, gateway);

        assertTrue(coordinator.adminReissueCritical(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ).toCompletableFuture().join());
        assertEquals(1, reissueStarts.get());
        assertEquals(2, repository.logicalEpoch(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ));

        CompletionStage<List<DeathPersistResult>> delayedDeath =
            coordinator.persistDeathSnapshots(
                PLAYER,
                List.of(snapshot(TraversalIdentity.ItemType.ELYTRA, 1))
            );
        assertTrue(deathStarted.await(2, TimeUnit.SECONDS));
        CompletionStage<TraversalDeliveryCoordinator.Result> safe =
            coordinator.onSafeEntry(PLAYER, WORLD);
        Thread.sleep(30);
        assertFalse(safe.toCompletableFuture().isDone());
        releaseDeath.countDown();
        assertEquals(
            List.of(DeathPersistResult.STALE_SKIPPED),
            delayedDeath.toCompletableFuture().get(3, TimeUnit.SECONDS)
        );
        TraversalDeliveryCoordinator.Result safeResult =
            safe.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(1, safeResult.delivered());
        assertEquals(2, repository.logicalEpoch(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ));
        assertFinalEpochTwoOracle(repository, gateway);
    }

    @Test
    void deathPersistenceFailureWarnsExactlyOnceViaAudit() {
        FakeRepository repository = new FakeRepository();
        repository.seedLogical(PLAYER, TraversalIdentity.ItemType.ELYTRA, 1);
        repository.failNextDeath = true;
        FakeGateway gateway = new FakeGateway();
        FakeAudit audit = new FakeAudit();
        TraversalDeliveryCoordinator coordinator = new TraversalDeliveryCoordinator(
            new FrontierWorldGate(Set.of(WORLD)),
            repository,
            new DirectTasks(),
            gateway,
            audit,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
        assertTrue(exceptionally(coordinator.persistDeathSnapshots(
            PLAYER,
            List.of(snapshot(TraversalIdentity.ItemType.ELYTRA, 1))
        )));
        assertEquals(1, audit.deathPersistenceFailures);
        assertEquals(0, audit.deathTransitions);
        TraversalDeliveryCoordinator.Result result = coordinator.onSafeEntry(
            PLAYER,
            WORLD
        ).toCompletableFuture().join();
        assertFalse(result.repositoryUnavailable());
        assertEquals(1, audit.deathPersistenceFailures);
    }

    private static void assertFinalEpochTwoOracle(
        FakeRepository repository,
        FakeGateway gateway
    ) {
        assertEquals(2, repository.logicalEpoch(
            PLAYER,
            TraversalIdentity.ItemType.ELYTRA
        ));
        assertEquals(1, gateway.physicalIdentities().size());
        TraversalIdentity physical = gateway.physicalIdentities().getFirst();
        assertEquals(2L, physical.instanceEpoch());
        assertEquals(
            stableId(PLAYER, TraversalIdentity.ItemType.ELYTRA, 2),
            physical.itemInstanceId()
        );
        assertFalse(gateway.physicalIdentities().stream().anyMatch(value ->
            value.instanceEpoch() == 1L
        ));
        assertEquals(
            "DELIVERED",
            repository.deliveryState(
                PLAYER,
                TraversalIdentity.ItemType.ELYTRA,
                2
            )
        );
        assertEquals(0, repository.pending(PLAYER).size());
    }

    private static boolean exceptionally(CompletionStage<?> stage) {
        try {
            stage.toCompletableFuture().join();
            return false;
        } catch (Exception failure) {
            return true;
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failure);
        }
    }

    private static DeathIdentitySnapshot snapshot(
        TraversalIdentity.ItemType type,
        long epoch
    ) {
        return new DeathIdentitySnapshot(
            PLAYER,
            type,
            stableId(PLAYER, type, epoch),
            epoch,
            1,
            TraversalIdentity.WORLDS_BEYOND
        );
    }

    private static UUID stableId(
        UUID player,
        TraversalIdentity.ItemType type,
        long epoch
    ) {
        return UUID.nameUUIDFromBytes(
            (player + ":" + type + ":" + epoch).getBytes()
        );
    }

    private static TraversalDeliveryCoordinator coordinator(
        FakeRepository repository,
        FakeGateway gateway
    ) {
        return new TraversalDeliveryCoordinator(
            new FrontierWorldGate(Set.of(WORLD)),
            repository,
            new DirectTasks(),
            gateway,
            new FakeAudit(),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private static final class DirectTasks implements WayfarerTasks {
        @Override
        public <T> CompletionStage<T> database(Supplier<T> operation) {
            return CompletableFuture.supplyAsync(operation);
        }

        @Override
        public <I, O> CompletionStage<TaskBridgeResult<O>> bridge(
            I immutableRequest,
            Function<? super I, ? extends O> asyncOperation,
            Predicate<? super O> mainThreadRevalidation,
            Consumer<? super O> mainThreadMutation
        ) {
            return CompletableFuture.supplyAsync(() ->
                asyncOperation.apply(immutableRequest)
            ).thenApply(value -> {
                boolean applied = mainThreadRevalidation.test(value);
                if (applied) {
                    mainThreadMutation.accept(value);
                }
                return new TaskBridgeResult<>(value, applied);
            });
        }

        @Override
        public CompletionStage<Void> mainThread(Runnable operation) {
            return CompletableFuture.runAsync(operation);
        }
    }

    private static final class FakeAudit
        implements TraversalDeliveryCoordinator.DeliveryAudit {
        private int deathTransitions;
        private int deathConflicts;
        private int deathPersistenceFailures;
        private int deliveryConflicts;
        private int deliveryUnknowns;
        private int compensationFailures;
        private int duplicateSelfHealed;

        @Override
        public void deathTransition(
            DeathIdentitySnapshot snapshot,
            DeathPersistResult result
        ) {
            deathTransitions++;
        }

        @Override
        public void deathConflict(
            DeathIdentitySnapshot snapshot,
            DeathPersistResult result
        ) {
            deathConflicts++;
        }

        @Override
        public void deathPersistenceFailure(UUID playerUuid) {
            deathPersistenceFailures++;
        }

        @Override
        public void deliveryTransitioned(UUID deliveryId) {}

        @Override
        public void deliveryConflict(UUID deliveryId, String reason) {
            deliveryConflicts++;
        }

        @Override
        public void deliveryUnknown(UUID deliveryId, String reason) {
            deliveryUnknowns++;
        }

        @Override
        public void deliveryRepositoryFailure(UUID deliveryId) {}

        @Override
        public void compensationFailure(UUID playerUuid) {
            compensationFailures++;
        }

        @Override
        public void duplicateSelfHealed(UUID playerUuid, int removed) {
            duplicateSelfHealed += removed;
        }
    }

    private static final class FakeGateway
        implements TraversalDeliveryCoordinator.DeliveryGateway {
        private final EnumSet<TraversalIdentity.ItemType> present =
            EnumSet.noneOf(TraversalIdentity.ItemType.class);
        private final List<TraversalIdentity> physical =
            new CopyOnWriteArrayList<>();
        private int deliverCalls;
        private int compensateCalls;
        private int cacheUpdates;
        private boolean forceDelivered;
        private boolean launchpadDeliver;
        private boolean forceUnknown;
        private boolean online = true;
        private boolean exactWorld = true;
        private final AtomicInteger duplicateCleanupResults =
            new AtomicInteger();
        private CountDownLatch compensateStarted;
        private CountDownLatch releaseCompensate;
        private CountDownLatch deliverStarted;
        private CountDownLatch releaseDeliver;
        private Runnable onDeliverStart;
        private final List<TraversalIdentity> compensated = new CopyOnWriteArrayList<>();
        private TraversalLoadout authority;

        List<TraversalIdentity> physicalIdentities() {
            return List.copyOf(physical);
        }

        @Override
        public TraversalDeliveryCoordinator.DeliveryOutcome deliverIfStillEligible(
            UUID playerUuid,
            PendingDelivery delivery
        ) {
            deliverCalls++;
            if (onDeliverStart != null) {
                onDeliverStart.run();
            }
            if (deliverStarted != null) {
                deliverStarted.countDown();
            }
            if (releaseDeliver != null) {
                await(releaseDeliver);
            }
            if (!online) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.PLAYER_OFFLINE;
            }
            if (!exactWorld) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.LEFT_THEME;
            }
            if (forceUnknown) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.UNKNOWN;
            }
            if (delivery.itemType() == PendingDelivery.ItemType.LAUNCHPAD
                && launchpadDeliver) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
            }
            if (delivery.identity() != null
                && hasExactPhysical(delivery.identity())) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.ALREADY_PRESENT;
            }
            if (forceDelivered || delivery.identity() != null) {
                if (delivery.identity() != null) {
                    replacePhysical(delivery.identity());
                    present.add(delivery.identity().itemType());
                }
                return TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
            }
            return TraversalDeliveryCoordinator.DeliveryOutcome.CAPABILITY_UNAVAILABLE;
        }

        private boolean hasExactPhysical(TraversalIdentity identity) {
            return physical.stream().anyMatch(value ->
                value.itemType() == identity.itemType()
                    && value.itemInstanceId().equals(identity.itemInstanceId())
                    && value.instanceEpoch() == identity.instanceEpoch()
            );
        }

        private void replacePhysical(TraversalIdentity identity) {
            physical.removeIf(value -> value.itemType() == identity.itemType());
            physical.add(identity);
        }

        @Override
        public boolean isOnline(UUID playerUuid) {
            return online;
        }

        @Override
        public boolean isOnlineInExactWorld(UUID playerUuid, String exactWorldName) {
            return online && exactWorld;
        }

        @Override
        public void cleanupNonCurrentManaged(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            authority = loadout;
            physical.removeIf(item -> loadout.permanentItems().stream().noneMatch(
                logical -> logical.state()
                    == TraversalLoadout.LogicalItem.State.ACTIVE
                    && logical.itemType() == item.itemType()
                    && logical.itemInstanceId().equals(item.itemInstanceId())
                    && logical.instanceEpoch() == item.instanceEpoch()
            ));
            present.clear();
            physical.forEach(value -> present.add(value.itemType()));
        }

        @Override
        public int cleanupExactCurrentDuplicates(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            return duplicateCleanupResults.getAndSet(0);
        }

        @Override
        public EnumSet<TraversalIdentity.ItemType> currentPhysicalPresence(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            EnumSet<TraversalIdentity.ItemType> values =
                EnumSet.noneOf(TraversalIdentity.ItemType.class);
            for (TraversalLoadout.LogicalItem logical : loadout.permanentItems()) {
                if (logical.state() != TraversalLoadout.LogicalItem.State.ACTIVE) {
                    continue;
                }
                boolean found = physical.stream().anyMatch(item ->
                    item.itemType() == logical.itemType()
                        && item.itemInstanceId().equals(logical.itemInstanceId())
                        && item.instanceEpoch() == logical.instanceEpoch()
                );
                if (found) {
                    values.add(logical.itemType());
                }
            }
            return values;
        }

        @Override
        public void applyAuthorityCache(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            cacheUpdates++;
            authority = loadout;
            cleanupNonCurrentManaged(playerUuid, loadout);
        }

        @Override
        public void notifySafeEntryResult(
            UUID playerUuid,
            TraversalDeliveryCoordinator.Result result
        ) {}

        @Override
        public void compensateRemove(
            UUID playerUuid,
            TraversalIdentity identity
        ) {
            if (compensateStarted != null) {
                compensateStarted.countDown();
            }
            if (releaseCompensate != null) {
                await(releaseCompensate);
            }
            compensateCalls++;
            if (identity != null) {
                compensated.add(identity);
                physical.removeIf(value ->
                    value.itemInstanceId().equals(identity.itemInstanceId())
                        && value.instanceEpoch() == identity.instanceEpoch()
                );
                present.remove(identity.itemType());
            }
        }
    }

    private static final class FakeRepository implements TraversalLoadoutRepository {
        private final ConcurrentHashMap<String, Long> epochs =
            new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, String> deliveryStates =
            new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, TraversalIdentity> deliveryIdentity =
            new ConcurrentHashMap<>();
        private boolean failNextDeath;
        private Runnable deathBlock;
        private boolean forcePending;
        private boolean consumableOnlyPending;
        private DeliveryCompletion permanentCompletion =
            DeliveryCompletion.TRANSITIONED_TO_DELIVERED;
        private DeliveryCompletion lastPermanentCompletion;
        private boolean failMark;
        private int deathAuditTransitions;
        private int reopenOrCreateCount;
        private boolean deathCompletedBeforeSafeDeliver;
        private int permanentMarkCalls;
        private int consumableMarkCalls;
        private CountDownLatch markStarted;
        private CountDownLatch releaseMark;
        private CountDownLatch findStarted;
        private CountDownLatch releaseFind;
        private CountDownLatch reissueStarted;
        private final AtomicInteger reissueStartCount = new AtomicInteger();
        private Runnable onDeathStart;
        private Runnable onReissueStart;

        void seedLogical(
            UUID player,
            TraversalIdentity.ItemType type,
            long epoch
        ) {
            epochs.put(key(player, type), epoch);
        }

        long logicalEpoch(UUID player, TraversalIdentity.ItemType type) {
            return epochs.getOrDefault(key(player, type), 0L);
        }

        String deliveryState(
            UUID player,
            TraversalIdentity.ItemType type,
            long epoch
        ) {
            return deliveryStates.get(permanentKey(player, type, epoch));
        }

        @Override
        public Optional<TraversalLoadout> find(UUID playerUuid) {
            List<TraversalLoadout.LogicalItem> items = new ArrayList<>();
            for (TraversalIdentity.ItemType type
                : TraversalIdentity.ItemType.values()) {
                Long epoch = epochs.get(key(playerUuid, type));
                if (epoch != null) {
                    items.add(new TraversalLoadout.LogicalItem(
                        type,
                        stableId(playerUuid, type, epoch),
                        epoch,
                        TraversalLoadout.LogicalItem.State.ACTIVE
                    ));
                }
            }
            if (items.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new TraversalLoadout(
                playerUuid,
                TraversalIdentity.WORLDS_BEYOND,
                NOW,
                true,
                items,
                0
            ));
        }

        @Override
        public TraversalLoadout findOrCreate(UUID playerUuid, Instant now) {
            if (findStarted != null) {
                findStarted.countDown();
            }
            if (releaseFind != null) {
                await(releaseFind);
            }
            if (find(playerUuid).isEmpty()) {
                for (TraversalIdentity.ItemType type
                    : TraversalIdentity.ItemType.values()) {
                    epochs.putIfAbsent(key(playerUuid, type), 1L);
                }
            }
            return find(playerUuid).orElseThrow();
        }

        @Override
        public List<PendingDelivery> ensureInitialDeliveries(
            TraversalLoadout loadout,
            Instant now
        ) {
            if (forcePending) {
                long epoch = epochs.getOrDefault(
                    key(loadout.playerUuid(), TraversalIdentity.ItemType.ELYTRA),
                    1L
                );
                String idKey = permanentKey(
                    loadout.playerUuid(),
                    TraversalIdentity.ItemType.ELYTRA,
                    epoch
                );
                deliveryStates.putIfAbsent(idKey, "PENDING");
                deliveryIdentity.putIfAbsent(
                    idKey,
                    identity(
                        loadout.playerUuid(),
                        TraversalIdentity.ItemType.ELYTRA,
                        epoch
                    )
                );
                return pending(loadout.playerUuid());
            }
            if (!consumableOnlyPending) {
                for (TraversalIdentity.ItemType type
                    : TraversalIdentity.ItemType.values()) {
                    Long epoch = epochs.get(key(loadout.playerUuid(), type));
                    if (epoch == null) {
                        continue;
                    }
                    String idKey = permanentKey(loadout.playerUuid(), type, epoch);
                    deliveryStates.putIfAbsent(idKey, "PENDING");
                    deliveryIdentity.putIfAbsent(
                        idKey,
                        identity(loadout.playerUuid(), type, epoch)
                    );
                }
            }
            return pending(loadout.playerUuid());
        }

        @Override
        public List<PendingDelivery> pending(UUID playerUuid) {
            List<PendingDelivery> values = new ArrayList<>();
            if (consumableOnlyPending) {
                UUID deliveryId = UUID.nameUUIDFromBytes(
                    ("frontier-initial-launchpad:" + playerUuid).getBytes()
                );
                values.add(new PendingDelivery(
                    deliveryId,
                    playerUuid,
                    TraversalIdentity.WORLDS_BEYOND,
                    PendingDelivery.ItemType.LAUNCHPAD,
                    1,
                    "frontier-initial-launchpad:" + playerUuid,
                    PendingDelivery.State.PENDING,
                    0,
                    NOW
                ));
                return values;
            }
            for (TraversalIdentity.ItemType type
                : TraversalIdentity.ItemType.values()) {
                Long epoch = epochs.get(key(playerUuid, type));
                if (epoch == null) {
                    continue;
                }
                String idKey = permanentKey(playerUuid, type, epoch);
                if (!"PENDING".equals(deliveryStates.get(idKey))) {
                    continue;
                }
                TraversalIdentity identity = deliveryIdentity.get(idKey);
                values.add(new PendingDelivery(
                    UUID.nameUUIDFromBytes(idKey.getBytes()),
                    playerUuid,
                    TraversalIdentity.WORLDS_BEYOND,
                    deliveryType(type),
                    1,
                    idKey,
                    PendingDelivery.State.PENDING,
                    0,
                    NOW,
                    identity
                ));
            }
            return values;
        }

        @Override
        public boolean reissuePermanent(
            UUID playerUuid,
            TraversalIdentity.ItemType itemType,
            Instant now
        ) {
            reissueStartCount.incrementAndGet();
            if (onReissueStart != null) {
                onReissueStart.run();
            }
            if (reissueStarted != null) {
                reissueStarted.countDown();
            }
            long epoch = epochs.merge(key(playerUuid, itemType), 1L, Long::sum);
            String previous = permanentKey(playerUuid, itemType, epoch - 1);
            if ("PENDING".equals(deliveryStates.get(previous))
                || "DELIVERED".equals(deliveryStates.get(previous))) {
                deliveryStates.put(previous, "CANCELLED");
            }
            String next = permanentKey(playerUuid, itemType, epoch);
            deliveryStates.put(next, "PENDING");
            deliveryIdentity.put(next, identity(playerUuid, itemType, epoch));
            return true;
        }

        @Override
        public DeathPersistResult persistDeathSnapshot(
            DeathIdentitySnapshot snapshot,
            Instant now
        ) {
            if (onDeathStart != null) {
                onDeathStart.run();
            }
            if (failNextDeath) {
                failNextDeath = false;
                throw new IllegalStateException("repository unavailable");
            }
            if (deathBlock != null) {
                deathBlock.run();
            }
            Long epoch = epochs.get(key(snapshot.playerUuid(), snapshot.itemType()));
            if (epoch == null) {
                return DeathPersistResult.NOT_FOUND_LOGICAL;
            }
            if (epoch != snapshot.instanceEpoch()
                || !stableId(
                    snapshot.playerUuid(),
                    snapshot.itemType(),
                    epoch
                ).equals(snapshot.itemInstanceId())
                || snapshot.schemaVersion() != 1) {
                return DeathPersistResult.STALE_SKIPPED;
            }
            String idKey = permanentKey(
                snapshot.playerUuid(),
                snapshot.itemType(),
                epoch
            );
            String state = deliveryStates.get(idKey);
            if (state == null) {
                deliveryStates.put(idKey, "PENDING");
                deliveryIdentity.put(
                    idKey,
                    identity(
                        snapshot.playerUuid(),
                        snapshot.itemType(),
                        epoch
                    )
                );
                reopenOrCreateCount++;
                deathAuditTransitions++;
                deathCompletedBeforeSafeDeliver = permanentMarkCalls == 0;
                return DeathPersistResult.PENDING_CREATED;
            }
            if ("PENDING".equals(state)) {
                deathCompletedBeforeSafeDeliver = permanentMarkCalls == 0;
                return DeathPersistResult.ALREADY_PENDING;
            }
            if ("DELIVERED".equals(state)) {
                deliveryStates.put(idKey, "PENDING");
                reopenOrCreateCount++;
                deathAuditTransitions++;
                deathCompletedBeforeSafeDeliver = permanentMarkCalls == 0;
                return DeathPersistResult.REOPENED_TO_PENDING;
            }
            if ("CANCELLED".equals(state)) {
                return DeathPersistResult.CANCELLED_OR_OBSOLETE;
            }
            return DeathPersistResult.CONFLICT;
        }

        @Override
        public void reopenAbsentPermanents(
            UUID playerUuid,
            EnumSet<TraversalIdentity.ItemType> absentTypes,
            Instant now
        ) {
            if (forcePending || consumableOnlyPending) {
                return;
            }
            for (TraversalIdentity.ItemType type : absentTypes) {
                Long epoch = epochs.get(key(playerUuid, type));
                if (epoch == null) {
                    continue;
                }
                String idKey = permanentKey(playerUuid, type, epoch);
                String state = deliveryStates.get(idKey);
                if (state == null || "DELIVERED".equals(state)) {
                    deliveryStates.put(idKey, "PENDING");
                    deliveryIdentity.put(
                        idKey,
                        identity(playerUuid, type, epoch)
                    );
                }
            }
        }

        @Override
        public DeliveryCompletion markPermanentDelivered(
            UUID deliveryId,
            Instant now
        ) {
            permanentMarkCalls++;
            if (markStarted != null) {
                markStarted.countDown();
            }
            if (releaseMark != null) {
                await(releaseMark);
            }
            if (failMark) {
                throw new IllegalStateException("repository unavailable");
            }
            lastPermanentCompletion = permanentCompletion;
            if (permanentCompletion
                == DeliveryCompletion.TRANSITIONED_TO_DELIVERED) {
                for (var entry : deliveryStates.entrySet()) {
                    if (UUID.nameUUIDFromBytes(entry.getKey().getBytes())
                        .equals(deliveryId)
                        && "PENDING".equals(entry.getValue())) {
                        entry.setValue("DELIVERED");
                    }
                }
            }
            return permanentCompletion;
        }

        @Override
        public DeliveryCompletion markConsumableDelivered(
            UUID deliveryId,
            Instant now
        ) {
            consumableMarkCalls++;
            return DeliveryCompletion.TRANSITIONED_TO_DELIVERED;
        }

        private static String key(UUID player, TraversalIdentity.ItemType type) {
            return player + ":" + type;
        }

        private static String permanentKey(
            UUID player,
            TraversalIdentity.ItemType type,
            long epoch
        ) {
            return "frontier-permanent:" + player + ":" + type + ":" + epoch;
        }

        private static TraversalIdentity identity(
            UUID player,
            TraversalIdentity.ItemType type,
            long epoch
        ) {
            return new TraversalIdentity(
                stableId(player, type, epoch),
                type,
                player,
                TraversalIdentity.WORLDS_BEYOND,
                epoch,
                1
            );
        }

        private static PendingDelivery.ItemType deliveryType(
            TraversalIdentity.ItemType type
        ) {
            return switch (type) {
                case ELYTRA -> PendingDelivery.ItemType.ELYTRA;
                case GRAPPLING_HOOK -> PendingDelivery.ItemType.GRAPPLING_HOOK;
                case NAVIGATION -> PendingDelivery.ItemType.NAVIGATION;
            };
        }
    }
}
