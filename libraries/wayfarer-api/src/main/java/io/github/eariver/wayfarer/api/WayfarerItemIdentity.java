package io.github.eariver.wayfarer.api;

import java.util.UUID;

public interface WayfarerItemIdentity {
    Identity newIdentity(String itemType, UUID ownerUuid, long instanceEpoch, int schemaVersion);

    record Identity(
        UUID itemInstanceId,
        String itemType,
        UUID ownerUuid,
        long instanceEpoch,
        int schemaVersion
    ) {}
}
