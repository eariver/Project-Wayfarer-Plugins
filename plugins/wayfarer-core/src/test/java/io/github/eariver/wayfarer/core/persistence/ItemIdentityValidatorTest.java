package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemIdentityValidatorTest {
    private static final UUID ITEM_ID = UUID.fromString(
        "11111111-1111-1111-1111-111111111111"
    );
    private static final UUID OWNER_ID = UUID.fromString(
        "22222222-2222-2222-2222-222222222222"
    );

    @Test
    void validatesCreateRequest() {
        ItemIdentityValidator.validateCreate(
            new WayfarerItemIdentity.CreateRequest("growth_pickaxe", OWNER_ID, 0, 1, 0)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ItemIdentityValidator.validateCreate(
                new WayfarerItemIdentity.CreateRequest("Growth Pickaxe", OWNER_ID, 0, 1, 0)
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ItemIdentityValidator.validateCreate(
                new WayfarerItemIdentity.CreateRequest("valid", OWNER_ID, -1, 1, 0)
            )
        );
    }

    @Test
    void followsRawValidationOrder() {
        assertReason(raw(null, "type", OWNER_ID.toString(), 1L, 1, 0),
            WayfarerItemIdentity.FailureReason.MISSING_FIELD);
        assertReason(raw("invalid", "type", OWNER_ID.toString(), 1L, 1, 0),
            WayfarerItemIdentity.FailureReason.INVALID_ITEM_INSTANCE_ID);
        assertReason(raw(ITEM_ID.toString(), "type", "invalid", 1L, 1, 0),
            WayfarerItemIdentity.FailureReason.INVALID_OWNER_UUID);
        assertReason(raw(ITEM_ID.toString(), "type", OWNER_ID.toString(), -1L, 1, 0),
            WayfarerItemIdentity.FailureReason.INVALID_INSTANCE_EPOCH);
        assertReason(raw(ITEM_ID.toString(), "type", OWNER_ID.toString(), 1L, 0, 0),
            WayfarerItemIdentity.FailureReason.INVALID_SCHEMA_VERSION);
        assertReason(raw(ITEM_ID.toString(), "type", OWNER_ID.toString(), 1L, 1, -1),
            WayfarerItemIdentity.FailureReason.INVALID_DISPLAY_REVISION);
        assertReason(raw(ITEM_ID.toString(), "unknown", OWNER_ID.toString(), 1L, 1, 0),
            WayfarerItemIdentity.FailureReason.UNKNOWN_ITEM_TYPE);
        assertReason(
            request(raw(ITEM_ID.toString(), "type", OWNER_ID.toString(), 1L, 2, 0)),
            WayfarerItemIdentity.FailureReason.UNKNOWN_SCHEMA_VERSION
        );
    }

    @Test
    void rejectsNonCanonicalUuidClaims() {
        assertReason(raw(
            "11111111-1111-1111-1111-11111111111",
            "type",
            OWNER_ID.toString(),
            1L,
            1,
            0
        ), WayfarerItemIdentity.FailureReason.INVALID_ITEM_INSTANCE_ID);
    }

    @Test
    void detectsPersistedMismatchesAndValidIdentity() {
        ItemIdentityValidator.ParsedClaim claim = ItemIdentityValidator.parse(request(raw(
            ITEM_ID.toString(), "type", OWNER_ID.toString(), 1L, 1, 0
        )));
        Instant now = Instant.parse("2026-07-27T00:00:00Z");
        WayfarerItemIdentity.Identity valid = new WayfarerItemIdentity.Identity(
            ITEM_ID, "type", OWNER_ID, 1, 1, 0, now, now, 0
        );
        assertNull(ItemIdentityValidator.mismatch(claim, valid));
        assertEquals(
            WayfarerItemIdentity.FailureReason.ITEM_TYPE_MISMATCH,
            ItemIdentityValidator.mismatch(claim, identity("other", OWNER_ID, 1, 1, 0))
        );
        assertEquals(
            WayfarerItemIdentity.FailureReason.OWNER_MISMATCH,
            ItemIdentityValidator.mismatch(
                claim,
                identity("type", UUID.randomUUID(), 1, 1, 0)
            )
        );
        assertEquals(
            WayfarerItemIdentity.FailureReason.EPOCH_MISMATCH,
            ItemIdentityValidator.mismatch(claim, identity("type", OWNER_ID, 2, 1, 0))
        );
        assertEquals(
            WayfarerItemIdentity.FailureReason.SCHEMA_MISMATCH,
            ItemIdentityValidator.mismatch(claim, identity("type", OWNER_ID, 1, 2, 0))
        );
        assertEquals(
            WayfarerItemIdentity.FailureReason.DISPLAY_REVISION_MISMATCH,
            ItemIdentityValidator.mismatch(claim, identity("type", OWNER_ID, 1, 1, 1))
        );
    }

    @Test
    void validationRequestDefensivelyCopiesSets() {
        Set<String> types = new HashSet<>(Set.of("type"));
        WayfarerItemIdentity.ValidationRequest request = new WayfarerItemIdentity.ValidationRequest(
            raw(ITEM_ID.toString(), "type", OWNER_ID.toString(), 1L, 1, 0),
            types,
            Set.of(1),
            OWNER_ID,
            OptionalLong.empty()
        );
        types.clear();
        assertEquals(Set.of("type"), request.allowedItemTypes());
        assertThrows(
            UnsupportedOperationException.class,
            () -> request.allowedItemTypes().add("other")
        );
    }

    private static void assertReason(
        WayfarerItemIdentity.RawClaim raw,
        WayfarerItemIdentity.FailureReason reason
    ) {
        assertReason(request(raw), reason);
    }

    private static void assertReason(
        WayfarerItemIdentity.ValidationRequest request,
        WayfarerItemIdentity.FailureReason reason
    ) {
        assertEquals(reason, ItemIdentityValidator.parse(request).failure());
    }

    private static WayfarerItemIdentity.ValidationRequest request(
        WayfarerItemIdentity.RawClaim raw
    ) {
        return new WayfarerItemIdentity.ValidationRequest(
            raw,
            Set.of("type"),
            Set.of(1),
            OWNER_ID,
            OptionalLong.empty()
        );
    }

    private static WayfarerItemIdentity.RawClaim raw(
        String itemId,
        String type,
        String ownerId,
        Long epoch,
        Integer schema,
        Integer display
    ) {
        return new WayfarerItemIdentity.RawClaim(
            itemId, type, ownerId, epoch, schema, display
        );
    }

    private static WayfarerItemIdentity.Identity identity(
        String type,
        UUID owner,
        long epoch,
        int schema,
        int display
    ) {
        Instant now = Instant.parse("2026-07-27T00:00:00Z");
        return new WayfarerItemIdentity.Identity(
            ITEM_ID, type, owner, epoch, schema, display, now, now, 0
        );
    }
}
