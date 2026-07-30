package io.github.eariver.wayfarer.main.persistence;

import io.github.eariver.wayfarer.main.application.GrowthToolRepository;
import io.github.eariver.wayfarer.main.domain.GrowthTool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class JdbcGrowthToolRepository implements GrowthToolRepository {
    private final DataSource dataSource;

    public JdbcGrowthToolRepository(DataSource dataSource) {
        this.dataSource = java.util.Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public GrowthTool findOrCreate(UUID ownerUuid, Instant now) {
        UUID toolId = UUID.randomUUID();
        UUID itemInstanceId = UUID.randomUUID();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO wf_main_growth_tool "
                     + "(tool_id,current_item_instance_id,owner_uuid,tool_type,"
                     + "updated_at) VALUES (?,?,?,'PICKAXE',?) "
                     + "ON DUPLICATE KEY UPDATE owner_uuid=owner_uuid"
             )) {
            insert.setString(1, toolId.toString());
            insert.setString(2, itemInstanceId.toString());
            insert.setString(3, ownerUuid.toString());
            insert.setTimestamp(4, Timestamp.from(now));
            insert.executeUpdate();
            return findByOwner(connection, ownerUuid).orElseThrow();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<GrowthTool> findByOwner(UUID ownerUuid) {
        try (Connection connection = dataSource.getConnection()) {
            return findByOwner(connection, ownerUuid);
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean markDelivered(
        UUID toolId,
        long expectedLockVersion,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_growth_tool SET delivery_status='DELIVERED',"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE tool_id=? AND delivery_status='PENDING' AND lock_version=?"
             )) {
            update.setTimestamp(1, Timestamp.from(now));
            update.setString(2, toolId.toString());
            update.setLong(3, expectedLockVersion);
            return update.executeUpdate() == 1;
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<GrowthTool> checkpoint(
        GrowthTool tool,
        long expectedLockVersion,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_main_growth_tool SET cumulative_progress_units=?,"
                     + "active_branch=?,tool_status=?,delivery_status=?,stored_damage=?,"
                     + "schema_version=?,display_revision=?,"
                     + "lock_version=lock_version+1,last_checkpoint_at=?,updated_at=? "
                     + "WHERE tool_id=? AND instance_epoch=? AND lock_version=?"
             )) {
            update.setLong(1, tool.cumulativeProgressUnits());
            update.setString(2, tool.branch().name());
            update.setString(3, tool.status().name());
            update.setString(4, tool.deliveryStatus().name());
            update.setInt(5, tool.storedDamage());
            update.setInt(6, tool.schemaVersion());
            update.setLong(7, tool.displayRevision());
            update.setTimestamp(8, Timestamp.from(now));
            update.setTimestamp(9, Timestamp.from(now));
            update.setString(10, tool.toolId().toString());
            update.setLong(11, tool.instanceEpoch());
            update.setLong(12, expectedLockVersion);
            if (update.executeUpdate() != 1) {
                return Optional.empty();
            }
            return findByOwner(connection, tool.ownerUuid());
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<GrowthTool> replaceAuthority(
        GrowthTool tool,
        long expectedLockVersion,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                "UPDATE wf_main_growth_tool SET instance_epoch=?,"
                    + "current_item_instance_id=?,"
                    + "cumulative_progress_units=?,active_branch=?,tool_status=?,"
                     + "delivery_status=?,stored_damage=?,schema_version=?,"
                     + "display_revision=?,lock_version=lock_version+1,updated_at=? "
                     + "WHERE tool_id=? AND lock_version=?"
             )) {
            update.setLong(1, tool.instanceEpoch());
            update.setString(2, tool.itemInstanceId().toString());
            update.setLong(3, tool.cumulativeProgressUnits());
            update.setString(4, tool.branch().name());
            update.setString(5, tool.status().name());
            update.setString(6, tool.deliveryStatus().name());
            update.setInt(7, tool.storedDamage());
            update.setInt(8, tool.schemaVersion());
            update.setLong(9, tool.displayRevision());
            update.setTimestamp(10, Timestamp.from(now));
            update.setString(11, tool.toolId().toString());
            update.setLong(12, expectedLockVersion);
            if (update.executeUpdate() != 1) {
                return Optional.empty();
            }
            return findByOwner(connection, tool.ownerUuid());
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private static Optional<GrowthTool> findByOwner(
        Connection connection,
        UUID ownerUuid
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_main_growth_tool WHERE owner_uuid=? AND tool_type='PICKAXE'"
        )) {
            query.setString(1, ownerUuid.toString());
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(new GrowthTool(
                    UUID.fromString(result.getString("tool_id")),
                    UUID.fromString(
                        result.getString("current_item_instance_id")
                    ),
                    UUID.fromString(result.getString("owner_uuid")),
                    result.getLong("instance_epoch"),
                    result.getLong("cumulative_progress_units"),
                    GrowthTool.Branch.valueOf(result.getString("active_branch")),
                    GrowthTool.Status.valueOf(result.getString("tool_status")),
                    GrowthTool.DeliveryStatus.valueOf(
                        result.getString("delivery_status")
                    ),
                    result.getInt("stored_damage"),
                    result.getInt("schema_version"),
                    result.getLong("display_revision"),
                    result.getLong("lock_version"),
                    result.getTimestamp("updated_at").toInstant()
                ));
            }
        }
    }

    private static IllegalStateException unavailable() {
        return new IllegalStateException("Main growth repository operation failed");
    }
}
