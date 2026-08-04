package io.github.eariver.wayfarer.frontier.domain;

import java.util.Objects;
import java.util.UUID;

public record TraversalIdentity(
    UUID itemInstanceId,
    ItemType itemType,
    UUID ownerUuid,
    String themeId,
    long instanceEpoch,
    int schemaVersion
) {
    public static final String WORLDS_BEYOND = "worlds_beyond";

    public TraversalIdentity {
        Objects.requireNonNull(itemInstanceId, "itemInstanceId");
        Objects.requireNonNull(itemType, "itemType");
        Objects.requireNonNull(ownerUuid, "ownerUuid");
        Objects.requireNonNull(themeId, "themeId");
        if (themeId.isBlank() || instanceEpoch < 1 || schemaVersion < 1) {
            throw new IllegalArgumentException("Traversal identity is invalid");
        }
    }

    public Validation validate(UUID actorUuid, String currentTheme, long currentEpoch) {
        if (!ownerUuid.equals(actorUuid)) {
            return Validation.OWNER_MISMATCH;
        }
        if (!themeId.equals(currentTheme)) {
            return Validation.THEME_MISMATCH;
        }
        if (instanceEpoch != currentEpoch) {
            return Validation.EPOCH_MISMATCH;
        }
        return Validation.VALID;
    }

    public enum ItemType {
        ELYTRA,
        GRAPPLING_HOOK,
        NAVIGATION
    }

    public enum Validation {
        VALID,
        OWNER_MISMATCH,
        THEME_MISMATCH,
        EPOCH_MISMATCH
    }
}
