package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.core.transaction.TransactionException;
import io.github.eariver.wayfarer.core.transaction.TransactionRecord;
import io.github.eariver.wayfarer.core.transaction.TransactionRepository;
import io.github.eariver.wayfarer.core.transaction.TransactionStateMachine;
import io.github.eariver.wayfarer.core.transaction.TransactionUpdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public final class MariaDbTransactionRepository implements TransactionRepository {
    private static final String RECOVERABLE_STATES =
        "'PREPARED','DEBIT_PENDING','DEBITED','DOMAIN_COMMIT_PENDING',"
            + "'REFUND_PENDING','UNKNOWN'";

    private final InternalDatabase database;

    MariaDbTransactionRepository(InternalDatabase database) {
        this.database = database;
    }

    @Override
    public CompletionStage<TransactionRecord> prepare(
        UUID transactionId,
        WayfarerTransactions.TransactionRequest request,
        String debitOperationId,
        Instant now
    ) {
        return database.transaction(connection -> {
            int inserted;
            try (PreparedStatement statement = connection.prepareStatement(
                """
                INSERT IGNORE INTO wf_core_transaction (
                    transaction_id, idempotency_key, transaction_type, actor_uuid,
                    subject_type, subject_id, amount_wm, state, payload_json,
                    debit_operation_id, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PREPARED', ?, ?, ?, ?)
                """
            )) {
                statement.setString(1, transactionId.toString());
                statement.setString(2, request.idempotencyKey());
                statement.setString(3, request.transactionType());
                statement.setString(
                    4,
                    request.actorUuid() == null ? null : request.actorUuid().toString()
                );
                statement.setString(5, request.subjectType());
                statement.setString(6, request.subjectId());
                statement.setLong(7, request.amountWaymark());
                statement.setString(8, request.payloadJson());
                statement.setString(9, debitOperationId);
                statement.setTimestamp(10, Timestamp.from(now));
                statement.setTimestamp(11, Timestamp.from(now));
                inserted = statement.executeUpdate();
            }
            TransactionRecord record = findByIdempotency(connection, request.idempotencyKey())
                .orElseThrow(() -> new TransactionException("Transaction prepare failed"));
            requireSameRequest(record, request);
            if (inserted == 1) {
                insertEvent(connection, null, record, now);
            }
            return record;
        });
    }

    @Override
    public CompletionStage<Optional<TransactionRecord>> find(UUID transactionId) {
        return database.read(connection -> findById(connection, transactionId));
    }

    @Override
    public CompletionStage<Optional<TransactionRecord>> transition(
        TransactionRecord current,
        TransactionUpdate update,
        Instant now
    ) {
        TransactionStateMachine.requireAllowed(current.state(), update.nextState());
        return database.transaction(connection -> {
            int updated;
            try (PreparedStatement statement = connection.prepareStatement(
                """
                UPDATE wf_core_transaction
                SET state = ?,
                    debit_provider_reference =
                        COALESCE(?, debit_provider_reference),
                    refund_operation_id = COALESCE(?, refund_operation_id),
                    refund_provider_reference =
                        COALESCE(?, refund_provider_reference),
                    refund_terminal_state = COALESCE(?, refund_terminal_state),
                    recovery_claim_id = NULL,
                    recovery_claim_until = NULL,
                    failure_code = ?,
                    updated_at = ?,
                    completed_at = ?,
                    lock_version = lock_version + 1
                WHERE transaction_id = ? AND state = ? AND lock_version = ?
                """
            )) {
                statement.setString(1, update.nextState().name());
                statement.setString(2, update.debitProviderReference());
                statement.setString(3, update.refundOperationId());
                statement.setString(4, update.refundProviderReference());
                statement.setString(
                    5,
                    update.refundTerminalState() == null
                        ? null
                        : update.refundTerminalState().name()
                );
                statement.setString(6, update.failureCode());
                statement.setTimestamp(7, Timestamp.from(now));
                statement.setTimestamp(
                    8,
                    TransactionStateMachine.terminal(update.nextState())
                        ? Timestamp.from(now)
                        : null
                );
                statement.setString(9, current.transactionId().toString());
                statement.setString(10, current.state().name());
                statement.setLong(11, current.lockVersion());
                updated = statement.executeUpdate();
            }
            if (updated == 0) {
                return Optional.empty();
            }
            TransactionRecord record = findById(connection, current.transactionId())
                .orElseThrow(() -> new TransactionException("Transitioned transaction missing"));
            insertEvent(connection, current, record, now);
            return Optional.of(record);
        });
    }

    @Override
    public CompletionStage<Optional<TransactionRecord>> claimRecovery(
        TransactionRecord current,
        String claimId,
        Instant claimUntil,
        Instant now
    ) {
        return database.transaction(connection -> {
            int updated;
            try (PreparedStatement statement = connection.prepareStatement(
                """
                UPDATE wf_core_transaction
                SET recovery_claim_id = ?, recovery_claim_until = ?,
                    reconcile_attempts = reconcile_attempts + 1,
                    updated_at = ?, lock_version = lock_version + 1
                WHERE transaction_id = ? AND state = ? AND lock_version = ?
                  AND (recovery_claim_until IS NULL OR recovery_claim_until <= ?)
                """
            )) {
                statement.setString(1, claimId);
                statement.setTimestamp(2, Timestamp.from(claimUntil));
                statement.setTimestamp(3, Timestamp.from(now));
                statement.setString(4, current.transactionId().toString());
                statement.setString(5, current.state().name());
                statement.setLong(6, current.lockVersion());
                statement.setTimestamp(7, Timestamp.from(now));
                updated = statement.executeUpdate();
            }
            if (updated == 0) {
                return Optional.empty();
            }
            TransactionRecord record = findById(connection, current.transactionId())
                .orElseThrow(() -> new TransactionException("Claimed transaction missing"));
            insertEvent(connection, current, record, now);
            return Optional.of(record);
        });
    }

    @Override
    public CompletionStage<List<TransactionRecord>> findRecoverable(int limit) {
        if (limit <= 0 || limit > 1_000) {
            throw new IllegalArgumentException("Recovery limit must be between 1 and 1000");
        }
        return database.read(connection -> {
            List<TransactionRecord> records = new ArrayList<>();
            String sql = "SELECT * FROM wf_core_transaction WHERE state IN ("
                + RECOVERABLE_STATES
                + ") AND (recovery_claim_until IS NULL "
                + "OR recovery_claim_until <= CURRENT_TIMESTAMP(3)) "
                + "ORDER BY updated_at, transaction_id LIMIT ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, limit);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        records.add(map(result));
                    }
                }
            }
            return List.copyOf(records);
        });
    }

    private static Optional<TransactionRecord> findById(
        Connection connection,
        UUID transactionId
    ) throws SQLException {
        return query(
            connection,
            "SELECT * FROM wf_core_transaction WHERE transaction_id = ?",
            transactionId.toString()
        );
    }

    private static Optional<TransactionRecord> findByIdempotency(
        Connection connection,
        String idempotencyKey
    ) throws SQLException {
        return query(
            connection,
            "SELECT * FROM wf_core_transaction WHERE idempotency_key = ?",
            idempotencyKey
        );
    }

    private static Optional<TransactionRecord> query(
        Connection connection,
        String sql,
        String value
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    private static TransactionRecord map(ResultSet result) throws SQLException {
        String actor = result.getString("actor_uuid");
        Timestamp claimUntil = result.getTimestamp("recovery_claim_until");
        String refundTerminal = result.getString("refund_terminal_state");
        return new TransactionRecord(
            UUID.fromString(result.getString("transaction_id")),
            result.getString("idempotency_key"),
            result.getString("transaction_type"),
            actor == null ? null : UUID.fromString(actor),
            result.getString("subject_type"),
            result.getString("subject_id"),
            result.getLong("amount_wm"),
            result.getString("payload_json"),
            parseState(result.getString("state")),
            result.getString("debit_operation_id"),
            result.getString("debit_provider_reference"),
            result.getString("refund_operation_id"),
            result.getString("refund_provider_reference"),
            refundTerminal == null ? null : parseState(refundTerminal),
            result.getString("recovery_claim_id"),
            claimUntil == null ? null : claimUntil.toInstant(),
            result.getString("failure_code"),
            result.getLong("lock_version")
        );
    }

    private static WayfarerTransactions.State parseState(String value) {
        try {
            return WayfarerTransactions.State.valueOf(value);
        } catch (IllegalArgumentException failure) {
            throw new TransactionException("Unknown persisted transaction state");
        }
    }

    private static void insertEvent(
        Connection connection,
        TransactionRecord previous,
        TransactionRecord current,
        Instant now
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            INSERT INTO wf_core_transaction_event (
                transaction_id, from_state, to_state,
                debit_operation_id, debit_provider_reference,
                refund_operation_id, refund_provider_reference,
                refund_terminal_state, recovery_claim_id, recovery_claim_until,
                failure_code, transaction_lock_version, occurred_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
        )) {
            statement.setString(1, current.transactionId().toString());
            statement.setString(
                2,
                previous == null ? null : previous.state().name()
            );
            statement.setString(3, current.state().name());
            statement.setString(4, current.debitOperationId());
            statement.setString(5, current.debitProviderReference());
            statement.setString(6, current.refundOperationId());
            statement.setString(7, current.refundProviderReference());
            statement.setString(
                8,
                current.refundTerminalState() == null
                    ? null
                    : current.refundTerminalState().name()
            );
            statement.setString(9, current.recoveryClaimId());
            statement.setTimestamp(
                10,
                current.recoveryClaimUntil() == null
                    ? null
                    : Timestamp.from(current.recoveryClaimUntil())
            );
            statement.setString(11, current.failureCode());
            statement.setLong(12, current.lockVersion());
            statement.setTimestamp(13, Timestamp.from(now));
            statement.executeUpdate();
        }
    }

    private static void requireSameRequest(
        TransactionRecord record,
        WayfarerTransactions.TransactionRequest request
    ) {
        if (!record.sameRequest(request)) {
            throw new TransactionException("Idempotency key was reused with different input");
        }
    }
}
