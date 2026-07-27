package io.github.eariver.wayfarer.core.transaction;

public final class TransactionException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public TransactionException(String message) {
        super(message);
    }
}
