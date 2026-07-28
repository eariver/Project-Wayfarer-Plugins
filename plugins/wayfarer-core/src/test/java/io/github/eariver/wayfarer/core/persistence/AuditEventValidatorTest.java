package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditEventValidatorTest {
    private static final UUID EVENT_ID = UUID.fromString(
        "11111111-1111-1111-1111-111111111111"
    );

    @Test
    void validEventIsNormalizedToMilliseconds() {
        AuditRecord result = new AuditEventValidator("configured-server").validate(event(
            "CORE_READY",
            "{\"result\":\"ok\",\"duration_ms\":3}",
            Instant.parse("2026-07-27T12:00:00.123456Z"),
            "configured-server"
        ));

        assertEquals(Instant.parse("2026-07-27T12:00:00.123Z"), result.occurredAt());
        assertEquals("{\"result\":\"ok\",\"duration_ms\":3}", result.detailsJson());
        assertEquals("configured-server", result.serverId());
    }

    @Test
    void rejectsInvalidFieldsAndJsonWithoutInfrastructureClassification() {
        AuditEventValidator validator = new AuditEventValidator();
        assertThrows(
            AuditValidationException.class,
            () -> validator.validate(event("lowercase", "{}", Instant.now()))
        );
        assertThrows(
            AuditValidationException.class,
            () -> validator.validate(new WayfarerAudit.AuditEvent(
                EVENT_ID,
                "VALID",
                null,
                "CORE",
                " ",
                "test-server",
                "{}",
                Instant.now()
            ))
        );
        assertThrows(
            AuditValidationException.class,
            () -> validator.validate(event("VALID", "{\"broken\":}", Instant.now()))
        );
    }

    @Test
    void rejectsOversizedAndSensitiveDetails() {
        String oversized = "\"" + "a".repeat(AuditSanitizer.MAX_DETAILS_BYTES) + "\"";
        assertThrows(
            AuditValidationException.class,
            () -> new AuditEventValidator().validate(event("VALID", oversized, Instant.now()))
        );
        assertThrows(
            AuditValidationException.class,
            () -> new AuditEventValidator().validate(event(
                "VALID",
                "{\"password\":\"masked\"}",
                Instant.now()
            ))
        );
        assertThrows(
            AuditValidationException.class,
            () -> new AuditEventValidator().validate(event(
                "VALID",
                "{\"endpoint\":\"redis://cache.invalid\"}",
                Instant.now()
            ))
        );
    }

    @Test
    void rejectsResolvedSecretEvenWhenJsonEscaped() {
        try (SecretValue secret = SecretValue.of("sentinel/value")) {
            AuditEventValidator validator = new AuditEventValidator(secret);
            assertThrows(
                AuditValidationException.class,
                () -> validator.validate(event(
                    "VALID",
                    "{\"value\":\"sentinel\\/value\"}",
                    Instant.now()
                ))
            );
        }
    }

    @Test
    void rejectsSensitiveSubjectValue() {
        WayfarerAudit.AuditEvent event = new WayfarerAudit.AuditEvent(
            EVENT_ID,
            "VALID",
            null,
            "CORE",
            "jdbc:mariadb://db.invalid/core",
            "test-server",
            null,
            Instant.now()
        );
        assertThrows(
            AuditValidationException.class,
            () -> new AuditEventValidator().validate(event)
        );
    }

    @Test
    void rejectsSecretsAndCredentialMarkersInEveryPersistentCallerString() {
        List<WayfarerAudit.AuditEvent> events;
        try (SecretValue eventSecret = SecretValue.of("CORE_READY");
             SecretValue subjectTypeSecret = SecretValue.of("PLAYER_IDENTITY");
             SecretValue subjectIdSecret = SecretValue.of("safe-subject")) {
            events = List.of(
                new WayfarerAudit.AuditEvent(
                    EVENT_ID,
                    "CORE_READY",
                    null,
                    "CORE",
                    "safe-subject",
                    "configured-server",
                    "{}",
                    Instant.now()
                ),
                new WayfarerAudit.AuditEvent(
                    EVENT_ID,
                    "VALID",
                    null,
                    "PLAYER_IDENTITY",
                    "safe-subject",
                    "configured-server",
                    "{}",
                    Instant.now()
                ),
                new WayfarerAudit.AuditEvent(
                    EVENT_ID,
                    "VALID",
                    null,
                    "CORE",
                    "safe-subject",
                    "configured-server",
                    "{}",
                    Instant.now()
                )
            );
            List<SecretValue> secrets = List.of(
                eventSecret,
                subjectTypeSecret,
                subjectIdSecret
            );
            for (int index = 0; index < events.size(); index++) {
                AuditEventValidator validator = new AuditEventValidator(
                    "configured-server",
                    secrets.get(index)
                );
                WayfarerAudit.AuditEvent rejected = events.get(index);
                assertThrows(
                    AuditValidationException.class,
                    () -> validator.validate(rejected)
                );
            }
        }

        assertThrows(
            AuditValidationException.class,
            () -> new AuditEventValidator("configured-server").validate(
                new WayfarerAudit.AuditEvent(
                    EVENT_ID,
                    "ACCESS_TOKEN",
                    null,
                    "CORE",
                    "safe-subject",
                    "configured-server",
                    "{}",
                    Instant.now()
                )
            )
        );
        assertThrows(
            AuditValidationException.class,
            () -> new AuditEventValidator("configured-server").validate(
                new WayfarerAudit.AuditEvent(
                    EVENT_ID,
                    "VALID",
                    null,
                    "AUTHORIZATION",
                    "safe-subject",
                    "configured-server",
                    "{}",
                    Instant.now()
                )
            )
        );
    }

    @Test
    void rejectsCallerServerIdThatDoesNotMatchConfiguredAuthority() {
        AuditEventValidator validator = new AuditEventValidator("configured-server");
        assertThrows(
            AuditValidationException.class,
            () -> validator.validate(event("VALID", "{}", Instant.now(), "other-server"))
        );
    }

    private static WayfarerAudit.AuditEvent event(
        String type,
        String details,
        Instant occurredAt
    ) {
        return event(type, details, occurredAt, "test-server");
    }

    private static WayfarerAudit.AuditEvent event(
        String type,
        String details,
        Instant occurredAt,
        String serverId
    ) {
        return new WayfarerAudit.AuditEvent(
            EVENT_ID,
            type,
            null,
            "CORE",
            "test-server",
            serverId,
            details,
            occurredAt
        );
    }
}
