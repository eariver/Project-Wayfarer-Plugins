package io.github.eariver.wayfarer.frontier.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PendingDelivery(
    UUID deliveryId,
    UUID playerUuid,
    String themeId,
    ItemType itemType,
    int quantity,
    String idempotencyKey,
    State state,
    int attempts,
    Instant createdAt,
    TraversalIdentity identity
) {
    public PendingDelivery(
        UUID deliveryId,
        UUID playerUuid,
        String themeId,
        ItemType itemType,
        int quantity,
        String idempotencyKey,
        State state,
        int attempts,
        Instant createdAt
    ) {
        this(
            deliveryId,
            playerUuid,
            themeId,
            itemType,
            quantity,
            idempotencyKey,
            state,
            attempts,
            createdAt,
            null
        );
    }

    public PendingDelivery {
        Objects.requireNonNull(deliveryId, "deliveryId");
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(themeId, "themeId");
        Objects.requireNonNull(itemType, "itemType");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(createdAt, "createdAt");
        if (themeId.isBlank() || quantity <= 0 || idempotencyKey.isBlank() || attempts < 0) {
            throw new IllegalArgumentException("Pending delivery is invalid");
        }
        if (identity != null
            && (!identity.ownerUuid().equals(playerUuid)
                || !identity.themeId().equals(themeId)
                || !matches(itemType, identity.itemType()))) {
            throw new IllegalArgumentException(
                "Pending delivery identity does not match"
            );
        }
    }

    public PendingDelivery attempt(boolean delivered) {
        if (state == State.DELIVERED || state == State.CANCELLED) {
            return this;
        }
        return new PendingDelivery(
            deliveryId,
            playerUuid,
            themeId,
            itemType,
            quantity,
            idempotencyKey,
            delivered ? State.DELIVERED : State.PENDING,
            Math.addExact(attempts, 1),
            createdAt,
            identity
        );
    }

    private static boolean matches(
        ItemType deliveryType,
        TraversalIdentity.ItemType identityType
    ) {
        return switch (identityType) {
            case ELYTRA -> deliveryType == ItemType.ELYTRA;
            case GRAPPLING_HOOK -> deliveryType == ItemType.GRAPPLING_HOOK;
            case NAVIGATION -> deliveryType == ItemType.NAVIGATION;
        };
    }

    public enum ItemType {
        ELYTRA,
        GRAPPLING_HOOK,
        NAVIGATION,
        LAUNCHPAD,
        FIREWORK_ROCKET
    }

    public enum State {
        PENDING,
        DELIVERED,
        CANCELLED
    }
}
