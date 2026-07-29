package io.github.eariver.wayfarer.main.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class GrowthToolClaimTest {
    private static final UUID TOOL = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000032");

    @Test
    void validatesPdcAuthorityWithoutNameLoreOrMaterial() {
        GrowthToolClaim claim = GrowthToolClaim.parse(validClaim()).claim().orElseThrow();
        assertEquals(GrowthToolClaim.Failure.NONE, claim.validate(OWNER, authority()));
    }

    @Test
    void failsClosedForOldEpochUnknownSchemaAndMalformedUuid() {
        Map<String, String> oldEpoch = new HashMap<>(validClaim());
        oldEpoch.put("wayfarer:instance_epoch", "2");
        assertEquals(
            GrowthToolClaim.Failure.EPOCH_MISMATCH,
            GrowthToolClaim.parse(oldEpoch).claim().orElseThrow().validate(OWNER, authority())
        );

        Map<String, String> schema = new HashMap<>(validClaim());
        schema.put("wayfarer:schema_version", "99");
        assertEquals(GrowthToolClaim.Failure.UNKNOWN_SCHEMA,
            GrowthToolClaim.parse(schema).failure());

        Map<String, String> malformed = new HashMap<>(validClaim());
        malformed.put("wayfarer:owner_uuid", "not-a-uuid");
        assertEquals(GrowthToolClaim.Failure.MALFORMED,
            GrowthToolClaim.parse(malformed).failure());
        assertTrue(GrowthToolClaim.parse(malformed).claim().isEmpty());
    }

    private static Map<String, String> validClaim() {
        return Map.of(
            "wayfarer:item_type", "GROWTH_TOOL",
            "wayfarer:item_instance_id", "00000000-0000-0000-0000-000000000033",
            "wayfarer:tool_id", TOOL.toString(),
            "wayfarer:owner_uuid", OWNER.toString(),
            "wayfarer:tool_type", "PICKAXE",
            "wayfarer:instance_epoch", "1",
            "wayfarer:schema_version", "1",
            "wayfarer:display_revision", "1"
        );
    }

    private static GrowthTool authority() {
        return new GrowthTool(
            TOOL,
            OWNER,
            1,
            0,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.ACTIVE,
            GrowthTool.DeliveryStatus.DELIVERED,
            0,
            1,
            1,
            0,
            Instant.EPOCH
        );
    }
}
