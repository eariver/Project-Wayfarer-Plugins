package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerItemIdentity;

import java.util.OptionalLong;
import java.util.UUID;
import java.util.regex.Pattern;

final class ItemIdentityValidator {
    private static final Pattern ITEM_TYPE = Pattern.compile("[a-z0-9_.-]{1,96}");

    private ItemIdentityValidator() {}

    static void validateCreate(WayfarerItemIdentity.CreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create request is required");
        }
        if (request.itemType() == null || !ITEM_TYPE.matcher(request.itemType()).matches()) {
            throw new IllegalArgumentException("Item type is invalid");
        }
        if (request.ownerUuid() == null) {
            throw new IllegalArgumentException("Owner UUID is required");
        }
        if (request.instanceEpoch() < 0) {
            throw new IllegalArgumentException("Instance epoch is invalid");
        }
        if (request.schemaVersion() <= 0) {
            throw new IllegalArgumentException("Schema version is invalid");
        }
        if (request.displayRevision() < 0) {
            throw new IllegalArgumentException("Display revision is invalid");
        }
    }

    static ParsedClaim parse(WayfarerItemIdentity.ValidationRequest request) {
        WayfarerItemIdentity.RawClaim claim = request.claim();
        if (claim.itemInstanceId() == null
            || claim.itemType() == null
            || claim.ownerUuid() == null
            || claim.instanceEpoch() == null
            || claim.schemaVersion() == null
            || claim.displayRevision() == null) {
            return ParsedClaim.invalid(WayfarerItemIdentity.FailureReason.MISSING_FIELD);
        }
        UUID itemId;
        try {
            itemId = canonicalUuid(claim.itemInstanceId());
        } catch (IllegalArgumentException failure) {
            return ParsedClaim.invalid(
                WayfarerItemIdentity.FailureReason.INVALID_ITEM_INSTANCE_ID
            );
        }
        UUID ownerId;
        try {
            ownerId = canonicalUuid(claim.ownerUuid());
        } catch (IllegalArgumentException failure) {
            return ParsedClaim.invalid(
                WayfarerItemIdentity.FailureReason.INVALID_OWNER_UUID,
                itemId,
                safeType(claim.itemType())
            );
        }
        if (claim.instanceEpoch() < 0) {
            return ParsedClaim.invalid(
                WayfarerItemIdentity.FailureReason.INVALID_INSTANCE_EPOCH,
                itemId,
                safeType(claim.itemType())
            );
        }
        if (claim.schemaVersion() <= 0) {
            return ParsedClaim.invalid(
                WayfarerItemIdentity.FailureReason.INVALID_SCHEMA_VERSION,
                itemId,
                safeType(claim.itemType())
            );
        }
        if (claim.displayRevision() < 0) {
            return ParsedClaim.invalid(
                WayfarerItemIdentity.FailureReason.INVALID_DISPLAY_REVISION,
                itemId,
                safeType(claim.itemType())
            );
        }
        if (!ITEM_TYPE.matcher(claim.itemType()).matches()
            || !request.allowedItemTypes().contains(claim.itemType())) {
            return ParsedClaim.invalid(
                WayfarerItemIdentity.FailureReason.UNKNOWN_ITEM_TYPE,
                itemId,
                safeType(claim.itemType())
            );
        }
        if (!request.supportedSchemaVersions().contains(claim.schemaVersion())) {
            return ParsedClaim.invalid(
                WayfarerItemIdentity.FailureReason.UNKNOWN_SCHEMA_VERSION,
                itemId,
                claim.itemType()
            );
        }
        return new ParsedClaim(
            null,
            itemId,
            claim.itemType(),
            ownerId,
            claim.instanceEpoch(),
            claim.schemaVersion(),
            claim.displayRevision(),
            request.expectedOwnerUuid(),
            request.expectedInstanceEpoch()
        );
    }

    static WayfarerItemIdentity.FailureReason mismatch(
        ParsedClaim claim,
        WayfarerItemIdentity.Identity identity
    ) {
        if (!identity.itemType().equals(claim.itemType())) {
            return WayfarerItemIdentity.FailureReason.ITEM_TYPE_MISMATCH;
        }
        if (!identity.ownerUuid().equals(claim.ownerUuid())
            || !identity.ownerUuid().equals(claim.expectedOwnerUuid())) {
            return WayfarerItemIdentity.FailureReason.OWNER_MISMATCH;
        }
        OptionalLong expectedEpoch = claim.expectedInstanceEpoch();
        if (identity.instanceEpoch() != claim.instanceEpoch()
            || (expectedEpoch.isPresent()
                && identity.instanceEpoch() != expectedEpoch.getAsLong())) {
            return WayfarerItemIdentity.FailureReason.EPOCH_MISMATCH;
        }
        if (identity.schemaVersion() != claim.schemaVersion()) {
            return WayfarerItemIdentity.FailureReason.SCHEMA_MISMATCH;
        }
        if (identity.displayRevision() != claim.displayRevision()) {
            return WayfarerItemIdentity.FailureReason.DISPLAY_REVISION_MISMATCH;
        }
        return null;
    }

    private static String safeType(String value) {
        return value != null && ITEM_TYPE.matcher(value).matches() ? value : null;
    }

    private static UUID canonicalUuid(String value) {
        UUID parsed = UUID.fromString(value);
        if (!parsed.toString().equals(value)) {
            throw new IllegalArgumentException("UUID is not canonical");
        }
        return parsed;
    }

    record ParsedClaim(
        WayfarerItemIdentity.FailureReason failure,
        UUID itemInstanceId,
        String itemType,
        UUID ownerUuid,
        Long instanceEpoch,
        Integer schemaVersion,
        Integer displayRevision,
        UUID expectedOwnerUuid,
        OptionalLong expectedInstanceEpoch
    ) {
        static ParsedClaim invalid(WayfarerItemIdentity.FailureReason reason) {
            return invalid(reason, null, null);
        }

        static ParsedClaim invalid(
            WayfarerItemIdentity.FailureReason reason,
            UUID itemId,
            String itemType
        ) {
            return new ParsedClaim(
                reason, itemId, itemType, null, null, null, null, null, OptionalLong.empty()
            );
        }
    }
}
