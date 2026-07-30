package io.github.eariver.wayfarer.frontier.persistence;

import io.github.eariver.wayfarer.frontier.application.LaunchpadRepository;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;

import javax.sql.DataSource;
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

public final class JdbcLaunchpadRepository implements LaunchpadRepository {
    private final DataSource dataSource;

    public JdbcLaunchpadRepository(DataSource dataSource) {
        this.dataSource = java.util.Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public boolean create(Launchpad launchpad, Instant now) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO wf_frontier_launchpad "
                     + "(launchpad_id,world_id,x,y,z,yaw,placer_uuid,"
                     + "successful_use_count,max_uses_at_creation,created_at,"
                     + "last_used_at,expires_at,definition_id,state,schema_version,"
                     + "lock_version,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
             )) {
            insert.setString(1, launchpad.launchpadId().toString());
            insert.setString(2, launchpad.location().worldId());
            insert.setInt(3, launchpad.location().x());
            insert.setInt(4, launchpad.location().y());
            insert.setInt(5, launchpad.location().z());
            insert.setFloat(6, launchpad.yaw());
            insert.setString(7, launchpad.placerUuid().toString());
            insert.setInt(8, launchpad.successfulUseCount());
            insert.setInt(9, launchpad.maxUsesAtCreation());
            insert.setTimestamp(10, Timestamp.from(launchpad.createdAt()));
            insert.setTimestamp(11, nullable(launchpad.lastUsedAt()));
            insert.setTimestamp(12, Timestamp.from(launchpad.expiresAt()));
            insert.setString(13, launchpad.definitionId());
            insert.setString(14, launchpad.state().name());
            insert.setInt(15, launchpad.schemaVersion());
            insert.setLong(16, launchpad.lockVersion());
            insert.setTimestamp(17, Timestamp.from(now));
            return insert.executeUpdate() == 1;
        } catch (SQLException failure) {
            if ("23000".equals(failure.getSQLState())) {
                return false;
            }
            throw unavailable();
        }
    }

    @Override
    public Optional<Launchpad> findAt(Launchpad.Location location) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement query = connection.prepareStatement(
                 "SELECT * FROM wf_frontier_launchpad "
                     + "WHERE world_id=? AND x=? AND y=? AND z=?"
             )) {
            query.setString(1, location.worldId());
            query.setInt(2, location.x());
            query.setInt(3, location.y());
            query.setInt(4, location.z());
            try (ResultSet result = query.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public Optional<Launchpad> claimForUse(
        UUID launchpadId,
        Instant claimUntil,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_launchpad SET use_claim_token=?,use_claim_until=?,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE launchpad_id=? AND state='ACTIVE' AND expires_at>? "
                     + "AND (use_claim_until IS NULL OR use_claim_until<=?)"
             )) {
            update.setString(1, UUID.randomUUID().toString());
            update.setTimestamp(2, Timestamp.from(claimUntil));
            update.setTimestamp(3, Timestamp.from(now));
            update.setString(4, launchpadId.toString());
            update.setTimestamp(5, Timestamp.from(now));
            update.setTimestamp(6, Timestamp.from(now));
            return update.executeUpdate() == 1
                ? find(connection, launchpadId)
                : Optional.empty();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean releaseUseClaim(
        UUID launchpadId,
        long expectedLockVersion,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_launchpad SET use_claim_token=NULL,"
                     + "use_claim_until=NULL,lock_version=lock_version+1,updated_at=? "
                     + "WHERE launchpad_id=? AND lock_version=? "
                     + "AND use_claim_token IS NOT NULL"
             )) {
            update.setTimestamp(1, Timestamp.from(now));
            update.setString(2, launchpadId.toString());
            update.setLong(3, expectedLockVersion);
            return update.executeUpdate() == 1;
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean saveAfterUse(
        Launchpad launchpad,
        long expectedLockVersion,
        Instant now
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_launchpad SET successful_use_count=?,"
                     + "last_used_at=?,expires_at=?,state=?,use_claim_token=NULL,"
                     + "use_claim_until=NULL,lock_version=lock_version+1,updated_at=? "
                     + "WHERE launchpad_id=? AND lock_version=? "
                     + "AND use_claim_token IS NOT NULL"
             )) {
            update.setInt(1, launchpad.successfulUseCount());
            update.setTimestamp(2, nullable(launchpad.lastUsedAt()));
            update.setTimestamp(3, Timestamp.from(launchpad.expiresAt()));
            update.setString(4, launchpad.state().name());
            update.setTimestamp(5, Timestamp.from(now));
            update.setString(6, launchpad.launchpadId().toString());
            update.setLong(7, expectedLockVersion);
            return update.executeUpdate() == 1;
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public void markUnknown(UUID launchpadId, String failureCode, Instant now) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_launchpad SET state='UNKNOWN',failure_code=?,"
                     + "use_claim_token=NULL,use_claim_until=NULL,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE launchpad_id=? AND state='ACTIVE'"
             )) {
            update.setString(1, failureCode);
            update.setTimestamp(2, Timestamp.from(now));
            update.setString(3, launchpadId.toString());
            update.executeUpdate();
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public boolean remove(
        UUID launchpadId,
        long expectedLockVersion,
        Launchpad.State removalState,
        Instant now
    ) {
        if (removalState != Launchpad.State.PLAYER_BROKEN
            && removalState != Launchpad.State.ADMIN_REMOVED
            && removalState != Launchpad.State.RECONCILED_REMOVED) {
            throw new IllegalArgumentException("Invalid launchpad removal state");
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(
                 "UPDATE wf_frontier_launchpad SET state=?,"
                     + "use_claim_token=NULL,use_claim_until=NULL,"
                     + "lock_version=lock_version+1,updated_at=? "
                     + "WHERE launchpad_id=? AND state='ACTIVE' AND lock_version=?"
             )) {
            update.setString(1, removalState.name());
            update.setTimestamp(2, Timestamp.from(now));
            update.setString(3, launchpadId.toString());
            update.setLong(4, expectedLockVersion);
            return update.executeUpdate() == 1;
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    @Override
    public List<Launchpad> findExpirationCandidates(Instant now, int limit) {
        if (limit < 1 || limit > 1_000) {
            throw new IllegalArgumentException("Expiration query limit is invalid");
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement query = connection.prepareStatement(
                 "SELECT * FROM wf_frontier_launchpad "
                     + "WHERE state='ACTIVE' AND expires_at<=? "
                     + "ORDER BY expires_at LIMIT ?"
             )) {
            query.setTimestamp(1, Timestamp.from(now));
            query.setInt(2, limit);
            try (ResultSet result = query.executeQuery()) {
                List<Launchpad> values = new ArrayList<>();
                while (result.next()) {
                    values.add(map(result));
                }
                return List.copyOf(values);
            }
        } catch (SQLException failure) {
            throw unavailable();
        }
    }

    private static Optional<Launchpad> find(
        Connection connection,
        UUID launchpadId
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM wf_frontier_launchpad WHERE launchpad_id=?"
        )) {
            query.setString(1, launchpadId.toString());
            try (ResultSet result = query.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    private static Launchpad map(ResultSet result) throws SQLException {
        Timestamp lastUsed = result.getTimestamp("last_used_at");
        return new Launchpad(
            UUID.fromString(result.getString("launchpad_id")),
            new Launchpad.Location(
                result.getString("world_id"),
                result.getInt("x"),
                result.getInt("y"),
                result.getInt("z")
            ),
            result.getFloat("yaw"),
            UUID.fromString(result.getString("placer_uuid")),
            result.getInt("successful_use_count"),
            result.getInt("max_uses_at_creation"),
            result.getTimestamp("created_at").toInstant(),
            lastUsed == null ? null : lastUsed.toInstant(),
            result.getTimestamp("expires_at").toInstant(),
            result.getString("definition_id"),
            Launchpad.State.valueOf(result.getString("state")),
            result.getInt("schema_version"),
            result.getLong("lock_version")
        );
    }

    private static Timestamp nullable(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static IllegalStateException unavailable() {
        return new IllegalStateException("Frontier launchpad repository operation failed");
    }
}
