package io.github.eariver.wayfarer.common.secret;

import java.util.Map;
import java.util.Objects;

public final class EnvironmentSecretResolver implements SecretReferenceResolver {
    private final Map<String, String> environment;

    public EnvironmentSecretResolver() {
        this(System.getenv());
    }

    public EnvironmentSecretResolver(Map<String, String> environment) {
        this.environment = Map.copyOf(Objects.requireNonNull(environment, "environment"));
    }

    @Override
    public SecretValue resolve(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new SecretResolutionException("Secret reference must not be blank");
        }
        String value = environment.get(reference);
        if (value == null || value.isBlank()) {
            throw new SecretResolutionException("Required secret reference is unavailable: " + reference);
        }
        return SecretValue.of(value);
    }
}
