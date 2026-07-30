package io.github.eariver.wayfarer.frontier.persistence;

import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.domain.FrontierShopCatalog;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class JdbcFrontierPurchaseRepository
    implements FrontierPurchaseRepository {
    private final DataSource dataSource;

    public JdbcFrontierPurchaseRepository(DataSource dataSource) {
        this.dataSource = java.util.Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public Purchase prepare(
        String idempotencyKey,
        UUID playerUuid,
        FrontierShopCatalog.Offer offer,
        Instant now
    ) {
        UUID purchaseId = UUID.randomUUID();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO wf_frontier_purchase "
                     + "(purchase_id,idempotency_key,player_uuid,theme_id,offer_id,"
                     + "item_type,quantity,price_waymark,state,created_at,updated_at) "
                     + "VALUES (?,?,?,'worlds_beyond',?,?,?,?, 'PREPARED',?,?) "
                     + "ON DUPLICATE KEY UPDATE idempotency_key=idempotency_key"
             )) {
            insert.setString(1, purchaseId.toString());
            insert.setString(2, idempotencyKey);
            insert.setString(3, playerUuid.toString());
            insert.setString(4, offer.offerId());
            insert.setString(5, offer.itemType().name());
            insert.setInt(6, offer.quantity());
            insert.setLong(7, offer.priceWaymark());
            insert.setTimestamp(8, Timestamp.from(now));
            insert.setTimestamp(9, Timestamp.from(now));
            insert.executeUpdate();
            return findByIdempotency(connection, idempotencyKey).orElseThrow();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<Purchase> claimPayment(
        UUID purchaseId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            purchaseId,
            expectedLockVersion,
            "PREPARED",
            "PAYMENT_PENDING",
            null,
            null,
            null,
            now
        );
    }

    @Override
    public boolean markPaymentCommitted(
        UUID purchaseId,
        UUID transactionId,
        long expectedLockVersion,
        Instant now
    ) {
        return transition(
            purchaseId,
            expectedLockVersion,
            "PAYMENT_PENDING",
            "PAYMENT_COMMITTED",
            transactionId,
            null,
            null,
            now
        ).isPresent();
    }

    @Override
    public Optional<Purchase> attachPendingDelivery(
        UUID purchaseId,
        long expectedLockVersion,
        PendingDelivery delivery,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO wf_frontier_pending_delivery "
                    + "(delivery_id,player_uuid,theme_id,item_type,quantity,"
                    + "idempotency_key,state,attempts,created_at,updated_at) "
                    + "VALUES (?,?,?,?,?,?,'PENDING',0,?,?) "
                    + "ON DUPLICATE KEY UPDATE idempotency_key=idempotency_key"
            ); PreparedStatement update = connection.prepareStatement(
                "UPDATE wf_frontier_purchase SET state='PENDING_DELIVERY',"
                    + "delivery_id=?,lock_version=lock_version+1,updated_at=? "
                    + "WHERE purchase_id=? AND state='PAYMENT_COMMITTED' "
                    + "AND lock_version=?"
            )) {
                insert.setString(1, delivery.deliveryId().toString());
                insert.setString(2, delivery.playerUuid().toString());
                insert.setString(3, delivery.themeId());
                insert.setString(4, delivery.itemType().name());
                insert.setInt(5, delivery.quantity());
                insert.setString(6, delivery.idempotencyKey());
                insert.setTimestamp(7, Timestamp.from(delivery.createdAt()));
                insert.setTimestamp(8, Timestamp.from(now));
                insert.executeUpdate();
                update.setString(1, delivery.deliveryId().toString());
                update.setTimestamp(2, Timestamp.from(now));
                update.setString(3, purchaseId.toString());
                update.setLong(4, expectedLockVersion);
                if (update.executeUpdate() != 1) {
                    connection.rollback();
                    return Optional.empty();
                }
                connection.commit();
                return find(connection, purchaseId);
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean markDelivered(
        UUID purchaseId,
        UUID deliveryId,
        long expectedLockVersion,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement delivery = connection.prepareStatement(
                "UPDATE wf_frontier_pending_delivery SET state='DELIVERED',"
                    + "attempts=attempts+1,updated_at=? "
                    + "WHERE delivery_id=? AND state='PENDING'"
            ); PreparedStatement purchase = connection.prepareStatement(
                "UPDATE wf_frontier_purchase SET state='DELIVERED',"
                    + "lock_version=lock_version+1,updated_at=? "
                    + "WHERE purchase_id=? AND delivery_id=? "
                    + "AND state='PENDING_DELIVERY' AND lock_version=?"
            )) {
                delivery.setTimestamp(1, Timestamp.from(now));
                delivery.setString(2, deliveryId.toString());
                if (delivery.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                purchase.setTimestamp(1, Timestamp.from(now));
                purchase.setString(2, purchaseId.toString());
                purchase.setString(3, deliveryId.toString());
                purchase.setLong(4, expectedLockVersion);
                if (purchase.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
                return true;
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean markFailed(
        UUID purchaseId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    ) {
        return transition(
            purchaseId,
            expectedLockVersion,
            "PAYMENT_PENDING",
            "FAILED",
            null,
            null,
            failureCode,
            now
        ).isPresent();
    }

    @Override
    public void markUnknown(
        UUID purchaseId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_purchase SET state='UNKNOWN',failure_code=?,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE purchase_id=? AND lock_version=? "
                     + "AND state NOT IN ('DELIVERED','FAILED')"
             )) {
            update.setString(1, failureCode);
            update.setTimestamp(2, Timestamp.from(now));
            update.setString(3, purchaseId.toString());
            update.setLong(4, expectedLockVersion);
            update.executeUpdate();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<Purchase> find(UUID purchaseId) {
        try (Connection connection = dataSource.getConnection()) {
            return find(connection, purchaseId);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private Optional<Purchase> transition(
        UUID purchaseId,
        long expectedLockVersion,
        String expectedState,
        String nextState,
        UUID transactionId,
        UUID deliveryId,
        String failureCode,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_purchase SET state=?,"
                     + "transaction_id=COALESCE(?,transaction_id),"
                     + "delivery_id=COALESCE(?,delivery_id),failure_code=?,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE purchase_id=? AND state=? AND lock_version=?"
             )) {
            update.setString(1, nextState);
            update.setString(2, transactionId == null ? null : transactionId.toString());
            update.setString(3, deliveryId == null ? null : deliveryId.toString());
            update.setString(4, failureCode);
            update.setTimestamp(5, Timestamp.from(now));
            update.setString(6, purchaseId.toString());
            update.setString(7, expectedState);
            update.setLong(8, expectedLockVersion);
            return update.executeUpdate() == 1
                ? find(connection, purchaseId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private static Optional<Purchase> findByIdempotency(
        Connection connection,
        String idempotencyKey
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_frontier_purchase WHERE idempotency_key=?"
        )) {
            query.setString(1, idempotencyKey);
            return one(query);
        }
    }

    private static Optional<Purchase> find(
        Connection connection,
        UUID purchaseId
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_frontier_purchase WHERE purchase_id=?"
        )) {
            query.setString(1, purchaseId.toString());
            return one(query);
        }
    }

    private static Optional<Purchase> one(PreparedStatement query)
        throws SQLException {
        try (ResultSet result = query.executeQuery()) {
            if (!result.next()) {
                return Optional.empty();
            }
            String transactionId = result.getString("transaction_id");
            String deliveryId = result.getString("delivery_id");
            PendingDelivery.ItemType itemType = PendingDelivery.ItemType.valueOf(
                result.getString("item_type")
            );
            return Optional.of(new Purchase(
                UUID.fromString(result.getString("purchase_id")),
                result.getString("idempotency_key"),
                UUID.fromString(result.getString("player_uuid")),
                new FrontierShopCatalog.Offer(
                    result.getString("offer_id"),
                    itemType,
                    result.getInt("quantity"),
                    result.getLong("price_waymark")
                ),
                State.valueOf(result.getString("state")),
                transactionId == null ? null : UUID.fromString(transactionId),
                deliveryId == null ? null : UUID.fromString(deliveryId),
                result.getLong("lock_version")
            ));
        }
    }

    private static IllegalStateException unavailable() {
        return new IllegalStateException("Frontier purchase repository operation failed");
    }
}
