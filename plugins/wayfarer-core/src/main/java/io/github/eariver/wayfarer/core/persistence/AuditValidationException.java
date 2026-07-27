package io.github.eariver.wayfarer.core.persistence;

final class AuditValidationException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    AuditValidationException(String message) {
        super(message);
    }
}
