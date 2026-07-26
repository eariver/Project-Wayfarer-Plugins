package io.github.eariver.wayfarer.common.secret;

@FunctionalInterface
public interface SecretReferenceResolver {
    SecretValue resolve(String reference);
}
