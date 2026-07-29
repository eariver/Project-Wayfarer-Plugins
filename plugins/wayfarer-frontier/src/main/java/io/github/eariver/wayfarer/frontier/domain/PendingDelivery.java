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
    Instant createdAt
) {
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
            createdAt
        );
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
