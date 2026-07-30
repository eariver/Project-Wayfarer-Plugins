package io.github.eariver.wayfarer.frontier.persistence;

import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JdbcTraversalLoadoutRepository
    implements TraversalLoadoutRepository {
    private static final String THEME = TraversalIdentity.WORLDS_BEYOND;
    private final DataSource dataSource;
    private final FrontierModuleConfig.LoadoutDefinition definition;

    public JdbcTraversalLoadoutRepository(
        DataSource dataSource,
        FrontierModuleConfig.LoadoutDefinition definition
    ) {
        this.dataSource = java.util.Objects.requireNonNull(dataSource, "dataSource");
        this.definition = java.util.Objects.requireNonNull(definition, "definition");
    }

    @Override
    public TraversalLoadout findOrCreate(UUID playerUuid, Instant now) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO wf_frontier_theme_player_state "
                     + "(player_uuid,theme_id,first_joined_at,updated_at) "
                     + "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE "
                     + "first_joined_at=COALESCE(first_joined_at,VALUES(first_joined_at))"
             )) {
            insert.setString(1, playerUuid.toString());
            insert.setString(2, THEME);
            insert.setTimestamp(3, Timestamp.from(now));
            insert.setTimestamp(4, Timestamp.from(now));
            insert.executeUpdate();
            return load(connection, playerUuid);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public List<PendingDelivery> ensureInitialDeliveries(
        TraversalLoadout loadout,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (TraversalIdentity.ItemType type
                    : TraversalIdentity.ItemType.values()) {
                    ensurePermanent(connection, loadout.playerUuid(), type, now);
                    ensureDelivery(
                        connection,
                        loadout.playerUuid(),
                        deliveryType(type),
                        1,
                        "frontier-permanent:" + loadout.playerUuid() + ":" + type,
                        now
                    );
                }
                if (!loadout.initialLaunchpadsGranted()) {
                    ensureDelivery(
                        connection,
                        loadout.playerUuid(),
                        PendingDelivery.ItemType.LAUNCHPAD,
                        definition.initialLaunchpadAmount(),
                        "frontier-initial-launchpad:" + loadout.playerUuid(),
                        now
                    );
                    try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE wf_frontier_theme_player_state SET "
                            + "initial_launchpad_granted=TRUE,"
                            + "initial_launchpad_granted_at=?,lock_version=lock_version+1,"
                            + "updated_at=? WHERE player_uuid=? AND theme_id=? "
                            + "AND initial_launchpad_granted=FALSE"
                    )) {
                        update.setTimestamp(1, Timestamp.from(now));
                        update.setTimestamp(2, Timestamp.from(now));
                        update.setString(3, loadout.playerUuid().toString());
                        update.setString(4, THEME);
                        update.executeUpdate();
                    }
                }
                connection.commit();
                return pending(connection, loadout.playerUuid());
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean markDelivered(UUID deliveryId, Instant now) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_pending_delivery SET state='DELIVERED',"
                     + "attempts=attempts+1,last_error=NULL,updated_at=? "
                     + "WHERE delivery_id=? AND state='PENDING'"
             )) {
            update.setTimestamp(1, Timestamp.from(now));
            update.setString(2, deliveryId.toString());
            return update.executeUpdate() == 1;
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private static TraversalLoadout load(
        Connection connection,
        UUID playerUuid
    ) throws SQLException {
        Instant joined;
        boolean launchpads;
        long lockVersion;
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_frontier_theme_player_state "
                + "WHERE player_uuid=? AND theme_id=?"
        )) {
            query.setString(1, playerUuid.toString());
            query.setString(2, THEME);
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) {
                    throw unavailable();
                }
                Timestamp firstJoined = result.getTimestamp("first_joined_at");
                joined = firstJoined == null ? Instant.EPOCH : firstJoined.toInstant();
                launchpads = result.getBoolean("initial_launchpad_granted");
                lockVersion = result.getLong("lock_version");
            }
        }
        List<TraversalLoadout.LogicalItem> items = new ArrayList<>();
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_frontier_item_instance "
                + "WHERE player_uuid=? AND theme_id=?"
        )) {
            query.setString(1, playerUuid.toString());
            query.setString(2, THEME);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    TraversalIdentity.ItemType type =
                        TraversalIdentity.ItemType.valueOf(
                            result.getString("item_type")
                        );
                    long epoch = result.getLong("instance_epoch");
                    items.add(new TraversalLoadout.LogicalItem(
                        type,
                        stableItemId(playerUuid, type, epoch),
                        epoch,
                        TraversalLoadout.LogicalItem.State.valueOf(
                            result.getString("state")
                        )
                    ));
                }
            }
        }
        return new TraversalLoadout(
            playerUuid,
            THEME,
            joined,
            launchpads,
            items,
            lockVersion
        );
    }

    private static void ensurePermanent(
        Connection connection,
        UUID playerUuid,
        TraversalIdentity.ItemType type,
        Instant now
    ) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO wf_frontier_item_instance "
                + "(player_uuid,theme_id,item_type,instance_epoch,state,issued_at,"
                + "updated_at) VALUES (?,?,?,1,'ACTIVE',?,?) "
                + "ON DUPLICATE KEY UPDATE player_uuid=player_uuid"
        )) {
            insert.setString(1, playerUuid.toString());
            insert.setString(2, THEME);
            insert.setString(3, type.name());
            insert.setTimestamp(4, Timestamp.from(now));
            insert.setTimestamp(5, Timestamp.from(now));
            insert.executeUpdate();
        }
    }

    private static void ensureDelivery(
        Connection connection,
        UUID playerUuid,
        PendingDelivery.ItemType type,
        int quantity,
        String idempotencyKey,
        Instant now
    ) throws SQLException {
        UUID deliveryId = UUID.nameUUIDFromBytes(
            idempotencyKey.getBytes(StandardCharsets.UTF_8)
        );
        try (PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO wf_frontier_pending_delivery "
                + "(delivery_id,player_uuid,theme_id,item_type,quantity,"
                + "idempotency_key,state,attempts,created_at,updated_at) "
                + "VALUES (?,?,?,?,?,?,'PENDING',0,?,?) "
                + "ON DUPLICATE KEY UPDATE idempotency_key=idempotency_key"
        )) {
            insert.setString(1, deliveryId.toString());
            insert.setString(2, playerUuid.toString());
            insert.setString(3, THEME);
            insert.setString(4, type.name());
            insert.setInt(5, quantity);
            insert.setString(6, idempotencyKey);
            insert.setTimestamp(7, Timestamp.from(now));
            insert.setTimestamp(8, Timestamp.from(now));
            insert.executeUpdate();
        }
    }

    private static List<PendingDelivery> pending(
        Connection connection,
        UUID playerUuid
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_frontier_pending_delivery "
                + "WHERE player_uuid=? AND theme_id=? AND state='PENDING' "
                + "ORDER BY created_at,delivery_id"
        )) {
            query.setString(1, playerUuid.toString());
            query.setString(2, THEME);
            try (ResultSet result = query.executeQuery()) {
                List<PendingDelivery> values = new ArrayList<>();
                while (result.next()) {
                    values.add(new PendingDelivery(
                        UUID.fromString(result.getString("delivery_id")),
                        playerUuid,
                        THEME,
                        PendingDelivery.ItemType.valueOf(
                            result.getString("item_type")
                        ),
                        result.getInt("quantity"),
                        result.getString("idempotency_key"),
                        PendingDelivery.State.PENDING,
                        result.getInt("attempts"),
                        result.getTimestamp("created_at").toInstant()
                    ));
                }
                return List.copyOf(values);
            }
        }
    }

    private static PendingDelivery.ItemType deliveryType(
        TraversalIdentity.ItemType type
    ) {
        return switch (type) {
            case ELYTRA -> PendingDelivery.ItemType.ELYTRA;
            case GRAPPLING_HOOK -> PendingDelivery.ItemType.GRAPPLING_HOOK;
            case NAVIGATION -> PendingDelivery.ItemType.NAVIGATION;
        };
    }

    private static UUID stableItemId(
        UUID playerUuid,
        TraversalIdentity.ItemType type,
        long epoch
    ) {
        return UUID.nameUUIDFromBytes(
            (playerUuid + ":" + type + ":" + epoch)
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    private static IllegalStateException unavailable() {
        return new IllegalStateException("Frontier loadout repository operation failed");
    }
}
