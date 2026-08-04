package io.github.eariver.wayfarer.main.identity;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record GrowthToolPhysicalClaim(
    ItemType itemType,
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
            ItemType itemType = ItemType.valueOf(required(raw, "item_type"));
            UUID itemInstanceId = UUID.fromString(
                required(raw, "item_instance_id")
            );
            UUID toolId = UUID.fromString(required(raw, "tool_id"));
            UUID ownerUuid = UUID.fromString(required(raw, "owner_uuid"));
            String toolType = required(raw, "tool_type");
            long epoch = Long.parseLong(required(raw, "instance_epoch"));
            int schema = Integer.parseInt(required(raw, "schema_version"));
            long displayRevision = Long.parseLong(
                required(raw, "display_revision")
            );
            if (!GrowthTool.TOOL_TYPE.equals(toolType)) {
                return ParseResult.invalid(Failure.UNKNOWN_TOOL_TYPE);
            }
            if (epoch < 1 || displayRevision < 1) {
                return ParseResult.invalid(Failure.MALFORMED);
            }
            if (!SUPPORTED_SCHEMAS.contains(schema)) {
                return ParseResult.invalid(Failure.UNKNOWN_SCHEMA);
            }
            return ParseResult.valid(new GrowthToolPhysicalClaim(
                itemType,
                itemInstanceId,
                toolId,
                ownerUuid,
                toolType,
                epoch,
                schema,
                displayRevision
            ));
        } catch (IllegalArgumentException | NullPointerException failure) {
            return ParseResult.invalid(Failure.MALFORMED);
        }
    }

    public Validation validate(UUID actorUuid, GrowthTool authority) {
        if (!ownerUuid.equals(actorUuid)
            || !ownerUuid.equals(authority.ownerUuid())) {
            return Validation.WRONG_OWNER;
        }
        if (!toolId.equals(authority.toolId())
            || !itemInstanceId.equals(authority.itemInstanceId())) {
            return Validation.STALE_INSTANCE;
        }
        if (instanceEpoch != authority.instanceEpoch()) {
            return Validation.STALE_EPOCH;
        }
        if (schemaVersion != authority.schemaVersion()) {
            return Validation.UNKNOWN_SCHEMA;
        }
        if (authority.status() == GrowthTool.Status.REVOKED) {
            return Validation.REVOKED;
        }
        boolean correctState = authority.status() == GrowthTool.Status.BROKEN
            ? itemType == ItemType.BROKEN_GROWTH_TOOL
            : itemType == ItemType.GROWTH_TOOL;
        return correctState ? Validation.VALID : Validation.WRONG_ITEM_STATE;
    }

    private static String required(Map<String, String> raw, String key) {
        String value = raw.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing growth tool identity");
        }
        return value;
    }

    public enum ItemType {
        GROWTH_TOOL,
        BROKEN_GROWTH_TOOL
    }

    public enum Failure {
        NONE,
        MALFORMED,
        UNKNOWN_TOOL_TYPE,
        UNKNOWN_SCHEMA
    }

    public enum Validation {
        VALID,
        WRONG_OWNER,
        STALE_INSTANCE,
        STALE_EPOCH,
        UNKNOWN_SCHEMA,
        REVOKED,
        WRONG_ITEM_STATE
    }

    public record ParseResult(
        Optional<GrowthToolPhysicalClaim> claim,
        Failure failure
    ) {
        private static ParseResult valid(GrowthToolPhysicalClaim claim) {
            return new ParseResult(Optional.of(claim), Failure.NONE);
        }

        private static ParseResult invalid(Failure failure) {
            return new ParseResult(Optional.empty(), failure);
        }
    }
}
