≠rá^—f•ñÿ¶{Oly 'v√Æ∂õ≠package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.identity.GrowthToolPhysicalClaim;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class HeldGrowthToolAuthorizerTest {
    private static final UUID OWNER =
        UUID.fromString("00000000-0000-0000-0000-00000000a401");
    private static final UUID OTHER =
        UUID.fromString("00000000-0000-0000-0000-00000000a402");
    private static final UUID TOOL =
        UUID.fromString("00000000-0000-0000-0000-00000000a403");
    private static final UUID INSTANCE =
        UUID.fromString("00000000-0000-0000-0000-00000000a404");
    private static final Instant NOW = Instant.parse("2026-08-03T00:00:00Z");

    @Test
    void mapsAuthorityUnavailableAndMalformedClaimsFailClosed() {
        GrowthToolPhysicalClaim.ParseResult valid =
            GrowthToolPhysicalClaim.parse(raw("GROWTH_TOOL"));

        assertEquals(
            HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE,
            HeldGrowthToolAuthorizer.authorize(true, valid, OWNER, null).state()
        );
        assertEquals(
            HeldGrowthToolAuthorization.State.MALFORMED,
            HeldGrowthToolAuthorizer.authorize(
                true,
                GrowthToolPhysicalClaim.parse(Map.of()),
                OWNER,
                authority(GrowthTool.Status.ACTIVE)
            ).state()
        );
    }

    @Test
    void mapsValidActiveAndBrokenOwnerClaims() {
        assertEquals(
            HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER,
            authorize("GROWTH_TOOL", GrowthTool.Status.ACTIVE, OWNER, OWNER)
        );
        assertEquals(
            HeldGrowthToolAuthorization.State.VALID_BROKEN_OWNER,
            authorize("BROKEN_GROWTH_TOOL", GrowthTool.Status.BROKEN, OWNER, OWNER)
        );
    }

    @Test
    void mapsWrongOwnerStaleIdentityEpochSchemaRevocationAndWrongState() {
        assertEquals(
            HeldGrowthToolAuthorization.State.WRONG_OWNER,
            authorize("GROWTH_TOOL", GrowthTool.Status.ACTIVE, OWNER, OTHER)
        );

        Map<String, String> staleTool = raw("GROWTH_TOOL");
        staleTool.put("tool_id", UUID.randomUUID().toString());
        assertEquals(
            HeldGrowthToolAuthorization.State.STALE_INSTANCE,
            state(staleTool, GrowthTool.Status.ACTIVE, OWNER)
        );

        Map<String, String> staleInstance = raw("GROWTH_TOOL");
        staleInstance.put("item_instance_id", UUID.randomUUID().toString());
        assertEquals(
            HeldGrowthToolAuthorization.State.STALE_INSTANCE,
            state(staleInstance, GrowthTool.Status.ACTIVE, OWNER)
        );

        Map<String, String> staleEpoch = raw("GROWTH_TOOL");
        staleEpoch.put("instance_epoch", "2");
        assertEquals(
            HeldGrowthToolAuthorization.State.STALE_EPOCH,
            state(staleEpoch, GrowthTool.Status.ACTIVE, OWNER)
        );

        Map<String, String> unknownSchema = raw("GROWTH_TOOL");
        unknownSchema.put("schema_version", "99");
        assertEquals(
            HeldGrowthToolAuthorization.State.UNKNOWN_SCHEMA,
            HeldGrowthToolAuthorizer.authorize(
                true,
                GrowthToolPhysicalClaim.parse(unknownSchema),
                OWNER,
                authority(GrowthTool.Status.ACTIVE)
            ).state()
        );

        assertEquals(
            HeldGrowthToolAuthorization.State.REVOKED,
            authorize("GROWTH_TOOL", GrowthTool.Status.REVOKED, OWNER, OWNER)
        );
        assertEquals(
            HeldGrowthToolAuthorization.State.WRONG_ITEM_STATE,
            authorize("BROKEN_GROWTH_TOOL", GrowthTool.Status.ACTIVE, OWNER, OWNER)
        );
    }

    @Test
    void nonManagedClassificationDoesNotInspectPresentationOrSlot() {
        assertEquals(
            HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM,
            HeldGrowthToolAuthorizer.authorize(
                false,
                GrowthToolPhysicalClaim.parse(Map.of()),
                OWNER,
                null
            ).state()
        );
    }

    private static HeldGrowthToolAuthorization.State authorize(
        String itemType,
        GrowthTool.Status status,
        UUID actor,
        UUID claimOwner
    ) {
        Map<String, String> raw = raw(itemType);
        raw.put("owner_uuid", claimOwner.toString());
        return state(raw, status, actor);
    }

    private static HeldGrowthToolAuthorization.State state(
        Map<String, String> raw,
        GrowthTool.Status status,
        UUID actor
    ) {
        return HeldGrowthToolAuthorizer.authorize(
            true,
            GrowthToolPhysicalClaim.parse(raw),
            actor,
            authority(status)
        ).state();
    }

    private static Map<String, String> raw(String itemType) {
        Map<String, String> raw = new HashMap<>();
        raw.put("item_type", itemType);
        raw.put("item_instance_id", INSTANCE.toString());
        raw.put("tool_id", TOOL.toString());
        raw.put("owner_uuid", OWNER.toString());
        raw.put("tool_type", GrowthTool.TOOL_TYPE);
        raw.put("instance_epoch", "1");
        raw.put("schema_version", "1");
        raw.put("display_revision", "1");
        return raw;
    }

    private static GrowthTool authority(GrowthTool.Status status) {
        return new GrowthTool(
            TOOL,
            INSTANCE,
            OWNER,
            1,
            0,
            GrowthTool.Branch.FORTUNE,
            status,
            GrowthTool.DeliveryStatus.DELIVERED,
            status == GrowthTool.Status.BROKEN ? 100 : 0,
            1,
            1,
            0,
            NOW
        );
    }
}
