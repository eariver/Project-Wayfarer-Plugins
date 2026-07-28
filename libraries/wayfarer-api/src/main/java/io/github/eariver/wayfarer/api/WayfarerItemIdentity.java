package io.github.eariver.wayfarer.api;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface WayfarerItemIdentity {
    CompletionStage<Identity> create(CreateRequest request);

    CompletionStage<Optional<Identity>> find(UUID itemInstanceId);

    CompletionStage<ValidationResult> validate(ValidationRequest request);

    record CreateRequest(
        String itemType,
        UUID ownerUuid,
        long instanceEpoch,
        int schemaVersion,
        int displayRevision
    ) {}

    record Identity(
        UUID itemInstanceId,
        String itemType,
        UUID ownerUuid,
        long instanceEpoch,
        int schemaVersion,
        int displayRevision,
        Instant createdAt,
        Instant updatedAt,
        long lockVersion
    ) {}

    record RawClaim(
        String itemInstanceId,
        String itemType,
        String ownerUuid,
        Long instanceEpoch,
        Integer schemaVersion,
        Integer displayRevision
    ) {}

    record ValidationRequest(
        RawClaim claim,
        Set<String> allowedItemTypes,
        Set<Integer> supportedSchemaVersions,
        UUID expectedOwnerUuid,
        OptionalLong expectedInstanceEpoch
    ) {
        public ValidationRequest {
            Objects.requireNonNull(claim, "claim");
            allowedItemTypes = Set.copyOf(allowedItemTypes);
            supportedSchemaVersions = Set.copyOf(supportedSchemaVersions);
            Objects.requireNonNull(expectedOwnerUuid, "expectedOwnerUuid");
            Objects.requireNonNull(expectedInstanceEpoch, "expectedInstanceEpoch");
        }
    }

    record ValidationResult(
        boolean valid,
        Optional<Identity> identity,
        Optional<FailureReason> failureReason
    ) {
        public ValidationResult {
            identity = Objects.requireNonNull(identity, "identity");
            failureReason = Objects.requireNonNull(failureReason, "failureReason");
            if ((valid && (identity.isEmpty() || failureReason.isPresent()))
                || (!valid && (identity.isPresent() || failureReason.isEmpty()))) {
                throw new IllegalArgumentException("Validation result state is inconsistent");
            }
        }

        public static ValidationResult valid(Identity identity) {
            return new ValidationResult(true, Optional.of(identity), Optional.empty());
        }

        public static ValidationResult invalid(FailureReason reason) {
            return new ValidationResult(false, Optional.empty(), Optional.of(reason));
        }
    }

    enum FailureReason {
        MISSING_FIELD,
        INVALID_ITEM_INSTANCE_ID,
        INVALID_OWNER_UUID,
        INVALID_INSTANCE_EPOCH,
        INVALID_SCHEMA_VERSION,
        INVALID_DISPLAY_REVISION,
        UNKNOWN_ITEM_TYPE,
        UNKNOWN_SCHEMA_VERSION,
        IDENTITY_NOT_FOUND,
        ITEM_TYPE_MISMATCH,
        OWNER_MISMATCH,
        EPOCH_MISMATCH,
        SCHEMA_MISMATCH,
        DISPLAY_REVISION_MISMATCH
    }
}
