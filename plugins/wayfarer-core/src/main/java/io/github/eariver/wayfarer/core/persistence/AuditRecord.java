package io.github.eariver.wayfarer.core.persistence;

import java.time.Instant;
import java.util.UUID;

record AuditRecord(
    UUID eventId,
    String eventType,
    UUID actorUuid,
    String subjectType,
    String subjectId,
    String serverId,
    String detailsJson,
    Instant occurredAt
) {}
