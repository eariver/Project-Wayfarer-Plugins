package io.github.eariver.wayfarer.api;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface WayfarerAudit {
    CompletionStage<Void> record(AuditEvent event);

    record AuditEvent(
        UUID eventId,
        String eventType,
        UUID actorUuid,
        String subjectType,
        String subjectId,
        String serverId,
        String detailsJson,
        Instant occurredAt
    ) {}
}
