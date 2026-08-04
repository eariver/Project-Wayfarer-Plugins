package io.github.eariver.wayfarer.main.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class GrowthToolPhysicalClaimTest {
    private final UUID toolId = UUID.randomUUID();
    private final UUID instanceId = UUID.randomUUID();
    private final UUID owner = UUID.randomUUID();

    @Test
    void completeIdentityValidatesAgainstLogicalAndPhysicalAuthority() {
        var parsed = GrowthToolPhysicalClaim.parse(raw());
        assertTrue(parsed.claim().isPresent());
        assertEquals(
            GrowthToolPhysicalClaim.Validation.VALID,
            parsed.claim().orElseThrow().validate(owner, authority())
        );
    }

    @Test
    void rejectsMissingWrongAndStaleAuthorityFields() {
        Map<String, String> missingInstance = new HashMap<>(raw());
        missingInstance.remove("item_instance_id");
        assertEquals(
            GrowthToolPhysicalClaim.Failure.MALFORMED,
            GrowthToolPhysicalClaim.parse(missingInstance).failure()
        );

        Map<String, String> wrongType = new HashMap<>(raw());
        wrongType.put("tool_type", "AXE");
        assertEquals(
            GrowthToolPhysicalClaim.Failure.UNKNOWN_TOOL_TYPE,
            GrowthToolPhysicalClaim.parse(wrongType).failure()
        );

        Map<String, String> unknownSchema = new HashMap<>(raw());
        unknownSchema.put("schema_version", "2");
        assertEquals(
            GrowthToolPhysicalClaim.Failure.UNKNOWN_SCHEMA,
            GrowthToolPhysicalClaim.parse(unknownSchema).failure()
        );

        Map<String, String> oldInstance = new HashMap<>(raw());
        oldInstance.put("item_instance_id", UUID.randomUUID().toString());
        assertEquals(
            GrowthToolPhysicalClaim.Validation.STALE_INSTANCE,
            GrowthToolPhysicalClaim.parse(oldInstance).claim().orElseThrow()
                .validate(owner, authority())
        );

        Map<String, String> oldEpoch = new HashMap<>(raw());
        oldEpoch.put("instance_epoch", "1");
        assertEquals(
            GrowthToolPhysicalClaim.Validation.STALE_EPOCH,
            GrowthToolPhysicalClaim.parse(oldEpoch).claim().orElseThrow()
                .validate(owner, authority())
        );

        assertEquals(
            GrowthToolPhysicalClaim.Validation.WRONG_OWNER,
            GrowthToolPhysicalClaim.parse(raw()).claim().orElseThrow()
                .validate(UUID.randomUUID(), authority())
        );
    }

    @Test
    void reissueChangesBothEpochAndPhysicalInstance() {
        GrowthTool reissued = authority().reissued(Instant.EPOCH.plusSeconds(1));
        assertEquals(3, reissued.instanceEpoch());
        assertTrue(!instanceId.equals(reissued.itemInstanceId()));
        assertEquals(
            GrowthToolPhysicalClaim.Validation.STALE_INSTANCE,
            GrowthToolPhysicalClaim.parse(raw()).claim().orElseThrow()
                .validate(owner, reissued)
        );
    }

    private Map<String, String> raw() {
        return Map.of(
            "item_type", "GROWTH_TOOL",
            "item_instance_id", instanceId.toString(),
            "tool_id", toolId.toString(),
            "owner_uuid", owner.toString(),
            "tool_type", "PICKAXE",
            "instance_epoch", "2",
            "schema_version", "1",
            "display_revision", "3"
        );
    }

    private GrowthTool authority() {
        return new GrowthTool(
            toolId,
            instanceId,
            owner,
            2,
            0,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.ACTIVE,
            GrowthTool.DeliveryStatus.DELIVERED,
            0,
            1,
            3,
            0,
            Instant.EPOCH
        );
    }
}
