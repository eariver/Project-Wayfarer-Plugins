package io.github.eariver.wayfarer.common.secret;

public final class SecretResolutionException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public SecretResolutionException(String message) {
        super(message);
    }
}
