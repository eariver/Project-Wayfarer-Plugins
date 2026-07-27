package io.github.eariver.wayfarer.core.command;

import java.util.UUID;

public record OperationalEvent(
    String eventType,
    UUID actorUuid,
    AudienceKind audienceKind,
    String subjectType,
    String subjectId,
    String safeDetailsJson
) {
    public enum AudienceKind {
        PLAYER,
        CONSOLE,
        OTHER
    }
}
