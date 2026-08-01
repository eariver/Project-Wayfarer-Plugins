package io.github.eariver.wayfarer.frontier.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class ManagedPermanentIdentityTest {
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-0000000000d1");
    private static final UUID INSTANCE =
        UUID.fromString("00000000-0000-0000-0000-0000000000d2");

    @Test
    void exactCurrentRequiresOwnerThemeTypeInstanceEpochSchemaAndActive() {
        TraversalLoadout.LogicalItem logical = new TraversalLoadout.LogicalItem(
            TraversalIdentity.ItemType.ELYTRA,
            INSTANCE,
            3,
            TraversalLoadout.LogicalItem.State.ACTIVE
        );
        ManagedPermanentIdentity.Parsed current =
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.ELYTRA,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                3,
                1
            );
        assertTrue(ManagedPermanentIdentity.isExactCurrent(
            current,
            PLAYER,
            logical,
            true
        ));
        assertFalse(ManagedPermanentIdentity.isExactCurrent(
            current,
            PLAYER,
            logical,
            false
        ));
        assertFalse(ManagedPermanentIdentity.isExactCurrent(
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.ELYTRA,
                UUID.randomUUID(),
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                3,
                1
            ),
            PLAYER,
            logical,
            true
        ));
        assertFalse(ManagedPermanentIdentity.isExactCurrent(
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.ELYTRA,
                PLAYER,
                "other_theme",
                INSTANCE,
                3,
                1
            ),
            PLAYER,
            logical,
            true
        ));
        assertFalse(ManagedPermanentIdentity.isExactCurrent(
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.NAVIGATION,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                3,
                1
            ),
            PLAYER,
            logical,
            true
        ));
        assertFalse(ManagedPermanentIdentity.isExactCurrent(
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.ELYTRA,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                UUID.randomUUID(),
                3,
                1
            ),
            PLAYER,
            logical,
            true
        ));
        assertFalse(ManagedPermanentIdentity.isExactCurrent(
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.ELYTRA,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                2,
                1
            ),
            PLAYER,
            logical,
            true
        ));
        assertFalse(ManagedPermanentIdentity.isExactCurrent(
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.ELYTRA,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                3,
                2
            ),
            PLAYER,
            logical,
            true
        ));
    }

    @Test
    void wrongSchemaAndPartialAreNotCompleteManaged() {
        assertFalse(ManagedPermanentIdentity.isCompleteManaged(
            TraversalIdentity.ItemType.ELYTRA,
            PLAYER,
            TraversalIdentity.WORLDS_BEYOND,
            INSTANCE,
            1,
            0
        ));
        assertFalse(ManagedPermanentIdentity.isCompleteManaged(
            null,
            PLAYER,
            TraversalIdentity.WORLDS_BEYOND,
            INSTANCE,
            1,
            1
        ));
        assertFalse(ManagedPermanentIdentity.isCompleteManaged(
            TraversalIdentity.ItemType.ELYTRA,
            PLAYER,
            TraversalIdentity.WORLDS_BEYOND,
            null,
            1,
            1
        ));
    }

    @Test
    void staleEpochIsNonCurrentCleanupTarget() {
        TraversalLoadout.LogicalItem logical = new TraversalLoadout.LogicalItem(
            TraversalIdentity.ItemType.GRAPPLING_HOOK,
            INSTANCE,
            5,
            TraversalLoadout.LogicalItem.State.ACTIVE
        );
        ManagedPermanentIdentity.Parsed stale =
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.GRAPPLING_HOOK,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                4,
                1
            );
        assertTrue(ManagedPermanentIdentity.isNonCurrentManaged(
            stale,
            PLAYER,
            List.of(logical),
            true
        ));
        ManagedPermanentIdentity.Parsed current =
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.GRAPPLING_HOOK,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                5,
                1
            );
        assertFalse(ManagedPermanentIdentity.isNonCurrentManaged(
            current,
            PLAYER,
            List.of(logical),
            true
        ));
    }

    @Test
    void cursorLocationIsRepresentedByExactCurrentRuleIndependentlyOfSlot() {
        // Presence/cleanup call the same pure predicate for storage, armor,
        // offhand, and cursor; this test locks the predicate, not Bukkit slots.
        TraversalLoadout.LogicalItem logical = new TraversalLoadout.LogicalItem(
            TraversalIdentity.ItemType.NAVIGATION,
            INSTANCE,
            1,
            TraversalLoadout.LogicalItem.State.ACTIVE
        );
        ManagedPermanentIdentity.Parsed onCursor =
            ManagedPermanentIdentity.completeOrNull(
                TraversalIdentity.ItemType.NAVIGATION,
                PLAYER,
                TraversalIdentity.WORLDS_BEYOND,
                INSTANCE,
                1,
                1
            );
        assertTrue(ManagedPermanentIdentity.isExactCurrent(
            onCursor,
            PLAYER,
            logical,
            true
        ));
    }
}
