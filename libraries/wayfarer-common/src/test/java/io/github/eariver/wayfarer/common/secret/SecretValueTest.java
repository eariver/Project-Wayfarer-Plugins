package io.github.eariver.wayfarer.common.secret;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecretValueTest {
    @Test
    void toStringIsAlwaysRedacted() {
        try (SecretValue secret = SecretValue.of("do-not-print")) {
            assertEquals("[REDACTED]", secret.toString());
        }
    }

    @Test
    void redactsSecretFromDiagnosticText() {
        try (SecretValue secret = SecretValue.of("do-not-print")) {
            String redacted = secret.redact("value=do-not-print");
            assertEquals("value=[REDACTED]", redacted);
            assertFalse(redacted.contains("do-not-print"));
        }
    }

    @Test
    void closesAndRejectsFurtherUse() {
        SecretValue secret = SecretValue.of("do-not-print");
        secret.close();
        assertThrows(IllegalStateException.class, () -> secret.use(String::new));
    }

    @Test
    void blankSecretIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> SecretValue.of(" "));
    }
}
