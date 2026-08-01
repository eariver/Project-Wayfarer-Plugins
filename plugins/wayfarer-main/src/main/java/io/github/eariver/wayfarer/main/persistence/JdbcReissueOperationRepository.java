package io.github.eariver.wayfarer.main.persistence;

import io.github.eariver.wayfarer.main.application.PrepareOutcome;
import io.github.eariver.wayfarer.main.application.PrepareResult;
import io.github.eariver.wayfarer.main.application.ReissueOperationRepository;
import io.github.eariver.wayfarer.main.domain.ReissueOperation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

/** JDBC implementation for REISSUE rows in the shared repair table. */
public final class JdbcReissueOperationRepository
    implements ReissueOperationRepository {
    private static final String KIND = "REISSUE";

    private final DataSource dataSource;

    public JdbcReissueOperationRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public Optional<ReissueOperation> find(UUID reissueId) {
        Objects.requireNonNull(reissueId, "reissueId");
        try (Connection connection = dataSource.getConnection()) {
            return find(connection, reissueId);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<ReissueOperation> findByIdempotency(String idempotencyKey) {
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        try (Connection connection = dataSource.getConnection()) {
            return findByIdempotency(connection, idempotencyKey);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<ReissueOperation> findActiveByTool(UUID toolId) {
        Objects.requireNonNull(toolId, "toolId");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement query = connection.prepareStatement(
                 "SELECT * FROM wf_main_repair_operation "
                     + "WHERE active_guard=? AND operation_kind='REISSUE'"
             )) {
            query.setString(1, toolId.toString());
            return one(query);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public PrepareOutcome prepare(ReissueOperation operation, Instant now) {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(now, "now");
        if (operation.state() != ReissueOperation.State.PREPARED
            || operation.transactionId() != null
            || operation.paymentCommittedAt() != null) {
            throw new IllegalArgumentException("Reissue prepare requires PREPARED state");
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO wf_main_repair_operation "
                     + "(repair_id,idempotency_key,operation_kind,player_uuid,tool_id,"
                     + "instance_epoch,amount_waymark,state,config_revision,evolution_count,"
                     + "expected_item_instance_id,new_item_instance_id,payment_committed_at,"
                     + "active_guard,created_at,updated_at) "
                     + "VALUES (?,?,'REISSUE',?,?,?,?, 'PREPARED',?,?,?,?,NULL,?,?,?) "
                     + "ON DUPLICATE KEY UPDATE idempotency_key=idempotency_key"
             )) {
            insert.setString(1, operation.reissueId().toString());
            insert.setString(2, operation.idempotencyKey());
            insert.setString(3, operation.playerUuid().toString());
            insert.setString(4, operation.toolId().toString());
            insert.setLong(5, operation.instanceEpoch());
            insert.setLong(6, operation.amountWaymark());
            insert.setString(7, operation.configRevision());
            insert.setInt(8, operation.evolutionCount());
            insert.setString(9, operation.expectedItemInstanceId().toString());
            insert.setString(10, operation.newItemInstanceId().toString());
            insert.setString(11, operation.toolId().toString());
            insert.setTimestamp(12, Timestamp.from(now));
            insert.setTimestamp(13, Timestamp.from(now));
            insert.executeUpdate();

            Optional<ReissueOperation> existing = findByIdempotency(
                connection,
                operation.idempotencyKey()
            );
            if (existing.isPresent()) {
                ReissueOperation value = existing.orElseThrow();
                PrepareResult result = value.reissueId().equals(operation.reissueId())
                    ? PrepareResult.CREATED
                    : PrepareResult.EXISTING;
                return new PrepareOutcome(result, value);
            }
            Optional<ReissueOperation> active = findActiveByTool(connection, operation.toolId());
            return active.map(value -> new PrepareOutcome(PrepareResult.IN_FLIGHT, value))
                .orElseThrow(() -> unavailable("Reissue prepare did not produce a row"));
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<ReissueOperation> claimPayment(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            reissueId,
            expectedLockVersion,
            "PREPARED",
            "PAYMENT_PENDING",
            now,
            "UPDATE wf_main_repair_operation SET state='PAYMENT_PENDING',"
                + "lock_version=lock_version+1,updated_at=? "
                + "WHERE repair_id=? AND operation_kind='REISSUE' "
                + "AND state='PREPARED' AND lock_version=?"
        );
    }

    @Override
    public Optional<ReissueOperation> paymentCommitted(
        UUID reissueId,
        UUID transactionId,
        long expectedLockVersion,
        Instant now
    ) {
        Objects.requireNonNull(transactionId, "transactionId");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_repair_operation SET state='PAYMENT_COMMITTED',"
                     + "transaction_id=?,payment_committed_at=?,failure_code=NULL,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE repair_id=? AND operation_kind='REISSUE' "
                     + "AND state='PAYMENT_PENDING' AND lock_version=? "
                     + "AND (transaction_id IS NULL OR transaction_id=?)"
             )) {
            update.setString(1, transactionId.toString());
            update.setTimestamp(2, Timestamp.from(now));
            update.setTimestamp(3, Timestamp.from(now));
            update.setString(4, reissueId.toString());
            update.setLong(5, expectedLockVersion);
            update.setString(6, transactionId.toString());
            return update.executeUpdate() == 1
                ? find(connection, reissueId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<ReissueOperation> unknown(
        UUID reissueId,
        long expectedLockVersion,
        UUID transactionId,
        String failureCode,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_repair_operation SET state='UNKNOWN',"
                     + "transaction_id=COALESCE(?,transaction_id),failure_code=?,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE repair_id=? AND operation_kind='REISSUE' "
                     + "AND lock_version=? "
                     + "AND state NOT IN ('PENDING_DELIVERY','DELIVERED','FAILED','ABANDONED') "
                     + "AND (? IS NULL OR transaction_id IS NULL OR transaction_id=?)"
             )) {
            setUuid(update, 1, transactionId);
            update.setString(2, failureCode);
            update.setTimestamp(3, Timestamp.from(now));
            update.setString(4, reissueId.toString());
            update.setLong(5, expectedLockVersion);
            setUuid(update, 6, transactionId);
            setUuid(update, 7, transactionId);
            return update.executeUpdate() == 1
                ? find(connection, reissueId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<ReissueOperation> failed(
        UUID reissueId,
        UUID transactionId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_repair_operation SET state='FAILED',"
                     + "transaction_id=COALESCE(?,transaction_id),active_guard=NULL,"
                     + "failure_code=?,lock_version=lock_version+1,updated_at=? "
                     + "WHERE repair_id=? AND operation_kind='REISSUE' "
                     + "AND state='PAYMENT_PENDING' AND lock_version=? "
                     + "AND (? IS NULL OR transaction_id IS NULL OR transaction_id=?)"
             )) {
            setUuid(update, 1, transactionId);
            update.setString(2, failureCode);
            update.setTimestamp(3, Timestamp.from(now));
            update.setString(4, reissueId.toString());
            update.setLong(5, expectedLockVersion);
            setUuid(update, 6, transactionId);
            setUuid(update, 7, transactionId);
            return update.executeUpdate() == 1
                ? find(connection, reissueId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<ReissueOperation> abandoned(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            reissueId,
            expectedLockVersion,
            "PREPARED",
            "ABANDONED",
            now,
            "UPDATE wf_main_repair_operation SET state='ABANDONED',"
                + "active_guard=NULL,lock_version=lock_version+1,updated_at=? "
                + "WHERE repair_id=? AND operation_kind='REISSUE' "
                + "AND state='PREPARED' AND lock_version=?"
        );
    }

    @Override
    public Optional<ReissueOperation> pendingDelivery(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            reissueId,
            expectedLockVersion,
            "PAYMENT_COMMITTED",
            "PENDING_DELIVERY",
            now,
            "UPDATE wf_main_repair_operation SET state='PENDING_DELIVERY',"
                + "active_guard=NULL,lock_version=lock_version+1,updated_at=? "
                + "WHERE repair_id=? AND operation_kind='REISSUE' "
                + "AND state='PAYMENT_COMMITTED' AND lock_version=?"
        );
    }

    @Override
    public boolean delivered(UUID reissueId, long expectedLockVersion, Instant now) {
        return transition(
            reissueId,
            expectedLockVersion,
            "PENDING_DELIVERY",
            "DELIVERED",
            now,
            "UPDATE wf_main_repair_operation SET state='DELIVERED',"
                + "active_guard=NULL,lock_version=lock_version+1,updated_at=? "
                + "WHERE repair_id=? AND operation_kind='REISSUE' "
                + "AND state='PENDING_DELIVERY' AND lock_version=?"
        ).isPresent();
    }

    @Override
    public Optional<ReissueOperation> reopenPayment(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            reissueId,
            expectedLockVersion,
            "UNKNOWN",
            "PAYMENT_PENDING",
            now,
            "UPDATE wf_main_repair_operation SET state='PAYMENT_PENDING',"
                + "lock_version=lock_version+1,updated_at=? "
                + "WHERE repair_id=? AND operation_kind='REISSUE' "
                + "AND state='UNKNOWN' AND lock_version=? "
                + "AND payment_committed_at IS NULL AND transaction_id IS NULL"
        );
    }

    @Override
    public Optional<ReissueOperation> reopenToPaymentCommitted(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            reissueId,
            expectedLockVersion,
            "UNKNOWN",
            "PAYMENT_COMMITTED",
            now,
            "UPDATE wf_main_repair_operation SET state='PAYMENT_COMMITTED',"
                + "lock_version=lock_version+1,updated_at=? "
                + "WHERE repair_id=? AND operation_kind='REISSUE' "
                + "AND state='UNKNOWN' AND lock_version=? "
                + "AND payment_committed_at IS NOT NULL"
        );
    }

    @Override
    public Optional<ReissueOperation> confirmPaymentCommittedFromUnknown(
        UUID reissueId,
        UUID expectedTransactionId,
        long expectedLockVersion,
        Instant committedAt,
        Instant now
    ) {
        Objects.requireNonNull(expectedTransactionId, "expectedTransactionId");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_repair_operation SET state='PAYMENT_COMMITTED',"
                     + "payment_committed_at=?,failure_code=NULL,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE repair_id=? AND operation_kind='REISSUE' "
                     + "AND state='UNKNOWN' AND transaction_id=? "
                     + "AND payment_committed_at IS NULL AND lock_version=?"
             )) {
            update.setTimestamp(1, Timestamp.from(committedAt));
            update.setTimestamp(2, Timestamp.from(now));
            update.setString(3, reissueId.toString());
            update.setString(4, expectedTransactionId.toString());
            update.setLong(5, expectedLockVersion);
            return update.executeUpdate() == 1
                ? find(connection, reissueId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<ReissueOperation> failFromUnknown(
        UUID reissueId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    ) {
        return transition(
            reissueId,
            expectedLockVersion,
            "UNKNOWN",
            "FAILED",
            now,
            "UPDATE wf_main_repair_operation SET state='FAILED',"
                + "active_guard=NULL,failure_code=?,"
                + "lock_version=lock_version+1,updated_at=? "
                + "WHERE repair_id=? AND operation_kind='REISSUE' "
                + "AND state='UNKNOWN' AND lock_version=? "
                + "AND payment_committed_at IS NULL"
        , failureCode);
    }

    @Override
    public List<ReissueOperation> findRecoveryCandidates() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement query = connection.prepareStatement(
                 "SELECT * FROM wf_main_repair_operation "
                     + "WHERE operation_kind='REISSUE' "
                     + "AND state IN ('PREPARED','PAYMENT_PENDING',"
                     + "'PAYMENT_COMMITTED','PENDING_DELIVERY') "
                     + "ORDER BY created_at"
             );
             ResultSet result = query.executeQuery()) {
            List<ReissueOperation> operations = new ArrayList<>();
            while (result.next()) {
                operations.add(one(result));
            }
            return List.copyOf(operations);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private Optional<ReissueOperation> transition(
        UUID reissueId,
        long expectedLockVersion,
        String expectedState,
        String nextState,
        Instant now,
        String sql
    ) {
        return transition(
            reissueId,
            expectedLockVersion,
            expectedState,
            nextState,
            now,
            sql,
            null
        );
    }

    private Optional<ReissueOperation> transition(
        UUID reissueId,
        long expectedLockVersion,
        String expectedState,
        String nextState,
        Instant now,
        String sql,
        String failureCode
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(sql)) {
            int index = 1;
            if (failureCode != null) {
                update.setString(index++, failureCode);
            }
            update.setTimestamp(index++, Timestamp.from(now));
            update.setString(index++, reissueId.toString());
            update.setLong(index, expectedLockVersion);
            if (update.executeUpdate() != 1) {
                return Optional.empty();
            }
            Optional<ReissueOperation> result = find(connection, reissueId);
            if (result.isEmpty() || result.orElseThrow().state().name().equals(nextState)) {
                return result;
            }
            throw unavailable("Unexpected reissue transition result from " + expectedState);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private static Optional<ReissueOperation> find(
        Connection connection,
        UUID reissueId
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_main_repair_operation "
                + "WHERE repair_id=? AND operation_kind='REISSUE'"
        )) {
            query.setString(1, reissueId.toString());
            return one(query);
        }
    }

    private static Optional<ReissueOperation> findByIdempotency(
        Connection connection,
        String idempotencyKey
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_main_repair_operation "
                + "WHERE idempotency_key=? AND operation_kind='REISSUE'"
        )) {
            query.setString(1, idempotencyKey);
            return one(query);
        }
    }

    private static Optional<ReissueOperation> findActiveByTool(
        Connection connection,
        UUID toolId
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_main_repair_operation "
                + "WHERE active_guard=? AND operation_kind='REISSUE'"
        )) {
            query.setString(1, toolId.toString());
            return one(query);
        }
    }

    private static Optional<ReissueOperation> one(PreparedStatement query)
        throws SQLException {
        try (ResultSet result = query.executeQuery()) {
            return result.next() ? Optional.of(one(result)) : Optional.empty();
        }
    }

    private static ReissueOperation one(ResultSet result) throws SQLException {
        String transaction = result.getString("transaction_id");
        Timestamp paymentCommitted = result.getTimestamp("payment_committed_at");
        return new ReissueOperation(
            UUID.fromString(result.getString("repair_id")),
            result.getString("idempotency_key"),
            UUID.fromString(result.getString("player_uuid")),
            UUID.fromString(result.getString("tool_id")),
            UUID.fromString(result.getString("expected_item_instance_id")),
            UUID.fromString(result.getString("new_item_instance_id")),
            result.getLong("instance_epoch"),
            result.getInt("evolution_count"),
            result.getString("config_revision"),
            result.getLong("amount_waymark"),
            ReissueOperation.State.valueOf(result.getString("state")),
            transaction == null ? null : UUID.fromString(transaction),
            paymentCommitted == null ? null : paymentCommitted.toInstant(),
            result.getString("failure_code"),
            result.getLong("lock_version")
        );
    }

    private static void setUuid(
        PreparedStatement statement,
        int index,
        UUID value
    ) throws SQLException {
        statement.setString(index, value == null ? null : value.toString());
    }

    private static IllegalStateException unavailable() {
        return unavailable("Main reissue repository operation failed");
    }

    private static IllegalStateException unavailable(String message) {
        return new IllegalStateException(message);
    }
}
