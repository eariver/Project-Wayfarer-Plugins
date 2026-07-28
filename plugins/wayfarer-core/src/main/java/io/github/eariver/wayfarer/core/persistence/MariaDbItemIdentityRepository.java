package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerItemIdentity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

final class MariaDbItemIdentityRepository {
    private static final String INSERT = """
        INSERT INTO wf_core_item_identity (
            item_instance_id, item_type, owner_uuid, instance_epoch,
            schema_version, display_revision, created_at, updated_at, lock_version
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
        """;
    private static final String FIND = """
        SELECT item_instance_id, item_type, owner_uuid, instance_epoch,
               schema_version, display_revision, created_at, updated_at, lock_version
          FROM wf_core_item_identity
         WHERE item_instance_id = ?
        """;
    private final InternalDatabase database;

    MariaDbItemIdentityRepository(InternalDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    CompletionStage<InsertResult> create(WayfarerItemIdentity.Identity identity) {
        return database.transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                statement.setString(1, identity.itemInstanceId().toString());
                statement.setString(2, identity.itemType());
                statement.setString(3, identity.ownerUuid().toString());
                statement.setLong(4, identity.instanceEpoch());
                statement.setInt(5, identity.schemaVersion());
                statement.setInt(6, identity.displayRevision());
                statement.setTimestamp(7, Timestamp.from(identity.createdAt()));
                statement.setTimestamp(8, Timestamp.from(identity.updatedAt()));
                statement.executeUpdate();
                return InsertResult.INSERTED;
            } catch (SQLException failure) {
                if (failure.getErrorCode() == 1062 || "23000".equals(failure.getSQLState())) {
                    return InsertResult.CONFLICT;
                }
                throw failure;
            }
        });
    }

    CompletionStage<Optional<WayfarerItemIdentity.Identity>> find(UUID itemInstanceId) {
        Objects.requireNonNull(itemInstanceId, "itemInstanceId");
        return database.read(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(FIND)) {
                statement.setString(1, itemInstanceId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(map(result));
                }
            }
        });
    }

    private static WayfarerItemIdentity.Identity map(ResultSet result) throws SQLException {
        return new WayfarerItemIdentity.Identity(
            UUID.fromString(result.getString("item_instance_id")),
            result.getString("item_type"),
            UUID.fromString(result.getString("owner_uuid")),
            result.getLong("instance_epoch"),
            result.getInt("schema_version"),
            result.getInt("display_revision"),
            result.getTimestamp("created_at").toInstant(),
            result.getTimestamp("updated_at").toInstant(),
            result.getLong("lock_version")
        );
    }

    enum InsertResult {
        INSERTED,
        CONFLICT
    }
}
