package io.github.eariver.wayfarer.core.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

final class MariaDbAuditRepository {
    private static final String INSERT = """
        INSERT INTO wf_core_audit (
            event_id, correlation_id, event_type, actor_uuid, subject_type,
            subject_id, server_id, details_json, occurred_at
        ) VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String FIND = """
        SELECT event_id, event_type, actor_uuid, subject_type, subject_id,
               server_id, details_json, occurred_at
          FROM wf_core_audit
         WHERE event_id = ?
        """;
    private final InternalDatabase database;

    MariaDbAuditRepository(InternalDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    CompletionStage<Void> probe() {
        return database.read(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT event_id FROM wf_core_audit WHERE 1 = 0"
            )) {
                statement.executeQuery().close();
                return null;
            }
        });
    }

    CompletionStage<WriteResult> record(AuditRecord event) {
        return database.transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                bind(statement, event);
                statement.executeUpdate();
                return WriteResult.INSERTED;
            } catch (SQLException failure) {
                if (!duplicate(failure)) {
                    throw failure;
                }
                AuditRecord existing = find(connection, event);
                return event.equals(existing) ? WriteResult.EXACT_DUPLICATE : WriteResult.CONFLICT;
            }
        });
    }

    private static AuditRecord find(
        java.sql.Connection connection,
        AuditRecord requested
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND)) {
            statement.setString(1, requested.eventId().toString());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Duplicate audit row disappeared");
                }
                String actor = result.getString("actor_uuid");
                return new AuditRecord(
                    java.util.UUID.fromString(result.getString("event_id")),
                    result.getString("event_type"),
                    actor == null ? null : java.util.UUID.fromString(actor),
                    result.getString("subject_type"),
                    result.getString("subject_id"),
                    result.getString("server_id"),
                    result.getString("details_json"),
                    result.getTimestamp("occurred_at").toInstant()
                );
            }
        }
    }

    private static void bind(PreparedStatement statement, AuditRecord event) throws SQLException {
        statement.setString(1, event.eventId().toString());
        statement.setString(2, event.eventType());
        statement.setString(3, event.actorUuid() == null ? null : event.actorUuid().toString());
        statement.setString(4, event.subjectType());
        statement.setString(5, event.subjectId());
        statement.setString(6, event.serverId());
        statement.setString(7, event.detailsJson());
        statement.setTimestamp(8, Timestamp.from(event.occurredAt()));
    }

    private static boolean duplicate(SQLException failure) {
        return failure.getErrorCode() == 1062 || "23000".equals(failure.getSQLState());
    }

    enum WriteResult {
        INSERTED,
        EXACT_DUPLICATE,
        CONFLICT
    }
}
