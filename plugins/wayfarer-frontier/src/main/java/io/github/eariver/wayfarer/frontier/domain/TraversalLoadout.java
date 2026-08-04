package io.github.eariver.wayfarer.frontier.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record TraversalLoadout(
    UUID playerUuid,
    String themeId,
    Instant firstJoinedAt,
    boolean initialLaunchpadsGranted,
    List<LogicalItem> permanentItems,
    long lockVersion
) {
    public TraversalLoadout {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(themeId, "themeId");
        permanentItems = List.copyOf(permanentItems);
        if (!TraversalIdentity.WORLDS_BEYOND.equals(themeId) || lockVersion < 0) {
            throw new IllegalArgumentException("Traversal loadout is invalid");
        }
    }

    public List<PendingDelivery.ItemType> missingPermanentItems() {
        return java.util.Arrays.stream(TraversalIdentity.ItemType.values())
            .filter(type -> permanentItems.stream().noneMatch(item ->
                item.itemType() == type && item.state() == LogicalItem.State.ACTIVE
            ))
            .map(TraversalLoadout::deliveryType)
            .toList();
    }

    private static PendingDelivery.ItemType deliveryType(TraversalIdentity.ItemType type) {
        return switch (type) {
            case ELYTRA -> PendingDelivery.ItemType.ELYTRA;
            case GRAPPLING_HOOK -> PendingDelivery.ItemType.GRAPPLING_HOOK;
            case NAVIGATION -> PendingDelivery.ItemType.NAVIGATION;
        };
    }

    public record LogicalItem(
        TraversalIdentity.ItemType itemType,
        UUID itemInstanceId,
        long instanceEpoch,
        State state
    ) {
        public LogicalItem {
            Objects.requireNonNull(itemType, "itemType");
            Objects.requireNonNull(itemInstanceId, "itemInstanceId");
            Objects.requireNonNull(state, "state");
            if (instanceEpoch < 1) {
                throw new IllegalArgumentException("Item epoch must be positive");
            }
        }

        public enum State {
            ACTIVE,
            REVOKED
        }
    }
}
