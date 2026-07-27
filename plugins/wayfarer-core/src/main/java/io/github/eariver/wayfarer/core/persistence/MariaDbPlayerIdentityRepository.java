package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

final class MariaDbPlayerIdentityRepository {
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Pattern SERVER_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final String UPSERT = """
        INSERT INTO wf_core_player_identity (
            player_uuid, last_known_name, first_seen_at, last_seen_at,
            last_server_id, lock_version
        ) VALUES (?, ?, ?, ?, ?, 0)
        ON DUPLICATE KEY UPDATE
            lock_version = IF(VALUES(last_seen_at) > last_seen_at, lock_version + 1, lock_version),
            last_known_name = IF(
                VALUES(last_seen_at) > last_seen_at, VALUES(last_known_name), last_known_name
            ),
            last_server_id = IF(
                VALUES(last_seen_at) > last_seen_at, VALUES(last_server_id), last_server_id
            ),
            last_seen_at = GREATEST(last_seen_at, VALUES(last_seen_at))
        """;
    private static final String FIND = """
        SELECT player_uuid, last_known_name, first_seen_at, last_seen_at,
               last_server_id, lock_version
          FROM wf_core_player_identity
         WHERE player_uuid = ?
        """;
    private final InternalDatabase database;

    MariaDbPlayerIdentityRepository(InternalDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    CompletionStage<Void> upsert(PlayerIdentityObservation observation) {
        validate(observation);
        return database.transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(UPSERT)) {
                Timestamp observed = Timestamp.from(observation.observedAt());
                statement.setString(1, observation.playerUuid().toString());
                statement.setString(2, observation.lastKnownName());
                statement.setTimestamp(3, observed);
                statement.setTimestamp(4, observed);
                statement.setString(5, observation.serverId());
                statement.executeUpdate();
                return null;
            }
        });
    }

    CompletionStage<Optional<PlayerIdentityRecord>> find(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        return database.read(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(FIND)) {
                statement.setString(1, playerUuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new PlayerIdentityRecord(
                        UUID.fromString(result.getString("player_uuid")),
                        result.getString("last_known_name"),
                        result.getTimestamp("first_seen_at").toInstant(),
                        result.getTimestamp("last_seen_at").toInstant(),
                        result.getString("last_server_id"),
                        result.getLong("lock_version")
                    ));
                }
            }
        });
    }

    private static void validate(PlayerIdentityObservation observation) {
        Objects.requireNonNull(observation, "observation");
        Objects.requireNonNull(observation.playerUuid(), "observation.playerUuid");
        Objects.requireNonNull(observation.observedAt(), "observation.observedAt");
        if (observation.lastKnownName() == null
            || !NAME.matcher(observation.lastKnownName()).matches()) {
            throw new IllegalArgumentException("Player name is invalid");
        }
        if (observation.serverId() == null
            || !SERVER_ID.matcher(observation.serverId()).matches()) {
            throw new IllegalArgumentException("Server ID is invalid");
        }
    }
}
