package io.github.eariver.wayfarer.main.identity;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record GrowthToolClaim(
    String itemType,
    UUID itemInstanceId,
    UUID toolId,
    UUID ownerUuid,
    String toolType,
    long instanceEpoch,
    int schemaVersion,
    long displayRevision
) {
    private static final Set<Integer> SUPPORTED_SCHEMAS = Set.of(1);

    public static ParseResult parse(Map<String, String> raw) {
        try {
            String itemType = required(raw, "wayfarer:item_type");
            UUID itemInstanceId = UUID.fromString(required(raw, "wayfarer:item_instance_id"));
            UUID toolId = UUID.fromString(required(raw, "wayfarer:tool_id"));
            UUID ownerUuid = UUID.fromString(required(raw, "wayfarer:owner_uuid"));
            String toolType = required(raw, "wayfarer:tool_type");
            long epoch = Long.parseLong(required(raw, "wayfarer:instance_epoch"));
            int schema = Integer.parseInt(required(raw, "wayfarer:schema_version"));
            long revision = Long.parseLong(required(raw, "wayfarer:display_revision"));
            if (!"GROWTH_TOOL".equals(itemType) && !"BROKEN_GROWTH_TOOL".equals(itemType)) {
                return ParseResult.invalid(Failure.UNKNOWN_ITEM_TYPE);
            }
            if (!GrowthTool.TOOL_TYPE.equals(toolType)) {
                return ParseResult.invalid(Failure.UNKNOWN_TOOL_TYPE);
            }
            if (!SUPPORTED_SCHEMAS.contains(schema)) {
                return ParseResult.invalid(Failure.UNKNOWN_SCHEMA);
            }
            if (epoch < 1 || revision < 1) {
                return ParseResult.invalid(Failure.INVALID_NUMERIC_VALUE);
            }
            return ParseResult.valid(new GrowthToolClaim(
                itemType, itemInstanceId, toolId, ownerUuid, toolType, epoch, schema, revision
            ));
        } catch (IllegalArgumentException | NullPointerException failure) {
            return ParseResult.invalid(Failure.MALFORMED);
        }
    }

    public Failure validate(UUID actorUuid, GrowthTool authority) {
        if (!ownerUuid.equals(actorUuid) || !authority.ownerUuid().equals(actorUuid)) {
            return Failure.OWNER_MISMATCH;
        }
        if (!toolId.equals(authority.toolId())) {
            return Failure.TOOL_MISMATCH;
        }
        if (instanceEpoch != authority.instanceEpoch()) {
            return Failure.EPOCH_MISMATCH;
        }
        if (schemaVersion != authority.schemaVersion()) {
            return Failure.SCHEMA_MISMATCH;
        }
        if (authority.status() == GrowthTool.Status.REVOKED) {
            return Failure.REVOKED;
        }
        boolean brokenClaim = "BROKEN_GROWTH_TOOL".equals(itemType);
        if (brokenClaim != (authority.status() == GrowthTool.Status.BROKEN)) {
            return Failure.STATUS_MISMATCH;
        }
        return Failure.NONE;
    }

    private static String required(Map<String, String> raw, String key) {
        String value = raw.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing identity field");
        }
        return value;
    }

    public record ParseResult(Optional<GrowthToolClaim> claim, Failure failure) {
        private static ParseResult valid(GrowthToolClaim claim) {
            return new ParseResult(Optional.of(claim), Failure.NONE);
        }

        private static ParseResult invalid(Failure failure) {
            return new ParseResult(Optional.empty(), failure);
        }
    }

    public enum Failure {
        NONE,
        MALFORMED,
        UNKNOWN_ITEM_TYPE,
        UNKNOWN_TOOL_TYPE,
        UNKNOWN_SCHEMA,
        INVALID_NUMERIC_VALUE,
        OWNER_MISMATCH,
        TOOL_MISMATCH,
        EPOCH_MISMATCH,
        SCHEMA_MISMATCH,
        STATUS_MISMATCH,
        REVOKED
    }
}
