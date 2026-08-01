package io.github.eariver.wayfarer.main.application;

import java.util.Objects;
import java.util.UUID;

public record ConfirmRequest(UUID playerUuid) {
    public ConfirmRequest {
        Objects.requireNonNull(playerUuid, "playerUuid");
    }
}
