package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerTransactions;

public record TransactionUpdate(
    WayfarerTransactions.State nextState,
    String debitProviderReference,
    String refundOperationId,
    String refundProviderReference,
    WayfarerTransactions.State refundTerminalState,
    String failureCode
) {
    public TransactionUpdate {
        if (nextState == null) {
            throw new IllegalArgumentException("nextState is required");
        }
    }

    public static TransactionUpdate to(
        WayfarerTransactions.State nextState,
        String failureCode
    ) {
        return new TransactionUpdate(nextState, null, null, null, null, failureCode);
    }
}
