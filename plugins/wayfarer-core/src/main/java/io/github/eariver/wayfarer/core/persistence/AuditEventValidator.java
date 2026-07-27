package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.common.secret.SecretValue;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

final class AuditEventValidator {
    private static final Pattern EVENT_TYPE = Pattern.compile("[A-Z0-9_.-]{1,96}");
    private static final Pattern SUBJECT_TYPE = Pattern.compile("[A-Z0-9_.-]{1,64}");
    private static final Pattern SERVER_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Set<String> RESERVED_SERVER_IDS = Set.of(
        "change_me", "change-me", "changeme", "default", "example"
    );
    private final String configuredServerId;
    private final SecretValue[] secrets;

    AuditEventValidator(SecretValue... secrets) {
        this("test-server", secrets);
    }

    AuditEventValidator(String configuredServerId, SecretValue... secrets) {
        matches(configuredServerId, SERVER_ID, "configuredServerId");
        if (RESERVED_SERVER_IDS.contains(
            configuredServerId.toLowerCase(java.util.Locale.ROOT)
        )) {
            throw new AuditValidationException("configuredServerId is reserved");
        }
        AuditSanitizer.validateText(configuredServerId, secrets);
        this.configuredServerId = configuredServerId;
        this.secrets = secrets.clone();
    }

    AuditRecord validate(WayfarerAudit.AuditEvent event) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(event.eventId(), "event.eventId");
        matches(event.eventType(), EVENT_TYPE, "event.eventType");
        AuditSanitizer.validateText(event.eventType(), secrets);
        matches(event.subjectType(), SUBJECT_TYPE, "event.subjectType");
        AuditSanitizer.validateText(event.subjectType(), secrets);
        String subjectId = boundedText(event.subjectId(), 191, "event.subjectId");
        AuditSanitizer.validateText(subjectId, secrets);
        matches(event.serverId(), SERVER_ID, "event.serverId");
        if (RESERVED_SERVER_IDS.contains(event.serverId().toLowerCase(java.util.Locale.ROOT))) {
            throw new AuditValidationException("event.serverId is reserved");
        }
        AuditSanitizer.validateText(event.serverId(), secrets);
        if (!configuredServerId.equals(event.serverId())) {
            throw new AuditValidationException("event.serverId does not match configured authority");
        }
        Objects.requireNonNull(event.occurredAt(), "event.occurredAt");
        String details = AuditSanitizer.validate(event.detailsJson(), secrets);
        return new AuditRecord(
            event.eventId(),
            event.eventType(),
            event.actorUuid(),
            event.subjectType(),
            subjectId,
            configuredServerId,
            details,
            event.occurredAt().truncatedTo(ChronoUnit.MILLIS)
        );
    }

    private static void matches(String value, Pattern pattern, String field) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new AuditValidationException(field + " is invalid");
        }
    }

    private static String boundedText(String value, int maximum, String field) {
        if (value == null || value.isBlank() || value.length() > maximum) {
            throw new AuditValidationException(field + " is invalid");
        }
        return value;
    }
}
