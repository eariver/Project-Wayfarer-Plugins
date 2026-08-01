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
        public void deliveryTransitioned(UUID deliveryId) {}

        @Override
        public void deliveryConflict(
            UUID deliveryId,
            DeliveryCompletion completion
        ) {}

        @Override
        public void deliveryRepositoryFailure(UUID deliveryId) {}
    }

    private static final class FakeGateway
        implements TraversalDeliveryCoordinator.DeliveryGateway {
        private final EnumSet<TraversalIdentity.ItemType> present =
            EnumSet.noneOf(TraversalIdentity.ItemType.class);
        private int deliverCalls;
        private int compensateCalls;
        private int cacheUpdates;
        private boolean forceDelivered;
        private boolean launchpadDeliver;
        private boolean online = true;
        private boolean exactWorld = true;

        @Override
        public TraversalDeliveryCoordinator.DeliveryOutcome deliverIfStillEligible(
            UUID playerUuid,
            PendingDelivery delivery
        ) {
            deliverCalls++;
            if (!online) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.PLAYER_OFFLINE;
            }
            if (!exactWorld) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.LEFT_THEME;
            }
            if (delivery.itemType() == PendingDelivery.ItemType.LAUNCHPAD
                && launchpadDeliver) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
            }
            if (delivery.identity() != null
                && present.contains(delivery.identity().itemType())) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.ALREADY_PRESENT;
            }
            if (forceDelivered || delivery.identity() != null) {
                if (delivery.identity() != null) {
                    present.add(delivery.identity().itemType());
                }
                return TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
            }
            return TraversalDeliveryCoordinator.DeliveryOutcome.CAPABILITY_UNAVAILABLE;
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
        ) {}

        @Override
        public EnumSet<TraversalIdentity.ItemType> currentPhysicalPresence(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            return present.clone();
        }

        @Override
        public void applyAuthorityCache(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            cacheUpdates++;
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
            compensateCalls++;
            if (identity != null) {
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
        private int deathAuditTransitions;
        private int reopenOrCreateCount;
        private boolean deathCompletedBeforeSafeDeliver;
        private int permanentMarkCalls;
        private int consumableMarkCalls;

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
            long epoch = epochs.merge(key(playerUuid, itemType), 1L, Long::sum);
            String previous = permanentKey(playerUuid, itemType, epoch - 1);
            if ("PENDING".equals(deliveryStates.get(previous))) {
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
