package io.github.eariver.wayfarer.frontier.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TraversalIdentityAuthorityTest {
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID ITEM =
        UUID.fromString("00000000-0000-0000-0000-000000000202");

    @Test
    void pendingPermanentIdentityRetainsUuidAuthorityAndEpoch() {
        TraversalIdentity identity = new TraversalIdentity(
            ITEM,
            TraversalIdentity.ItemType.ELYTRA,
            PLAYER,
            TraversalIdentity.WORLDS_BEYOND,
            4,
            1
        );
        PendingDelivery delivery = new PendingDelivery(
            UUID.fromString("00000000-0000-0000-0000-000000000203"),
            PLAYER,
            TraversalIdentity.WORLDS_BEYOND,
            PendingDelivery.ItemType.ELYTRA,
            1,
            "frontier-permanent:test:4",
            PendingDelivery.State.PENDING,
            0,
            Instant.parse("2026-07-30T00:00:00Z"),
            identity
        );

        assertEquals(identity, delivery.identity());
        assertEquals(
            TraversalIdentity.Validation.EPOCH_MISMATCH,
            identity.validate(
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                5
            )
        );
    }

    @Test
    void mismatchedPendingIdentityFailsClosed() {
        TraversalIdentity identity = new TraversalIdentity(
            ITEM,
            TraversalIdentity.ItemType.NAVIGATION,
            PLAYER,
            TraversalIdentity.WORLDS_BEYOND,
            1,
            1
        );
        assertThrows(IllegalArgumentException.class, () ->
            new PendingDelivery(
                UUID.randomUUID(),
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                PendingDelivery.ItemType.ELYTRA,
                1,
                "frontier-permanent:test:1",
                PendingDelivery.State.PENDING,
                0,
                Instant.parse("2026-07-30T00:00:00Z"),
                identity
            )
        );
    }
}
