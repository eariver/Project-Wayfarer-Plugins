package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.frontier.domain.DeathIdentitySnapshot;
import io.github.eariver.wayfarer.frontier.domain.DeathPersistResult;
import io.github.eariver.wayfarer.frontier.domain.DeliveryCompletion;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;
import java.time.Clock;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public final class TraversalDeliveryCoordinator {
    private final FrontierWorldGate worldGate;
    private final TraversalLoadoutRepository repository;
    private final WayfarerTasks tasks;
    private final DeliveryGateway gateway;
    private final DeliveryAudit audit;
    private final Clock clock;
    private final PlayerOperationSerializer serializer;

    public TraversalDeliveryCoordinator(
        FrontierWorldGate worldGate,
        TraversalLoadoutRepository repository,
        WayfarerTasks tasks,
        DeliveryGateway gateway,
        DeliveryAudit audit,
        Clock clock
    ) {
        this(
            worldGate,
            repository,
            tasks,
            gateway,
            audit,
            clock,
            new PlayerOperationSerializer()
        );
    }

    public TraversalDeliveryCoordinator(
        FrontierWorldGate worldGate,
        TraversalLoadoutRepository repository,
        WayfarerTasks tasks,
        DeliveryGateway gateway,
        DeliveryAudit audit,
        Clock clock,
        PlayerOperationSerializer serializer
    ) {
        this.worldGate = Objects.requireNonNull(worldGate, "worldGate");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
    }

    public PlayerOperationSerializer serializer() {
        return serializer;
    }

    public void shutdown() {
        serializer.shutdown();
    }

    public CompletionStage<List<DeathPersistResult>> persistDeathSnapshots(
        UUID playerUuid,
        List<DeathIdentitySnapshot> snapshots
    ) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        List<DeathIdentitySnapshot> copy = List.copyOf(snapshots);
        if (copy.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        return serializer.enqueue(playerUuid, () ->
            tasks.database(() -> {
                List<DeathPersistResult> results = new ArrayList<>(copy.size());
                for (DeathIdentitySnapshot snapshot : copy) {
                    DeathPersistResult result = repository.persistDeathSnapshot(
                        snapshot,
                        clock.instant()
                    );
                    results.add(result);
                    if (result == DeathPersistResult.PENDING_CREATED
                        || result == DeathPersistResult.REOPENED_TO_PENDING) {
                        audit.deathTransition(snapshot, result);
                    } else if (result == DeathPersistResult.CONFLICT) {
                        audit.deathConflict(snapshot, result);
                    }
                }
                return List.copyOf(results);
            })
        );
    }

    public CompletionStage<Result> onSafeEntry(UUID playerUuid, String exactWorldName) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(exactWorldName, "exactWorldName");
        if (!worldGate.allows(exactWorldName)) {
            return CompletableFuture.completedFuture(Result.rejectedWorld());
        }
        return serializer.enqueue(playerUuid, () -> safeEntryOperation(playerUuid, exactWorldName))
            .exceptionally(failure -> Result.unavailable());
    }

    public CompletionStage<Boolean> adminReissueCritical(
        UUID playerUuid,
        TraversalIdentity.ItemType itemType
    ) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(itemType, "itemType");
        return serializer.enqueue(playerUuid, () ->
            tasks.database(() -> {
                boolean applied = repository.reissuePermanent(
                    playerUuid,
                    itemType,
                    clock.instant()
                );
                if (!applied) {
                    return Optional.<TraversalLoadout>empty();
                }
                return repository.find(playerUuid);
            }).thenCompose(found -> {
                if (found.isEmpty()) {
                    return CompletableFuture.completedFuture(false);
                }
                return tasks.mainThread(() ->
                    gateway.applyAuthorityCache(playerUuid, found.orElseThrow())
                ).thenApply(ignored -> true);
            })
        );
    }

    private CompletionStage<Result> safeEntryOperation(
        UUID playerUuid,
        String exactWorldName
    ) {
        return tasks.database(() -> {
            TraversalLoadout loadout = repository.findOrCreate(
                playerUuid,
                clock.instant()
            );
            repository.ensureInitialDeliveries(loadout, clock.instant());
            return repository.find(playerUuid).orElse(loadout);
        }).thenCompose(loadout -> {
            PresenceCapture capture = new PresenceCapture();
            return tasks.mainThread(() -> {
                if (!gateway.isOnlineInExactWorld(playerUuid, exactWorldName)) {
                    capture.leftOrOffline = true;
                    capture.offline = !gateway.isOnline(playerUuid);
                    return;
                }
                gateway.cleanupNonCurrentManaged(playerUuid, loadout);
                capture.presentTypes = gateway.currentPhysicalPresence(
                    playerUuid,
                    loadout
                );
                gateway.applyAuthorityCache(playerUuid, loadout);
                capture.loadout = loadout;
            }).thenCompose(ignored -> {
                if (capture.leftOrOffline) {
                    Result empty = Result.empty();
                    if (capture.offline) {
                        return CompletableFuture.completedFuture(empty.plusOffline());
                    }
                    return CompletableFuture.completedFuture(empty.plusLeftTheme());
                }
                EnumSet<TraversalIdentity.ItemType> absent =
                    EnumSet.allOf(TraversalIdentity.ItemType.class);
                absent.removeAll(capture.presentTypes);
                return tasks.database(() -> {
                    repository.reopenAbsentPermanents(
                        playerUuid,
                        absent,
                        clock.instant()
                    );
                    return repository.pending(playerUuid);
                }).thenCompose(deliveries ->
                    deliverSequentially(playerUuid, deliveries, Result.empty())
                ).thenCompose(result -> tasks.database(() ->
                    repository.find(playerUuid)
                ).thenCompose(found -> tasks.mainThread(() -> {
                    found.ifPresent(latest -> {
                        gateway.applyAuthorityCache(playerUuid, latest);
                        gateway.cleanupNonCurrentManaged(playerUuid, latest);
                    });
                    gateway.notifySafeEntryResult(playerUuid, result);
                }).thenApply(nothing -> result)));
            });
        }).exceptionally(ignored -> Result.unavailable());
    }

    private CompletionStage<Result> deliverSequentially(
        UUID playerUuid,
        List<PendingDelivery> deliveries,
        Result seed
    ) {
        CompletionStage<Result> result = CompletableFuture.completedFuture(seed);
        for (PendingDelivery delivery : deliveries) {
            result = result.thenCompose(current ->
                deliverOne(playerUuid, delivery).thenApply(current::add)
            );
        }
        return result;
    }

    private CompletionStage<DeliveryOutcome> deliverOne(
        UUID playerUuid,
        PendingDelivery delivery
    ) {
        DeliveryCapture capture = new DeliveryCapture();
        return tasks.mainThread(() ->
            capture.outcome = gateway.deliverIfStillEligible(playerUuid, delivery)
        ).thenCompose(ignored -> {
            if (capture.outcome != DeliveryOutcome.DELIVERED
                && capture.outcome != DeliveryOutcome.ALREADY_PRESENT) {
                return CompletableFuture.completedFuture(capture.outcome);
            }
            boolean permanent = isPermanent(delivery.itemType());
            boolean physicalAdded = capture.outcome == DeliveryOutcome.DELIVERED;
            return tasks.database(() -> permanent
                ? repository.markPermanentDelivered(
                    delivery.deliveryId(),
                    clock.instant()
                )
                : repository.markConsumableDelivered(
                    delivery.deliveryId(),
                    clock.instant()
                )
            ).thenCompose(completion -> mapCompletion(
                playerUuid,
                delivery,
                capture.outcome,
                completion,
                physicalAdded
            )).exceptionally(failure -> {
                if (physicalAdded && delivery.identity() != null) {
                    tasks.mainThread(() ->
                        gateway.compensateRemove(playerUuid, delivery.identity())
                    );
                }
                audit.deliveryRepositoryFailure(delivery.deliveryId());
                return DeliveryOutcome.REPOSITORY_UNAVAILABLE;
            });
        });
    }

    private CompletionStage<DeliveryOutcome> mapCompletion(
        UUID playerUuid,
        PendingDelivery delivery,
        DeliveryOutcome gatewayOutcome,
        DeliveryCompletion completion,
        boolean physicalAdded
    ) {
        return switch (completion) {
            case TRANSITIONED_TO_DELIVERED -> {
                audit.deliveryTransitioned(delivery.deliveryId());
                yield CompletableFuture.completedFuture(
                    gatewayOutcome == DeliveryOutcome.DELIVERED
                        ? DeliveryOutcome.DELIVERED
                        : DeliveryOutcome.ALREADY_PRESENT
                );
            }
            case ALREADY_DELIVERED -> CompletableFuture.completedFuture(
                DeliveryOutcome.ALREADY_PRESENT
            );
            case MALFORMED_PAYLOAD -> compensateAndOutcome(
                playerUuid,
                delivery,
                physicalAdded,
                DeliveryOutcome.UNKNOWN,
                completion
            );
            case CANCELLED, NOT_FOUND, STALE_IDENTITY, TRUE_CONFLICT ->
                compensateAndOutcome(
                    playerUuid,
                    delivery,
                    physicalAdded,
                    DeliveryOutcome.CONFLICT,
                    completion
                );
        };
    }

    private CompletionStage<DeliveryOutcome> compensateAndOutcome(
        UUID playerUuid,
        PendingDelivery delivery,
        boolean physicalAdded,
        DeliveryOutcome outcome,
        DeliveryCompletion completion
    ) {
        audit.deliveryConflict(delivery.deliveryId(), completion);
        if (physicalAdded && delivery.identity() != null) {
            return tasks.mainThread(() ->
                gateway.compensateRemove(playerUuid, delivery.identity())
            ).thenApply(ignored -> outcome);
        }
        return CompletableFuture.completedFuture(outcome);
    }

    private static boolean isPermanent(PendingDelivery.ItemType type) {
        return switch (type) {
            case ELYTRA, GRAPPLING_HOOK, NAVIGATION -> true;
            case LAUNCHPAD, FIREWORK_ROCKET -> false;
        };
    }

    public interface DeliveryGateway {
        DeliveryOutcome deliverIfStillEligible(UUID playerUuid, PendingDelivery delivery);

        boolean isOnline(UUID playerUuid);

        boolean isOnlineInExactWorld(UUID playerUuid, String exactWorldName);

        void cleanupNonCurrentManaged(UUID playerUuid, TraversalLoadout loadout);

        EnumSet<TraversalIdentity.ItemType> currentPhysicalPresence(
            UUID playerUuid,
            TraversalLoadout loadout
        );

        void applyAuthorityCache(UUID playerUuid, TraversalLoadout loadout);

        void notifySafeEntryResult(UUID playerUuid, Result result);

        void compensateRemove(UUID playerUuid, TraversalIdentity identity);
    }

    public interface DeliveryAudit {
        void deathTransition(DeathIdentitySnapshot snapshot, DeathPersistResult result);

        void deathConflict(DeathIdentitySnapshot snapshot, DeathPersistResult result);

        void deliveryTransitioned(UUID deliveryId);

        void deliveryConflict(UUID deliveryId, DeliveryCompletion completion);

        void deliveryRepositoryFailure(UUID deliveryId);
    }

    public enum DeliveryOutcome {
        DELIVERED,
        ALREADY_PRESENT,
        INVENTORY_FULL,
        PLAYER_OFFLINE,
        LEFT_THEME,
        CAPABILITY_UNAVAILABLE,
        CONFLICT,
        UNKNOWN,
        REPOSITORY_UNAVAILABLE
    }

    public record Result(
        int delivered,
        int alreadyPresent,
        int inventoryFull,
        int capabilityUnavailable,
        int playerOffline,
        int leftTheme,
        int conflict,
        int unknown,
        boolean rejectedExactWorld,
        boolean repositoryUnavailable
    ) {
        public static Result empty() {
            return new Result(0, 0, 0, 0, 0, 0, 0, 0, false, false);
        }

        public static Result rejectedWorld() {
            return new Result(0, 0, 0, 0, 0, 0, 0, 0, true, false);
        }

        public static Result unavailable() {
            return new Result(0, 0, 0, 0, 0, 0, 0, 0, false, true);
        }

        public Result plusOffline() {
            return new Result(
                delivered,
                alreadyPresent,
                inventoryFull,
                capabilityUnavailable,
                playerOffline + 1,
                leftTheme,
                conflict,
                unknown,
                rejectedExactWorld,
                repositoryUnavailable
            );
        }

        public Result plusLeftTheme() {
            return new Result(
                delivered,
                alreadyPresent,
                inventoryFull,
                capabilityUnavailable,
                playerOffline,
                leftTheme + 1,
                conflict,
                unknown,
                rejectedExactWorld,
                repositoryUnavailable
            );
        }

        private Result add(DeliveryOutcome outcome) {
            return switch (outcome) {
                case DELIVERED -> new Result(
                    delivered + 1,
                    alreadyPresent,
                    inventoryFull,
                    capabilityUnavailable,
                    playerOffline,
                    leftTheme,
                    conflict,
                    unknown,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case ALREADY_PRESENT -> new Result(
                    delivered,
                    alreadyPresent + 1,
                    inventoryFull,
                    capabilityUnavailable,
                    playerOffline,
                    leftTheme,
                    conflict,
                    unknown,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case INVENTORY_FULL -> new Result(
                    delivered,
                    alreadyPresent,
                    inventoryFull + 1,
                    capabilityUnavailable,
                    playerOffline,
                    leftTheme,
                    conflict,
                    unknown,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case CAPABILITY_UNAVAILABLE -> new Result(
                    delivered,
                    alreadyPresent,
                    inventoryFull,
                    capabilityUnavailable + 1,
                    playerOffline,
                    leftTheme,
                    conflict,
                    unknown,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case PLAYER_OFFLINE -> new Result(
                    delivered,
                    alreadyPresent,
                    inventoryFull,
                    capabilityUnavailable,
                    playerOffline + 1,
                    leftTheme,
                    conflict,
                    unknown,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case LEFT_THEME -> new Result(
                    delivered,
                    alreadyPresent,
                    inventoryFull,
                    capabilityUnavailable,
                    playerOffline,
                    leftTheme + 1,
                    conflict,
                    unknown,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case CONFLICT -> new Result(
                    delivered,
                    alreadyPresent,
                    inventoryFull,
                    capabilityUnavailable,
                    playerOffline,
                    leftTheme,
                    conflict + 1,
                    unknown,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case UNKNOWN -> new Result(
                    delivered,
                    alreadyPresent,
                    inventoryFull,
                    capabilityUnavailable,
                    playerOffline,
                    leftTheme,
                    conflict,
                    unknown + 1,
                    rejectedExactWorld,
                    repositoryUnavailable
                );
                case REPOSITORY_UNAVAILABLE -> new Result(
                    delivered,
                    alreadyPresent,
                    inventoryFull,
                    capabilityUnavailable,
                    playerOffline,
                    leftTheme,
                    conflict,
                    unknown,
                    rejectedExactWorld,
                    true
                );
            };
        }

        public static String formatAdmin(Result result) {
            Objects.requireNonNull(result, "result");
            return "delivered=" + result.delivered
                + " alreadyPresent=" + result.alreadyPresent
                + " inventoryFull=" + result.inventoryFull
                + " capabilityUnavailable=" + result.capabilityUnavailable
                + " playerOffline=" + result.playerOffline
                + " leftTheme=" + result.leftTheme
                + " conflict=" + result.conflict
                + " unknown=" + result.unknown
                + " rejectedExactWorld=" + result.rejectedExactWorld
                + " repositoryUnavailable=" + result.repositoryUnavailable;
        }
    }

    private static final class DeliveryCapture {
        private DeliveryOutcome outcome = DeliveryOutcome.CONFLICT;
    }

    private static final class PresenceCapture {
        private boolean leftOrOffline;
        private boolean offline;
        private EnumSet<TraversalIdentity.ItemType> presentTypes =
            EnumSet.noneOf(TraversalIdentity.ItemType.class);
        private TraversalLoadout loadout;
    }
}
