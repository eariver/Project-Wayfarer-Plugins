package io.github.eariver.wayfarer.main.persistence;

import io.github.eariver.wayfarer.main.application.RepairOperationRepository;
import io.github.eariver.wayfarer.main.domain.RepairOperation;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class JdbcRepairOperationRepository
    implements RepairOperationRepository {
    private final DataSource dataSource;

    public JdbcRepairOperationRepository(DataSource dataSource) {
        this.dataSource = java.util.Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public Optional<RepairOperation> find(UUID repairId) {
        try (Connection connection = dataSource.getConnection()) {
            return find(connection, repairId);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public RepairOperation prepare(
        String idempotencyKey,
        UUID playerUuid,
        UUID toolId,
        long instanceEpoch,
        long amountWaymark,
        Instant now
    ) {
        UUID repairId = UUID.randomUUID();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO wf_main_repair_operation "
                     + "(repair_id,idempotency_key,player_uuid,tool_id,instance_epoch,"
                     + "amount_waymark,state,created_at,updated_at) "
                     + "VALUES (?,?,?,?,?,?,'PREPARED',?,?) "
                     + "ON DUPLICATE KEY UPDATE idempotency_key=idempotency_key"
             )) {
            insert.setString(1, repairId.toString());
            insert.setString(2, idempotencyKey);
            insert.setString(3, playerUuid.toString());
            insert.setString(4, toolId.toString());
            insert.setLong(5, instanceEpoch);
            insert.setLong(6, amountWaymark);
            insert.setTimestamp(7, Timestamp.from(now));
            insert.setTimestamp(8, Timestamp.from(now));
            insert.executeUpdate();
            return findByIdempotency(connection, idempotencyKey).orElseThrow();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<RepairOperation> claimPayment(
        UUID repairId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            repairId,
            expectedLockVersion,
            "PREPARED",
            "PAYMENT_PENDING",
            null,
            null,
            now
        );
    }

    @Override
    public Optional<RepairOperation> paymentCommitted(
        UUID repairId,
        UUID transactionId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            repairId,
            expectedLockVersion,
            "PAYMENT_PENDING",
            "PAYMENT_COMMITTED",
            transactionId,
            null,
            now
        );
    }

    @Override
    public Optional<RepairOperation> failed(
        UUID repairId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    ) {
        return transition(
            repairId,
            expectedLockVersion,
            "PAYMENT_PENDING",
            "FAILED",
            null,
            failureCode,
            now
        );
    }

    @Override
    public boolean domainCommitted(
        UUID repairId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            repairId,
            expectedLockVersion,
            "PAYMENT_COMMITTED",
            "DOMAIN_COMMITTED",
            null,
            null,
            now
        ).isPresent();
    }

    @Override
    public Optional<RepairOperation> claimRefund(
        UUID repairId,
        long expectedLockVersion,
        Instant now
    ) {
        String refundId = "main-refund:" + repairId;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_repair_operation SET state='REFUND_PENDING',"
                     + "refund_operation_id=?,lock_version=lock_version+1,updated_at=? "
                     + "WHERE repair_id=? AND state='PAYMENT_COMMITTED' AND lock_version=?"
             )) {
            update.setString(1, refundId);
            update.setTimestamp(2, Timestamp.from(now));
            update.setString(3, repairId.toString());
            update.setLong(4, expectedLockVersion);
            return update.executeUpdate() == 1
                ? find(connection, repairId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean refunded(UUID repairId, long expectedLockVersion, Instant now) {
        return transition(
            repairId,
            expectedLockVersion,
            "REFUND_PENDING",
            "REFUNDED",
            null,
            null,
            now
        ).isPresent();
    }

    @Override
    public void unknown(
        UUID repairId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_repair_operation SET state='UNKNOWN',failure_code=?,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE repair_id=? AND lock_version=? "
                     + "AND state NOT IN ('DOMAIN_COMMITTED','REFUNDED','FAILED')"
             )) {
            update.setString(1, failureCode);
            update.setTimestamp(2, Timestamp.from(now));
            update.setString(3, repairId.toString());
            update.setLong(4, expectedLockVersion);
            update.executeUpdate();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private Optional<RepairOperation> transition(
        UUID repairId,
        long expectedLockVersion,
        String expectedState,
        String nextState,
        UUID transactionId,
        String failureCode,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_repair_operation SET state=?,"
                     + "transaction_id=COALESCE(?,transaction_id),failure_code=?,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE repair_id=? AND state=? AND lock_version=?"
             )) {
            update.setString(1, nextState);
            update.setString(2, transactionId == null ? null : transactionId.toString());
            update.setString(3, failureCode);
            update.setTimestamp(4, Timestamp.from(now));
            update.setString(5, repairId.toString());
            update.setString(6, expectedState);
            update.setLong(7, expectedLockVersion);
            return update.executeUpdate() == 1
                ? find(connection, repairId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private static Optional<RepairOperation> findByIdempotency(
        Connection connection,
        String idempotencyKey
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_main_repair_operation WHERE idempotency_key=?"
        )) {
            query.setString(1, idempotencyKey);
            return one(query);
        }
    }

    private static Optional<RepairOperation> find(
        Connection connection,
        UUID repairId
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_main_repair_operation WHERE repair_id=?"
        )) {
            query.setString(1, repairId.toString());
            return one(query);
        }
    }

    private static Optional<RepairOperation> one(PreparedStatement query)
        throws SQLException {
        try (ResultSet result = query.executeQuery()) {
            if (!result.next()) {
                return Optional.empty();
            }
            String transactionId = result.getString("transaction_id");
            return Optional.of(new RepairOperation(
                UUID.fromString(result.getString("repair_id")),
                result.getString("idempotency_key"),
                UUID.fromString(result.getString("player_uuid")),
                UUID.fromString(result.getString("tool_id")),
                result.getLong("instance_epoch"),
                result.getLong("amount_waymark"),
                RepairOperation.State.valueOf(result.getString("state")),
                transactionId == null ? null : UUID.fromString(transactionId),
                result.getString("refund_operation_id"),
                result.getString("failure_code"),
                result.getLong("lock_version")
            ));
        }
    }

    private static IllegalStateException unavailable() {
        return new IllegalStateException("Main repair repository operation failed");
    }
}
