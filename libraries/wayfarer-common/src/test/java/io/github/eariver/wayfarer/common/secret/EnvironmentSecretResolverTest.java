package io.github.eariver.wayfarer.common.secret;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvironmentSecretResolverTest {
    @Test
    void resolvesReferencedEnvironmentValue() {
        EnvironmentSecretResolver resolver = new EnvironmentSecretResolver(
            Map.of("WAYFARER_TEST_SECRET", "expected-value")
        );
        try (SecretValue secret = resolver.resolve("WAYFARER_TEST_SECRET")) {
            assertEquals("expected-value", secret.use(String::new));
        }
    }

    @Test
    void missingReferenceFailsClosed() {
        EnvironmentSecretResolver resolver = new EnvironmentSecretResolver(Map.of());
        SecretResolutionException failure = assertThrows(
            SecretResolutionException.class,
            () -> resolver.resolve("WAYFARER_MISSING")
        );
        assertFalse(failure.getMessage().contains("expected-value"));
    }

    @Test
    void blankReferenceFailsClosed() {
        EnvironmentSecretResolver resolver = new EnvironmentSecretResolver(Map.of());
        assertThrows(SecretResolutionException.class, () -> resolver.resolve(" "));
    }
}
