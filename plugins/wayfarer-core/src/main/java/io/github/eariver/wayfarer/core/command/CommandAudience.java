package io.github.eariver.wayfarer.core.command;

import java.util.Optional;
import java.util.UUID;

public interface CommandAudience {
    boolean hasPermission(String permission);
    boolean console();
    Optional<UUID> actorUuid();
    OperationalEvent.AudienceKind audienceKind();
    void sendMessage(String message);
}
