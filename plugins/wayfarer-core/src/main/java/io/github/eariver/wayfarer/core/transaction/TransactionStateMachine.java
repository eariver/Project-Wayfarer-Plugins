package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerTransactions.State;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public final class TransactionStateMachine {
    private static final Map<State, EnumSet<State>> ALLOWED = allowed();

    private TransactionStateMachine() {}

    public static void requireAllowed(State from, State to) {
        if (!ALLOWED.getOrDefault(from, EnumSet.noneOf(State.class)).contains(to)) {
            throw new TransactionException("Illegal transaction state transition");
        }
    }

    public static boolean terminal(State state) {
        return switch (state) {
            case COMMITTED, REFUNDED, RECONCILED_COMMITTED,
                RECONCILED_REFUNDED, FAILED -> true;
            default -> false;
        };
    }

    private static Map<State, EnumSet<State>> allowed() {
        Map<State, EnumSet<State>> transitions = new EnumMap<>(State.class);
        transitions.put(State.PREPARED, EnumSet.of(State.DEBIT_PENDING, State.FAILED));
        transitions.put(
            State.DEBIT_PENDING,
            EnumSet.of(State.DEBITED, State.UNKNOWN, State.FAILED)
        );
        transitions.put(
            State.DEBITED,
            EnumSet.of(State.DOMAIN_COMMIT_PENDING, State.REFUND_PENDING, State.UNKNOWN)
        );
        transitions.put(
            State.DOMAIN_COMMIT_PENDING,
            EnumSet.of(State.COMMITTED, State.REFUND_PENDING, State.UNKNOWN)
        );
        transitions.put(
            State.REFUND_PENDING,
            EnumSet.of(
                State.REFUNDED,
                State.RECONCILED_REFUNDED,
                State.UNKNOWN,
                State.FAILED
            )
        );
        transitions.put(
            State.UNKNOWN,
            EnumSet.of(
                State.REFUND_PENDING,
                State.RECONCILED_COMMITTED,
                State.RECONCILED_REFUNDED,
                State.FAILED
            )
        );
        return Map.copyOf(transitions);
    }
}
