package io.github.eariver.wayfarer.core.persistence;

public final class PersistenceException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public PersistenceException(String message) {
        super(message);
    }
}
