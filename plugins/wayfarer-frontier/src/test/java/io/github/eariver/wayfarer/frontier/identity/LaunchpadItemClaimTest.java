package io.github.eariver.wayfarer.frontier.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class LaunchpadItemClaimTest {
    @Test
    void acceptsCanonicalIdentityWithoutRemainingUses() {
        LaunchpadItemClaim.ParseResult result = LaunchpadItemClaim.parse(valid());
        assertTrue(result.claim().isPresent());
        assertEquals("launchpad-v1", result.claim().orElseThrow().definitionId());
    }

    @Test
    void rejectsUnknownSchemaAndType() {
        Map<String, String> schema = new HashMap<>(valid());
        schema.put("schema_version", "2");
        assertEquals(LaunchpadItemClaim.Failure.UNKNOWN_SCHEMA,
            LaunchpadItemClaim.parse(schema).failure());

        Map<String, String> type = new HashMap<>(valid());
        type.put("item_type", "WAYSTONE_PLACEMENT_TOOL");
        assertEquals(LaunchpadItemClaim.Failure.UNKNOWN_ITEM_TYPE,
            LaunchpadItemClaim.parse(type).failure());
    }

    private static Map<String, String> valid() {
        return Map.of(
            "item_type", "LAUNCHPAD",
            "item_instance_id", "00000000-0000-0000-0000-000000000060",
            "definition_id", "launchpad-v1",
            "schema_version", "1"
        );
    }
}
