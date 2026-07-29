package io.github.eariver.wayfarer.frontier.identity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record LaunchpadItemClaim(
    UUID itemInstanceId,
    String definitionId,
    int schemaVersion
) {
    private static final Set<Integer> SUPPORTED_SCHEMAS = Set.of(1);

    public static ParseResult parse(Map<String, String> raw) {
        try {
            if (!"LAUNCHPAD".equals(required(raw, "item_type"))) {
                return ParseResult.invalid(Failure.UNKNOWN_ITEM_TYPE);
            }
            UUID itemInstanceId = UUID.fromString(required(raw, "item_instance_id"));
            String definition = required(raw, "definition_id");
            int schema = Integer.parseInt(required(raw, "schema_version"));
            if (!definition.matches("[A-Za-z0-9:_-]{1,64}")) {
                return ParseResult.invalid(Failure.INVALID_DEFINITION);
            }
            if (!SUPPORTED_SCHEMAS.contains(schema)) {
                return ParseResult.invalid(Failure.UNKNOWN_SCHEMA);
            }
            return ParseResult.valid(new LaunchpadItemClaim(itemInstanceId, definition, schema));
        } catch (IllegalArgumentException | NullPointerException failure) {
            return ParseResult.invalid(Failure.MALFORMED);
        }
    }

    private static String required(Map<String, String> raw, String key) {
        String value = raw.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing launchpad identity");
        }
        return value;
    }

    public record ParseResult(Optional<LaunchpadItemClaim> claim, Failure failure) {
        private static ParseResult valid(LaunchpadItemClaim claim) {
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
        INVALID_DEFINITION,
        UNKNOWN_SCHEMA
    }
}
