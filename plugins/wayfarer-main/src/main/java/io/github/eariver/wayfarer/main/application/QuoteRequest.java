package io.github.eariver.wayfarer.main.application;

import java.util.Objects;
import java.util.UUID;

public record QuoteRequest(UUID playerUuid) {
    public QuoteRequest {
        Objects.requireNonNull(playerUuid, "playerUuid");
    }
}
