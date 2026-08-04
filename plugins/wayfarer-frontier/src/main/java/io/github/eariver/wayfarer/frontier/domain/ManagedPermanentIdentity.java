package io.github.eariver.wayfarer.frontier.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Pure identity rules for managed permanent items. Callers supply already-parsed
 * PDC values; this type does not touch Bukkit APIs.
 */
public final class ManagedPermanentIdentity {
    private ManagedPermanentIdentity() {}

    public record Parsed(
        TraversalIdentity.ItemType itemType,
        UUID ownerUuid,
        String themeId,
        UUID itemInstanceId,
        long instanceEpoch,
        int schemaVersion
    ) {
        public Parsed {
            Objects.requireNonNull(itemType, "itemType");
            Objects.requireNonNull(ownerUuid, "ownerUuid");
            Objects.requireNonNull(themeId, "themeId");
            Objects.requireNonNull(itemInstanceId, "itemInstanceId");
        }
    }

    public static boolean isCompleteManaged(
        TraversalIdentity.ItemType itemType,
        UUID ownerUuid,
        String themeId,
        UUID itemInstanceId,
        long instanceEpoch,
        int schemaVersion
    ) {
        return itemType != null
            && ownerUuid != null
            && itemInstanceId != null
            && TraversalIdentity.WORLDS_BEYOND.equals(themeId)
            && instanceEpoch >= 1
            && schemaVersion >= 1;
    }

    public static Parsed completeOrNull(
        TraversalIdentity.ItemType itemType,
        UUID ownerUuid,
        String themeId,
        UUID itemInstanceId,
        long instanceEpoch,
        int schemaVersion
    ) {
        if (!isCompleteManaged(
            itemType,
            ownerUuid,
            themeId,
            itemInstanceId,
            instanceEpoch,
            schemaVersion
        )) {
            return null;
        }
        return new Parsed(
            itemType,
            ownerUuid,
            themeId,
            itemInstanceId,
            instanceEpoch,
            schemaVersion
        );
    }

    public static boolean isExactCurrent(
        Parsed item,
        UUID playerUuid,
        TraversalLoadout.LogicalItem logical,
        boolean inExactWorld
    ) {
        if (item == null || playerUuid == null || logical == null || !inExactWorld) {
            return false;
        }
        return logical.state() == TraversalLoadout.LogicalItem.State.ACTIVE
            && item.itemType() == logical.itemType()
            && item.ownerUuid().equals(playerUuid)
            && TraversalIdentity.WORLDS_BEYOND.equals(item.themeId())
            && item.itemInstanceId().equals(logical.itemInstanceId())
            && item.instanceEpoch() == logical.instanceEpoch()
            && item.schemaVersion() == 1;
    }

    public static boolean isNonCurrentManaged(
        Parsed item,
        UUID playerUuid,
        Iterable<TraversalLoadout.LogicalItem> logicalItems,
        boolean inExactWorld
    ) {
        if (item == null) {
            return false;
        }
        for (TraversalLoadout.LogicalItem logical : logicalItems) {
            if (isExactCurrent(item, playerUuid, logical, inExactWorld)) {
                return false;
            }
        }
        return true;
    }
}
