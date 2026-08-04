package io.github.eariver.wayfarer.frontier.persistence;

import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.domain.DeathIdentitySnapshot;
import io.github.eariver.wayfarer.frontier.domain.DeathPersistResult;
import io.github.eariver.wayfarer.frontier.domain.DeliveryCompletion;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
    public Optional<TraversalLoadout> find(UUID playerUuid) {
        try (Connection connection = dataSource.getConnection()) {
            return exists(connection, playerUuid)
                ? Optional.of(load(connection, playerUuid))
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
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
                    long epoch = currentEpoch(
                        connection,
                        loadout.playerUuid(),
                        type
                    );
                    TraversalIdentity identity = identity(
                        loadout.playerUuid(),
                        type,
                        epoch
                    );
                    ensureDelivery(
                        connection,
                        loadout.playerUuid(),
                        deliveryType(type),
                        1,
                        "frontier-permanent:" + loadout.playerUuid() + ":"
                            + type + ":" + epoch,
                        now,
                        identity
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
    public List<PendingDelivery> pending(UUID playerUuid) {
        try (Connection connection = dataSource.getConnection()) {
            return pending(connection, playerUuid);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean reissuePermanent(
        UUID playerUuid,
        TraversalIdentity.ItemType itemType,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!lockPlayer(connection, playerUuid)) {
                    connection.rollback();
                    return false;
                }
                try (PreparedStatement cancel = connection.prepareStatement(
                    "UPDATE wf_frontier_pending_delivery SET state='CANCELLED',"
                        + "updated_at=? WHERE player_uuid=? AND theme_id=? "
                        + "AND item_type=? AND state='PENDING'"
                )) {
                    cancel.setTimestamp(1, Timestamp.from(now));
                    cancel.setString(2, playerUuid.toString());
                    cancel.setString(3, THEME);
                    cancel.setString(4, deliveryType(itemType).name());
                    cancel.executeUpdate();
                }
                try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE wf_frontier_item_instance SET "
                        + "instance_epoch=instance_epoch+1,state='ACTIVE',"
                        + "invalidated_at=NULL,lock_version=lock_version+1,"
                        + "updated_at=? WHERE player_uuid=? AND theme_id=? "
                        + "AND item_type=?"
                )) {
                    update.setTimestamp(1, Timestamp.from(now));
                    update.setString(2, playerUuid.toString());
                    update.setString(3, THEME);
                    update.setString(4, itemType.name());
                    if (update.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                long epoch = currentEpoch(connection, playerUuid, itemType);
                ensureDelivery(
                    connection,
                    playerUuid,
                    deliveryType(itemType),
                    1,
                    "frontier-permanent:" + playerUuid + ":" + itemType
                        + ":" + epoch,
                    now,
                    identity(playerUuid, itemType, epoch)
                );
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
    public DeathPersistResult persistDeathSnapshot(
        DeathIdentitySnapshot snapshot,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!lockPlayer(connection, snapshot.playerUuid())) {
                    connection.commit();
                    return DeathPersistResult.NOT_FOUND_LOGICAL;
                }
                LogicalLock logical = lockLogical(
                    connection,
                    snapshot.playerUuid(),
                    snapshot.itemType()
                );
                if (logical == null
                    || logical.state != TraversalLoadout.LogicalItem.State.ACTIVE
                    || logical.epoch != snapshot.instanceEpoch()
                    || !stableItemId(
                        snapshot.playerUuid(),
                        snapshot.itemType(),
                        logical.epoch
                    ).equals(snapshot.itemInstanceId())
                    || !THEME.equals(snapshot.themeId())
                    || snapshot.schemaVersion() != 1) {
                    connection.commit();
                    return DeathPersistResult.STALE_SKIPPED;
                }
                String idempotency = permanentIdempotency(
                    snapshot.playerUuid(),
                    snapshot.itemType(),
                    snapshot.instanceEpoch()
                );
                UUID deliveryId = deliveryId(idempotency);
                DeliveryLock row = lockDeliveryById(connection, deliveryId);
                TraversalIdentity expected = identity(
                    snapshot.playerUuid(),
                    snapshot.itemType(),
                    snapshot.instanceEpoch()
                );
                DeathPersistResult result;
                if (row == null) {
                    insertPending(
                        connection,
                        deliveryId,
                        snapshot.playerUuid(),
                        deliveryType(snapshot.itemType()),
                        1,
                        idempotency,
                        now,
                        expected
                    );
                    result = DeathPersistResult.PENDING_CREATED;
                } else if ("PENDING".equals(row.state)) {
                    if (payloadMatches(row.payload, expected)) {
                        result = DeathPersistResult.ALREADY_PENDING;
                    } else {
                        result = DeathPersistResult.CONFLICT;
                    }
                } else if ("DELIVERED".equals(row.state)) {
                    if (!payloadMatches(row.payload, expected)) {
                        result = DeathPersistResult.CONFLICT;
                    } else {
                        try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE wf_frontier_pending_delivery SET state='PENDING',"
                                + "last_error=NULL,updated_at=? "
                                + "WHERE delivery_id=? AND state='DELIVERED'"
                        )) {
                            update.setTimestamp(1, Timestamp.from(now));
                            update.setString(2, deliveryId.toString());
                            if (update.executeUpdate() != 1) {
                                result = DeathPersistResult.CONFLICT;
                            } else {
                                result = DeathPersistResult.REOPENED_TO_PENDING;
                            }
                        }
                    }
                } else if ("CANCELLED".equals(row.state)) {
                    result = DeathPersistResult.CANCELLED_OR_OBSOLETE;
                } else {
                    result = DeathPersistResult.CONFLICT;
                }
                connection.commit();
                return result;
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public void reopenAbsentPermanents(
        UUID playerUuid,
        EnumSet<TraversalIdentity.ItemType> absentTypes,
        Instant now
    ) {
        if (absentTypes == null || absentTypes.isEmpty()) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!lockPlayer(connection, playerUuid)) {
                    connection.commit();
                    return;
                }
                for (TraversalIdentity.ItemType type : absentTypes) {
                    LogicalLock logical = lockLogical(connection, playerUuid, type);
                    if (logical == null
                        || logical.state != TraversalLoadout.LogicalItem.State.ACTIVE) {
                        continue;
                    }
                    String idempotency = permanentIdempotency(
                        playerUuid,
                        type,
                        logical.epoch
                    );
                    UUID deliveryId = deliveryId(idempotency);
                    DeliveryLock row = lockDeliveryById(connection, deliveryId);
                    TraversalIdentity expected = identity(
                        playerUuid,
                        type,
                        logical.epoch
                    );
                    if (row == null) {
                        insertPending(
                            connection,
                            deliveryId,
                            playerUuid,
                            deliveryType(type),
                            1,
                            idempotency,
                            now,
                            expected
                        );
                    } else if ("DELIVERED".equals(row.state)
                        && payloadMatches(row.payload, expected)) {
                        try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE wf_frontier_pending_delivery SET state='PENDING',"
                                + "last_error=NULL,updated_at=? "
                                + "WHERE delivery_id=? AND state='DELIVERED'"
                        )) {
                            update.setTimestamp(1, Timestamp.from(now));
                            update.setString(2, deliveryId.toString());
                            update.executeUpdate();
                        }
                    }
                }
                connection.commit();
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public DeliveryCompletion markPermanentDelivered(UUID deliveryId, Instant now) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                DeliveryLock row = lockDeliveryById(connection, deliveryId);
                if (row == null) {
                    connection.commit();
                    return DeliveryCompletion.NOT_FOUND;
                }
                if ("DELIVERED".equals(row.state)) {
                    connection.commit();
                    return DeliveryCompletion.ALREADY_DELIVERED;
                }
                if ("CANCELLED".equals(row.state)) {
                    connection.commit();
                    return DeliveryCompletion.CANCELLED;
                }
                if (!"PENDING".equals(row.state)) {
                    connection.commit();
                    return DeliveryCompletion.TRUE_CONFLICT;
                }
                PendingDelivery.ItemType deliveryType;
                try {
                    deliveryType = PendingDelivery.ItemType.valueOf(row.itemType);
                } catch (RuntimeException failure) {
                    connection.commit();
                    return DeliveryCompletion.TRUE_CONFLICT;
                }
                if (!isPermanent(deliveryType)) {
                    connection.commit();
                    return DeliveryCompletion.TRUE_CONFLICT;
                }
                ParsedPayload payload = parsePayloadSoft(row.payload);
                if (payload == null) {
                    connection.commit();
                    return DeliveryCompletion.MALFORMED_PAYLOAD;
                }
                TraversalIdentity.ItemType logicalType = logicalType(deliveryType);
                LogicalLock logical = lockLogical(
                    connection,
                    row.playerUuid,
                    logicalType
                );
                if (logical == null
                    || logical.state != TraversalLoadout.LogicalItem.State.ACTIVE
                    || logical.epoch != payload.epoch
                    || !stableItemId(row.playerUuid, logicalType, logical.epoch)
                        .equals(payload.itemInstanceId)
                    || payload.schemaVersion != 1) {
                    connection.commit();
                    return DeliveryCompletion.STALE_IDENTITY;
                }
                try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE wf_frontier_pending_delivery SET state='DELIVERED',"
                        + "attempts=attempts+1,last_error=NULL,updated_at=? "
                        + "WHERE delivery_id=? AND state='PENDING'"
                )) {
                    update.setTimestamp(1, Timestamp.from(now));
                    update.setString(2, deliveryId.toString());
                    int updated = update.executeUpdate();
                    connection.commit();
                    return updated == 1
                        ? DeliveryCompletion.TRANSITIONED_TO_DELIVERED
                        : DeliveryCompletion.TRUE_CONFLICT;
                }
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public DeliveryCompletion markConsumableDelivered(UUID deliveryId, Instant now) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                DeliveryLock row = lockDeliveryById(connection, deliveryId);
                if (row == null) {
                    connection.commit();
                    return DeliveryCompletion.NOT_FOUND;
                }
                if ("DELIVERED".equals(row.state)) {
                    connection.commit();
                    return DeliveryCompletion.ALREADY_DELIVERED;
                }
                if ("CANCELLED".equals(row.state)) {
                    connection.commit();
                    return DeliveryCompletion.CANCELLED;
                }
                if (!"LAUNCHPAD".equals(row.itemType)
                    && !"FIREWORK_ROCKET".equals(row.itemType)) {
                    connection.commit();
                    return DeliveryCompletion.TRUE_CONFLICT;
                }
                if (!"PENDING".equals(row.state)) {
                    connection.commit();
                    return DeliveryCompletion.TRUE_CONFLICT;
                }
                try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE wf_frontier_pending_delivery SET state='DELIVERED',"
                        + "attempts=attempts+1,last_error=NULL,updated_at=? "
                        + "WHERE delivery_id=? AND state='PENDING'"
                )) {
                    update.setTimestamp(1, Timestamp.from(now));
                    update.setString(2, deliveryId.toString());
                    int updated = update.executeUpdate();
                    connection.commit();
                    return updated == 1
                        ? DeliveryCompletion.TRANSITIONED_TO_DELIVERED
                        : DeliveryCompletion.TRUE_CONFLICT;
                }
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private static boolean exists(
        Connection connection,
        UUID playerUuid
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT 1 FROM wf_frontier_theme_player_state "
                + "WHERE player_uuid=? AND theme_id=?"
        )) {
            query.setString(1, playerUuid.toString());
            query.setString(2, THEME);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }

    private static boolean lockPlayer(
        Connection connection,
        UUID playerUuid
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT lock_version FROM wf_frontier_theme_player_state "
                + "WHERE player_uuid=? AND theme_id=? FOR UPDATE"
        )) {
            query.setString(1, playerUuid.toString());
            query.setString(2, THEME);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
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
        ensureDelivery(
            connection,
            playerUuid,
            type,
            quantity,
            idempotencyKey,
            now,
            null
        );
    }

    private static void ensureDelivery(
        Connection connection,
        UUID playerUuid,
        PendingDelivery.ItemType type,
        int quantity,
        String idempotencyKey,
        Instant now,
        TraversalIdentity identity
    ) throws SQLException {
        insertPending(
            connection,
            deliveryId(idempotencyKey),
            playerUuid,
            type,
            quantity,
            idempotencyKey,
            now,
            identity
        );
    }

    private static void insertPending(
        Connection connection,
        UUID deliveryId,
        UUID playerUuid,
        PendingDelivery.ItemType type,
        int quantity,
        String idempotencyKey,
        Instant now,
        TraversalIdentity identity
    ) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO wf_frontier_pending_delivery "
                + "(delivery_id,player_uuid,theme_id,item_type,quantity,"
                + "idempotency_key,payload_json,state,attempts,created_at,updated_at) "
                + "VALUES (?,?,?,?,?,?,?,'PENDING',0,?,?) "
                + "ON DUPLICATE KEY UPDATE idempotency_key=idempotency_key"
        )) {
            insert.setString(1, deliveryId.toString());
            insert.setString(2, playerUuid.toString());
            insert.setString(3, THEME);
            insert.setString(4, type.name());
            insert.setInt(5, quantity);
            insert.setString(6, idempotencyKey);
            insert.setString(7, payload(identity));
            insert.setTimestamp(8, Timestamp.from(now));
            insert.setTimestamp(9, Timestamp.from(now));
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
                        result.getTimestamp("created_at").toInstant(),
                        parseIdentitySoft(
                            result.getString("payload_json"),
                            playerUuid,
                            PendingDelivery.ItemType.valueOf(
                                result.getString("item_type")
                            )
                        )
                    ));
                }
                return List.copyOf(values);
            }
        }
    }

    private static LogicalLock lockLogical(
        Connection connection,
        UUID playerUuid,
        TraversalIdentity.ItemType type
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT instance_epoch,state FROM wf_frontier_item_instance "
                + "WHERE player_uuid=? AND theme_id=? AND item_type=? FOR UPDATE"
        )) {
            query.setString(1, playerUuid.toString());
            query.setString(2, THEME);
            query.setString(3, type.name());
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new LogicalLock(
                    result.getLong("instance_epoch"),
                    TraversalLoadout.LogicalItem.State.valueOf(
                        result.getString("state")
                    )
                );
            }
        }
    }

    private static DeliveryLock lockDeliveryById(
        Connection connection,
        UUID deliveryId
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT delivery_id,player_uuid,item_type,state,payload_json "
                + "FROM wf_frontier_pending_delivery WHERE delivery_id=? FOR UPDATE"
        )) {
            query.setString(1, deliveryId.toString());
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new DeliveryLock(
                    UUID.fromString(result.getString("delivery_id")),
                    UUID.fromString(result.getString("player_uuid")),
                    result.getString("item_type"),
                    result.getString("state"),
                    result.getString("payload_json")
                );
            }
        }
    }

    private static String permanentIdempotency(
        UUID playerUuid,
        TraversalIdentity.ItemType type,
        long epoch
    ) {
        return "frontier-permanent:" + playerUuid + ":" + type + ":" + epoch;
    }

    private static UUID deliveryId(String idempotencyKey) {
        return UUID.nameUUIDFromBytes(
            idempotencyKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static boolean payloadMatches(
        String payload,
        TraversalIdentity expected
    ) {
        ParsedPayload parsed = parsePayloadSoft(payload);
        return parsed != null
            && parsed.itemInstanceId.equals(expected.itemInstanceId())
            && parsed.epoch == expected.instanceEpoch()
            && parsed.schemaVersion == expected.schemaVersion();
    }

    private static boolean isPermanent(PendingDelivery.ItemType type) {
        return switch (type) {
            case ELYTRA, GRAPPLING_HOOK, NAVIGATION -> true;
            case LAUNCHPAD, FIREWORK_ROCKET -> false;
        };
    }

    private static TraversalIdentity.ItemType logicalType(
        PendingDelivery.ItemType type
    ) {
        return switch (type) {
            case ELYTRA -> TraversalIdentity.ItemType.ELYTRA;
            case GRAPPLING_HOOK -> TraversalIdentity.ItemType.GRAPPLING_HOOK;
            case NAVIGATION -> TraversalIdentity.ItemType.NAVIGATION;
            default -> throw new IllegalArgumentException(
                "Not a permanent delivery type"
            );
        };
    }

    private static final class LogicalLock {
        private final long epoch;
        private final TraversalLoadout.LogicalItem.State state;

        private LogicalLock(long epoch, TraversalLoadout.LogicalItem.State state) {
            this.epoch = epoch;
            this.state = state;
        }
    }

    private static final class DeliveryLock {
        private final UUID deliveryId;
        private final UUID playerUuid;
        private final String itemType;
        private final String state;
        private final String payload;

        private DeliveryLock(
            UUID deliveryId,
            UUID playerUuid,
            String itemType,
            String state,
            String payload
        ) {
            this.deliveryId = deliveryId;
            this.playerUuid = playerUuid;
            this.itemType = itemType;
            this.state = state;
            this.payload = payload;
        }
    }

    private static final class ParsedPayload {
        private final UUID itemInstanceId;
        private final long epoch;
        private final int schemaVersion;

        private ParsedPayload(UUID itemInstanceId, long epoch, int schemaVersion) {
            this.itemInstanceId = itemInstanceId;
            this.epoch = epoch;
            this.schemaVersion = schemaVersion;
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

    private static long currentEpoch(
        Connection connection,
        UUID playerUuid,
        TraversalIdentity.ItemType type
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT instance_epoch FROM wf_frontier_item_instance "
                + "WHERE player_uuid=? AND theme_id=? AND item_type=?"
        )) {
            query.setString(1, playerUuid.toString());
            query.setString(2, THEME);
            query.setString(3, type.name());
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) {
                    throw unavailable();
                }
                return result.getLong(1);
            }
        }
    }

    private static TraversalIdentity identity(
        UUID playerUuid,
        TraversalIdentity.ItemType type,
        long epoch
    ) {
        return new TraversalIdentity(
            stableItemId(playerUuid, type, epoch),
            type,
            playerUuid,
            THEME,
            epoch,
            1
        );
    }

    private static String payload(TraversalIdentity identity) {
        if (identity == null) {
            return null;
        }
        return "{\"item_instance_id\":\"" + identity.itemInstanceId()
            + "\",\"instance_epoch\":" + identity.instanceEpoch()
            + ",\"schema_version\":" + identity.schemaVersion() + "}";
    }

    private static TraversalIdentity parseIdentitySoft(
        String payload,
        UUID playerUuid,
        PendingDelivery.ItemType deliveryType
    ) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        if (deliveryType == PendingDelivery.ItemType.LAUNCHPAD
            || deliveryType == PendingDelivery.ItemType.FIREWORK_ROCKET) {
            return null;
        }
        ParsedPayload parsed = parsePayloadSoft(payload);
        if (parsed == null) {
            return null;
        }
        try {
            TraversalIdentity.ItemType type = logicalType(deliveryType);
            return new TraversalIdentity(
                parsed.itemInstanceId,
                type,
                playerUuid,
                THEME,
                parsed.epoch,
                parsed.schemaVersion
            );
        } catch (RuntimeException failure) {
            return null;
        }
    }

    private static ParsedPayload parsePayloadSoft(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            String id = jsonString(payload, "item_instance_id");
            long epoch = jsonLong(payload, "instance_epoch");
            int schema = Math.toIntExact(jsonLong(payload, "schema_version"));
            if (epoch < 1 || schema < 1) {
                return null;
            }
            return new ParsedPayload(UUID.fromString(id), epoch, schema);
        } catch (RuntimeException failure) {
            return null;
        }
    }

    private static String jsonString(String json, String key) {
        String prefix = "\"" + key + "\":\"";
        int start = json.indexOf(prefix);
        if (start < 0) {
            throw new IllegalArgumentException("Missing delivery identity");
        }
        start += prefix.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Invalid delivery identity");
        }
        return json.substring(start, end);
    }

    private static long jsonLong(String json, String key) {
        String prefix = "\"" + key + "\":";
        int start = json.indexOf(prefix);
        if (start < 0) {
            throw new IllegalArgumentException("Missing delivery identity");
        }
        start += prefix.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }

    private static IllegalStateException unavailable() {
        return new IllegalStateException("Frontier loadout repository operation failed");
    }
}
