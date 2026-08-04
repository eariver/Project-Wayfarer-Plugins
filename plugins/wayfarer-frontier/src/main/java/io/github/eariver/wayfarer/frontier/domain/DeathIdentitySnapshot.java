package io.github.eariver.wayfarer.frontier.domain;

import java.util.Objects;
import java.util.UUID;

public record DeathIdentitySnapshot(
    UUID playerUuid,
    TraversalIdentity.ItemType itemType,
    UUID itemInstanceId,
    long instanceEpoch,
    int schemaVersion,
    String themeId
) {
    public DeathIdentitySnapshot {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(itemType, "itemType");
        Objects.requireNonNull(itemInstanceId, "itemInstanceId");
        Objects.requireNonNull(themeId, "themeId");
        if (themeId.isBlank() || instanceEpoch < 1 || schemaVersion < 1) {
            throw new IllegalArgumentException("Death identity snapshot is invalid");
        }
    }
}
