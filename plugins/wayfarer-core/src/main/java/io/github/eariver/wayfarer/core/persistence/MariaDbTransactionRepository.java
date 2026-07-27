package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.core.transaction.TransactionException;
import io.github.eariver.wayfarer.core.transaction.TransactionRecord;
import io.github.eariver.wayfarer.core.transaction.TransactionRepository;
import io.github.eariver.wayfarer.core.transaction.TransactionStateMachine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public final class MariaDbTransactionRepository implements TransactionRepository {
    private final InternalDatabase database;

    MariaDbTransactionRepository(InternalDatabase database) {
        this.database = database;
    }

    @Override
    public CompletionStage<TransactionRecord> prepare(
        UUID transactionId,
        WayfarerTransactions.TransactionRequest request,
        String providerOperationId,
        Instant now
    ) {
        return database.transaction(connection -> {
            int inserted;
            try (PreparedStatement statement = connection.prepareStatement(
                """
                INSERT IGNORE INTO wf_core_transaction (
                    transaction_id, idempotency_key, transaction_type, actor_uuid,
                    subject_type, subject_id, amount_wm, state, payload_json,
                    provider_operation_id, created_at, updated_at
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
                statement.setString(9, providerOperationId);
                statement.setTimestamp(10, Timestamp.from(now));
                statement.setTimestamp(11, Timestamp.from(now));
                inserted = statement.executeUpdate();
            }
            TransactionRecord record = findByIdempotency(connection, request.idempotencyKey())
                .orElseThrow(() -> new TransactionException("Transaction prepare failed"));
            requireSameRequest(record, request);
            if (inserted == 1) {
                insertEvent(connection, record.transactionId(), null, record.state(), null, null, now);
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
        WayfarerTransactions.State next,
        String providerReference,
        String failureCode,
        Instant now
    ) {
        TransactionStateMachine.requireAllowed(current.state(), next);
        return database.transaction(connection -> {
            int updated;
            try (PreparedStatement statement = connection.prepareStatement(
                """
                UPDATE wf_core_transaction
                SET state = ?, provider_reference = COALESCE(?, provider_reference),
                    failure_code = ?, updated_at = ?, completed_at = ?,
                    reconcile_attempts = reconcile_attempts + ?,
                    lock_version = lock_version + 1
                WHERE transaction_id = ? AND state = ? AND lock_version = ?
                """
            )) {
                statement.setString(1, next.name());
                statement.setString(2, providerReference);
                statement.setString(3, failureCode);
                statement.setTimestamp(4, Timestamp.from(now));
                statement.setTimestamp(
                    5,
                    TransactionStateMachine.terminal(next) ? Timestamp.from(now) : null
                );
                statement.setInt(6, current.state() == WayfarerTransactions.State.UNKNOWN ? 1 : 0);
                statement.setString(7, current.transactionId().toString());
                statement.setString(8, current.state().name());
                statement.setLong(9, current.lockVersion());
                updated = statement.executeUpdate();
            }
            if (updated == 0) {
                return Optional.empty();
            }
            insertEvent(
                connection,
                current.transactionId(),
                current.state(),
                next,
                providerReference,
                failureCode,
                now
            );
            return findById(connection, current.transactionId());
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
        return new TransactionRecord(
            UUID.fromString(result.getString("transaction_id")),
            result.getString("idempotency_key"),
            result.getString("transaction_type"),
            actor == null ? null : UUID.fromString(actor),
            result.getString("subject_type"),
            result.getString("subject_id"),
            result.getLong("amount_wm"),
            parseState(result.getString("state")),
            result.getString("provider_reference"),
            result.getString("provider_operation_id"),
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
        UUID transactionId,
        WayfarerTransactions.State from,
        WayfarerTransactions.State to,
        String providerReference,
        String failureCode,
        Instant now
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            INSERT INTO wf_core_transaction_event (
                transaction_id, from_state, to_state, provider_reference,
                failure_code, occurred_at
            ) VALUES (?, ?, ?, ?, ?, ?)
            """
        )) {
            statement.setString(1, transactionId.toString());
            statement.setString(2, from == null ? null : from.name());
            statement.setString(3, to.name());
            statement.setString(4, providerReference);
            statement.setString(5, failureCode);
            statement.setTimestamp(6, Timestamp.from(now));
            statement.executeUpdate();
        }
    }

    private static void requireSameRequest(
        TransactionRecord record,
        WayfarerTransactions.TransactionRequest request
    ) {
        if (!record.transactionType().equals(request.transactionType())
            || !java.util.Objects.equals(record.actorUuid(), request.actorUuid())
            || !record.subjectType().equals(request.subjectType())
            || !record.subjectId().equals(request.subjectId())
            || record.amountWaymark() != request.amountWaymark()) {
            throw new TransactionException("Idempotency key was reused with different input");
        }
    }
}
