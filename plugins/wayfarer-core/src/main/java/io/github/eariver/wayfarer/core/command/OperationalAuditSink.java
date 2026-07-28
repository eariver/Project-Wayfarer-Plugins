package io.github.eariver.wayfarer.core.command;

import io.github.eariver.wayfarer.api.WayfarerAudit;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public final class OperationalAuditSink implements OperationalEventSink {
    private final WayfarerAudit audit;
    private final String serverId;
    private final Clock clock;

    public OperationalAuditSink(WayfarerAudit audit, String serverId, Clock clock) {
        this.audit = Objects.requireNonNull(audit, "audit");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CompletionStage<Void> record(OperationalEvent event) {
        Objects.requireNonNull(event, "event");
        String details = event.safeDetailsJson();
        if (details == null) {
            details = "{\"audience_kind\":\"" + event.audienceKind().name() + "\"}";
        }
        return audit.record(new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            event.eventType(),
            event.actorUuid(),
            event.subjectType(),
            event.subjectId(),
            serverId,
            details,
            clock.instant()
        ));
    }
}
