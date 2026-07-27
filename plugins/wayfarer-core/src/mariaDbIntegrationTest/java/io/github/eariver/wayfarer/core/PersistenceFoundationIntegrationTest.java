package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleException;
import io.github.eariver.wayfarer.core.persistence.MariaDbPool;
import io.github.eariver.wayfarer.core.persistence.MigrationLifecycle;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import io.github.eariver.wayfarer.testkit.MariaDbContainerFixture;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceFoundationIntegrationTest {
    private static final String V001_SHA256 =
        "59035d3bf0ee9f11e2a6756138fa55f331dc79546778c473bacbde887a894840";

    @Test
    void migratesEmptyDatabaseAndRepeatedlyValidatesCoreOwnership() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start();
             CoreConfig config = enabledConfig(fixture, List.of("db/migration/core"));
             MariaDbPool pool = MariaDbPool.open(config.serverId(), config.mariadb());
             MigrationLifecycle first = MigrationLifecycle.migrate(
                 pool,
                 config.migration().locations()
             );
             MigrationLifecycle second = MigrationLifecycle.migrate(
                 pool,
                 config.migration().locations()
             );
             Connection connection = DriverManager.getConnection(
                 fixture.jdbcUrl(),
                 fixture.username(),
                 fixture.password()
             )) {
            assertEquals(2, first.appliedMigrationCount());
            assertEquals(2, second.appliedMigrationCount());
            assertEquals(
                Set.of(
                    "flyway_schema_history",
                    "wf_core_audit",
                    "wf_core_transaction",
                    "wf_core_player_identity",
                    "wf_core_item_identity"
                ),
                tableNames(connection)
            );
            assertEquals(2, appliedMigrationRows(connection));
            assertRequiredIndexes(connection);
            assertTimestampPrecision(connection);
            assertCoreConstraints(connection);
            assertIdentitySchema(connection);
        }
    }

    @Test
    void coreRuntimePublishesOnlyAfterPersistenceAndClosesInReverseOrder() {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start();
             CoreConfig config = enabledConfig(fixture, List.of("db/migration/core"))) {
            RecordingPublisher publisher = new RecordingPublisher();
            CoreRuntime runtime = runtime(config, publisher);
            runtime.enable();
            assertEquals(1, publisher.publishCount);
            assertEquals(
                WayfarerHealth.Status.UP,
                runtime.health().snapshot().components().get("MariaDB").status()
            );
            assertEquals(
                WayfarerHealth.Status.UP,
                runtime.health().snapshot().components().get("Migration").status()
            );

            runtime.disable();

            assertEquals(1, publisher.unpublishCount);
            assertTrue(runtime.isMariaDbPoolClosed());
            assertEquals(
                WayfarerHealth.Status.DISABLED,
                runtime.health().snapshot().components().get("Migration").status()
            );
            assertEquals(
                WayfarerHealth.Status.DISABLED,
                runtime.health().snapshot().components().get("MariaDB").status()
            );
            runtime.disable();
            assertEquals(1, publisher.unpublishCount);
        }
    }

    @Test
    void migrationFailureFailsClosedWithoutPublishingServicesOrLeakingSecret() {
        String password;
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            password = fixture.password();
            try (CoreConfig config = enabledConfig(
                fixture,
                List.of("db/migration/core", "db/migration/failure")
            )) {
                RecordingPublisher publisher = new RecordingPublisher();
                List<String> warnings = new ArrayList<>();
                CoreRuntime runtime = runtime(config, publisher, warnings::add);
                LifecycleException failure = assertThrows(
                    LifecycleException.class,
                    runtime::enable
                );
                assertEquals("Initialization failed at step Migration", failure.getMessage());
                assertFalse(failure.getMessage().contains(password));
                assertEquals(0, publisher.publishCount);
                assertTrue(runtime.isMariaDbPoolClosed());
                assertEquals(
                    WayfarerHealth.Status.DOWN,
                    runtime.health().snapshot().components().get("Migration").status()
                );
                String diagnostics = failure.getMessage()
                    + runtime.health().snapshot().components().values()
                    + warnings;
                assertFalse(diagnostics.contains(password));
                runtime.disable();
            }
        }
    }

    @Test
    void databaseOutageAtEnableFailsClosedWithoutPublishingServices() {
        MariaDbContainerFixture fixture = MariaDbContainerFixture.start();
        CoreConfig config = enabledConfig(fixture, List.of("db/migration/core"));
        String password = fixture.password();
        fixture.close();
        try (config) {
            RecordingPublisher publisher = new RecordingPublisher();
            List<String> warnings = new ArrayList<>();
            CoreRuntime runtime = runtime(config, publisher, warnings::add);
            LifecycleException failure = assertThrows(
                LifecycleException.class,
                runtime::enable
            );
            assertEquals("Initialization failed at step MariaDB", failure.getMessage());
            assertFalse(failure.getMessage().contains(password));
            assertFalse(
                (runtime.health().snapshot().components().values() + warnings.toString())
                    .contains(password)
            );
            assertEquals(0, publisher.publishCount);
            assertTrue(runtime.isMariaDbPoolClosed());
            runtime.disable();
        }
    }

    @Test
    void releasedMigrationBytesRemainImmutable() throws Exception {
        try (InputStream migration = getClass().getResourceAsStream(
            "/db/migration/core/V001__core_schema.sql"
        )) {
            assertNotNull(migration);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            assertEquals(
                V001_SHA256,
                HexFormat.of().formatHex(digest.digest(migration.readAllBytes()))
            );
        }
    }

    private static CoreRuntime runtime(CoreConfig config, RecordingPublisher publisher) {
        return runtime(config, publisher, ignored -> {});
    }

    private static CoreRuntime runtime(
        CoreConfig config,
        RecordingPublisher publisher,
        Consumer<String> warningSink
    ) {
        return new CoreRuntime(
            config,
            publisher,
            Runnable::run,
            Clock.systemUTC(),
            warningSink,
            () -> false
        );
    }

    private static CoreConfig enabledConfig(
        MariaDbContainerFixture fixture,
        List<String> locations
    ) {
        return new CoreConfig(
            1,
            "alpha-2-integration",
            Duration.ofSeconds(2),
            new CoreConfig.ExecutorSettings(2, "Wayfarer-Persistence-Test"),
            new CoreConfig.AuditSettings(true),
            new CoreConfig.HealthSettings(false),
            new CoreConfig.MariaDbSettings(
                true,
                "TEST_DB_URL",
                "TEST_DB_USERNAME",
                "TEST_DB_PASSWORD",
                4,
                1,
                Duration.ofSeconds(2),
                SecretValue.of(fixture.jdbcUrl()),
                SecretValue.of(fixture.username()),
                SecretValue.of(fixture.password())
            ),
            new CoreConfig.RedisSettings(
                false,
                "TEST_REDIS_URI",
                Duration.ofSeconds(1),
                null
            ),
            new CoreConfig.MigrationSettings(true, locations),
            new CoreConfig.WaymarkSettings(false, "RedisEconomy", Duration.ofSeconds(1))
        );
    }

    private static Set<String> tableNames(Connection connection) throws SQLException {
        Set<String> tables = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT table_name FROM information_schema.tables "
                     + "WHERE table_schema = DATABASE()"
             )) {
            while (result.next()) {
                tables.add(result.getString(1));
            }
        }
        return tables;
    }

    private static int appliedMigrationRows(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1"
             )) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private static void assertCoreConstraints(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "INSERT INTO wf_core_transaction "
                    + "(transaction_id, idempotency_key, transaction_type, subject_type, "
                    + "subject_id, amount_wm, state, payload_json) VALUES "
                    + "('00000000-0000-0000-0000-000000000001', 'once', 'TEST', "
                    + "'TEST', 'subject', 1, 'PREPARED', '{\"valid\":true}')"
            );
            assertThrows(
                SQLException.class,
                () -> statement.executeUpdate(
                    "INSERT INTO wf_core_transaction "
                        + "(transaction_id, idempotency_key, transaction_type, subject_type, "
                        + "subject_id, amount_wm, state) VALUES "
                        + "('00000000-0000-0000-0000-000000000002', 'once', 'TEST', "
                        + "'TEST', 'subject', 1, 'PREPARED')"
                )
            );
            assertThrows(
                SQLException.class,
                () -> statement.executeUpdate(
                    "INSERT INTO wf_core_transaction "
                        + "(transaction_id, idempotency_key, transaction_type, subject_type, "
                        + "subject_id, amount_wm, state, payload_json) VALUES "
                        + "('00000000-0000-0000-0000-000000000005', 'invalid-json', 'TEST', "
                        + "'TEST', 'subject', 1, 'PREPARED', 'not-json')"
                )
            );
            statement.executeUpdate(
                "INSERT INTO wf_core_audit "
                    + "(event_id, event_type, subject_type, subject_id, server_id, "
                    + "details_json, occurred_at) VALUES "
                    + "('00000000-0000-0000-0000-000000000006', 'TEST', 'TEST', "
                    + "'subject', 'test', '{\"valid\":true}', CURRENT_TIMESTAMP(3))"
            );
            assertThrows(
                SQLException.class,
                () -> statement.executeUpdate(
                    "INSERT INTO wf_core_audit "
                        + "(event_id, event_type, subject_type, subject_id, server_id, "
                        + "occurred_at) VALUES "
                        + "('00000000-0000-0000-0000-000000000006', 'TEST', 'TEST', "
                        + "'other', 'test', CURRENT_TIMESTAMP(3))"
                )
            );
            assertThrows(
                SQLException.class,
                () -> statement.executeUpdate(
                    "INSERT INTO wf_core_transaction "
                        + "(transaction_id, idempotency_key, transaction_type, subject_type, "
                        + "subject_id, amount_wm, state) VALUES "
                        + "('00000000-0000-0000-0000-000000000003', 'negative', 'TEST', "
                        + "'TEST', 'subject', -1, 'PREPARED')"
                )
            );
            assertThrows(
                SQLException.class,
                () -> statement.executeUpdate(
                    "INSERT INTO wf_core_audit "
                        + "(event_id, event_type, subject_type, subject_id, server_id, "
                        + "details_json, occurred_at) VALUES "
                        + "('00000000-0000-0000-0000-000000000004', 'TEST', 'TEST', "
                        + "'subject', 'test', 'not-json', CURRENT_TIMESTAMP(3))"
                )
            );
        }
    }

    private static void assertRequiredIndexes(Connection connection) throws SQLException {
        Set<String> indexes = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT DISTINCT index_name FROM information_schema.statistics "
                     + "WHERE table_schema = DATABASE() "
                     + "AND table_name IN ('wf_core_transaction', 'wf_core_audit', "
                     + "'wf_core_player_identity', 'wf_core_item_identity')"
             )) {
            while (result.next()) {
                indexes.add(result.getString(1));
            }
        }
        assertTrue(indexes.containsAll(Set.of(
            "uq_wf_core_transaction_idempotency",
            "ix_wf_core_transaction_state_updated",
            "uq_wf_core_audit_event",
            "ix_wf_core_audit_subject",
            "ix_wf_core_audit_actor",
            "ix_wf_core_audit_type",
            "ix_wf_core_player_identity_last_seen",
            "ix_wf_core_player_identity_name",
            "ix_wf_core_item_identity_owner_type",
            "ix_wf_core_item_identity_type_updated"
        )));
    }

    private static void assertIdentitySchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT COUNT(*) FROM information_schema.columns "
                     + "WHERE table_schema = DATABASE() "
                     + "AND ((table_name = 'wf_core_player_identity' "
                     + "AND column_name = 'player_uuid') "
                     + "OR (table_name = 'wf_core_item_identity' "
                     + "AND column_name IN ('item_instance_id', 'owner_uuid'))) "
                     + "AND character_set_name = 'ascii' AND character_maximum_length = 36"
             )) {
            assertTrue(result.next());
            assertEquals(3, result.getInt(1));
        }
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT COUNT(*) FROM information_schema.table_constraints "
                     + "WHERE constraint_schema = DATABASE() "
                     + "AND table_name IN "
                     + "('wf_core_player_identity', 'wf_core_item_identity') "
                     + "AND constraint_type = 'CHECK'"
             )) {
            assertTrue(result.next());
            assertEquals(5, result.getInt(1));
        }
        try (Statement statement = connection.createStatement()) {
            assertThrows(
                SQLException.class,
                () -> statement.executeUpdate(
                    "INSERT INTO wf_core_player_identity "
                        + "(player_uuid, last_known_name, first_seen_at, last_seen_at, "
                        + "last_server_id, lock_version) VALUES "
                        + "('10000000-0000-0000-0000-000000000001', 'PlayerOne', "
                        + "CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'test', -1)"
                )
            );
            assertThrows(
                SQLException.class,
                () -> statement.executeUpdate(
                    "INSERT INTO wf_core_item_identity "
                        + "(item_instance_id, item_type, owner_uuid, instance_epoch, "
                        + "schema_version, display_revision, created_at, updated_at) VALUES "
                        + "('20000000-0000-0000-0000-000000000001', 'type', "
                        + "'30000000-0000-0000-0000-000000000001', -1, 0, -1, "
                        + "CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))"
                )
            );
        }
    }

    private static void assertTimestampPrecision(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT COUNT(*) FROM information_schema.columns "
                     + "WHERE table_schema = DATABASE() "
                     + "AND table_name IN ('wf_core_transaction', 'wf_core_audit', "
                     + "'wf_core_player_identity', 'wf_core_item_identity') "
                     + "AND data_type = 'timestamp' AND datetime_precision = 3"
             )) {
            assertTrue(result.next());
            assertEquals(8, result.getInt(1));
        }
    }

    private static final class RecordingPublisher implements ServicePublisher {
        private int publishCount;
        private int unpublishCount;

        @Override
        public void publish(WayfarerServices services, WayfarerHealth health) {
            publishCount++;
        }

        @Override
        public void unpublish() {
            unpublishCount++;
        }
    }
}
