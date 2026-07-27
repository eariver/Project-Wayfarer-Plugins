package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import io.github.eariver.wayfarer.core.command.OperationalAuditSink;
import io.github.eariver.wayfarer.core.command.OperationalEvent;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.identity.PlayerIdentityListenerRegistrar;
import io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation;
import io.github.eariver.wayfarer.core.identity.PlayerIdentitySink;
import io.github.eariver.wayfarer.core.persistence.MariaDbPool;
import io.github.eariver.wayfarer.core.persistence.MigrationLifecycle;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import io.github.eariver.wayfarer.testkit.MariaDbContainerFixture;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DurableAuditIdentityIntegrationTest {
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-07-27T12:00:00.123456Z"),
        ZoneOffset.UTC
    );

    @Test
    void upgradesV001OnlyDatabaseWithAdditiveV002AndV003() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            Flyway.configure()
                .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
                .locations("classpath:db/migration/core")
                .target("1")
                .load()
                .migrate();
            try (Connection connection = connection(fixture)) {
                assertEquals(
                    Set.of(
                        "flyway_schema_history",
                        "wf_core_transaction",
                        "wf_core_audit"
                    ),
                    tables(connection)
                );
            }

            try (CoreConfig config = config(fixture);
                 MariaDbPool pool = MariaDbPool.open(config.serverId(), config.mariadb());
                 MigrationLifecycle migration = MigrationLifecycle.migrate(
                     pool,
                     config.migration().locations()
                 );
                 Connection connection = connection(fixture)) {
                assertEquals(3, migration.appliedMigrationCount());
                assertEquals(
                    Set.of(
                        "flyway_schema_history",
                        "wf_core_transaction",
                        "wf_core_transaction_event",
                        "wf_core_audit",
                        "wf_core_player_identity",
                        "wf_core_item_identity"
                    ),
                    tables(connection)
                );
                assertEquals(3, scalar(connection,
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1"));
                assertEquals(9, scalar(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND data_type = 'timestamp' "
                        + "AND datetime_precision = 3"));
            }
        }
    }

    @Test
    void persistsAuditPlayerAndItemIdentityAcrossRestart() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            UUID auditId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
            UUID playerId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            UUID ownerId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
            UUID itemId;

            CapturingPublisher publisher = new CapturingPublisher();
            CapturingRegistrar registrar = new CapturingRegistrar();
            CoreRuntime runtime = runtime(config(fixture), publisher, registrar);
            runtime.enable();
            WayfarerServices services = publisher.services;

            WayfarerAudit.AuditEvent auditEvent = new WayfarerAudit.AuditEvent(
                auditId,
                "TEST_DURABLE_EVENT",
                playerId,
                "TEST",
                "subject",
                "alpha-2-integration",
                "{\"result\":\"ok\"}",
                CLOCK.instant()
            );
            services.audit().record(auditEvent).toCompletableFuture().join();
            services.audit().record(auditEvent).toCompletableFuture().join();
            assertThrows(
                CompletionException.class,
                () -> services.audit().record(new WayfarerAudit.AuditEvent(
                    auditId,
                    "TEST_DURABLE_EVENT",
                    playerId,
                    "TEST",
                    "different",
                    "alpha-2-integration",
                    "{\"result\":\"changed\"}",
                    CLOCK.instant()
                )).toCompletableFuture().join()
            );

            UUID secretEvent = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            assertThrows(
                CompletionException.class,
                () -> services.audit().record(new WayfarerAudit.AuditEvent(
                    secretEvent,
                    "TEST_SECRET",
                    null,
                    "TEST",
                    "subject",
                    "alpha-2-integration",
                    "{\"value\":\"" + fixture.password() + "\"}",
                    CLOCK.instant()
                )).toCompletableFuture().join()
            );

            registrar.sink.get().observe(new PlayerIdentityObservation(
                playerId,
                "NewName",
                "alpha-2-integration",
                Instant.parse("2026-07-27T12:00:02Z")
            )).toCompletableFuture().join();
            registrar.sink.get().observe(new PlayerIdentityObservation(
                playerId,
                "StaleName",
                "alpha-2-integration",
                Instant.parse("2026-07-27T12:00:01Z")
            )).toCompletableFuture().join();
            registrar.sink.get().observe(new PlayerIdentityObservation(
                UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                "NewName",
                "alpha-2-integration",
                Instant.parse("2026-07-27T12:00:03Z")
            )).toCompletableFuture().join();

            WayfarerItemIdentity.Identity created = services.itemIdentity().create(
                new WayfarerItemIdentity.CreateRequest("growth_pickaxe", ownerId, 7, 1, 2)
            ).toCompletableFuture().join();
            itemId = created.itemInstanceId();
            WayfarerItemIdentity.ValidationResult valid = services.itemIdentity().validate(
                validation(created, ownerId, 7)
            ).toCompletableFuture().join();
            assertTrue(valid.valid());
            WayfarerItemIdentity.ValidationResult mismatch = services.itemIdentity().validate(
                validation(created, ownerId, 8)
            ).toCompletableFuture().join();
            assertEquals(
                WayfarerItemIdentity.FailureReason.EPOCH_MISMATCH,
                mismatch.failureReason().orElseThrow()
            );

            new OperationalAuditSink(services.audit(), "alpha-2-integration", CLOCK)
                .record(new OperationalEvent(
                    "ADMIN_HEALTH_PERMISSION_DENIED",
                    playerId,
                    OperationalEvent.AudienceKind.PLAYER,
                    "ADMIN_COMMAND",
                    "health",
                    null
                )).toCompletableFuture().join();

            try (Connection connection = connection(fixture)) {
                assertEquals(1, scalar(connection,
                    "SELECT COUNT(*) FROM wf_core_audit WHERE event_id = '" + auditId + "'"));
                assertEquals(0, scalar(connection,
                    "SELECT COUNT(*) FROM wf_core_audit WHERE event_id = '" + secretEvent + "'"));
                assertEquals("NewName", text(connection,
                    "SELECT last_known_name FROM wf_core_player_identity WHERE player_uuid = '"
                        + playerId + "'"));
                assertEquals(2, scalar(connection,
                    "SELECT COUNT(*) FROM wf_core_player_identity "
                        + "WHERE last_known_name = 'NewName'"));
                assertEquals(1, scalar(connection,
                    "SELECT COUNT(*) FROM wf_core_audit "
                        + "WHERE event_type = 'ITEM_IDENTITY_VALIDATION_FAILED'"));
                assertEquals(1, scalar(connection,
                    "SELECT COUNT(*) FROM wf_core_audit "
                        + "WHERE event_type = 'ADMIN_HEALTH_PERMISSION_DENIED' "
                        + "AND actor_uuid = '" + playerId + "'"));
            }
            WayfarerAudit closedAudit = services.audit();
            WayfarerItemIdentity closedIdentity = services.itemIdentity();
            runtime.disable();
            assertEquals(null, registrar.sink.get());
            assertThrows(
                CompletionException.class,
                () -> closedAudit.record(auditEvent).toCompletableFuture().join()
            );
            assertThrows(
                CompletionException.class,
                () -> closedIdentity.find(itemId).toCompletableFuture().join()
            );
            assertEquals(
                WayfarerHealth.Status.DISABLED,
                runtime.health().snapshot().components().get("Audit").status()
            );
            assertEquals(
                WayfarerHealth.Status.DISABLED,
                runtime.health().snapshot().components().get("Identity").status()
            );

            CapturingPublisher restartedPublisher = new CapturingPublisher();
            CoreRuntime restarted = runtime(
                config(fixture),
                restartedPublisher,
                new CapturingRegistrar()
            );
            restarted.enable();
            assertTrue(
                restartedPublisher.services.itemIdentity().find(itemId)
                    .toCompletableFuture().join().isPresent()
            );
            restartedPublisher.services.audit().record(auditEvent).toCompletableFuture().join();
            restarted.disable();

            try (Connection connection = connection(fixture)) {
                assertEquals(1, scalar(connection,
                    "SELECT COUNT(*) FROM wf_core_audit WHERE event_id = '" + auditId + "'"));
            }
        }
    }

    @Test
    void databaseOutageMakesAuditAndIdentityExceptionalAndDown() {
        MariaDbContainerFixture fixture = MariaDbContainerFixture.start();
        CapturingPublisher publisher = new CapturingPublisher();
        CoreRuntime runtime = runtime(config(fixture), publisher, new CapturingRegistrar());
        runtime.enable();
        WayfarerServices services = publisher.services;
        fixture.close();

        assertThrows(
            CompletionException.class,
            () -> services.audit().record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                "OUTAGE_TEST",
                null,
                "TEST",
                "subject",
                "alpha-2-integration",
                null,
                CLOCK.instant()
            )).toCompletableFuture().join()
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            runtime.health().snapshot().components().get("Audit").status()
        );
        assertThrows(
            CompletionException.class,
            () -> services.itemIdentity().create(
                new WayfarerItemIdentity.CreateRequest(
                    "outage_item",
                    UUID.randomUUID(),
                    0,
                    1,
                    0
                )
            ).toCompletableFuture().join()
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            runtime.health().snapshot().components().get("Identity").status()
        );
        runtime.disable();
    }

    private static WayfarerItemIdentity.ValidationRequest validation(
        WayfarerItemIdentity.Identity identity,
        UUID owner,
        long expectedEpoch
    ) {
        return new WayfarerItemIdentity.ValidationRequest(
            new WayfarerItemIdentity.RawClaim(
                identity.itemInstanceId().toString(),
                identity.itemType(),
                identity.ownerUuid().toString(),
                identity.instanceEpoch(),
                identity.schemaVersion(),
                identity.displayRevision()
            ),
            Set.of(identity.itemType()),
            Set.of(identity.schemaVersion()),
            owner,
            OptionalLong.of(expectedEpoch)
        );
    }

    private static CoreRuntime runtime(
        CoreConfig config,
        CapturingPublisher publisher,
        PlayerIdentityListenerRegistrar registrar
    ) {
        return new CoreRuntime(
            config,
            publisher,
            Runnable::run,
            CLOCK,
            ignored -> {},
            () -> false,
            registrar
        );
    }

    private static CoreConfig config(MariaDbContainerFixture fixture) {
        return new CoreConfig(
            1,
            "alpha-2-integration",
            Duration.ofSeconds(5),
            new CoreConfig.ExecutorSettings(2, "Wayfarer-Audit-Identity-Test"),
            new CoreConfig.AuditSettings(true),
            new CoreConfig.HealthSettings(false),
            new CoreConfig.MariaDbSettings(
                true,
                "TEST_DB_URL",
                "TEST_DB_USERNAME",
                "TEST_DB_PASSWORD",
                4,
                1,
                Duration.ofSeconds(3),
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
            new CoreConfig.MigrationSettings(true, List.of("db/migration/core")),
            new CoreConfig.WaymarkSettings(false, "RedisEconomy", Duration.ofSeconds(1))
        );
    }

    private static Connection connection(MariaDbContainerFixture fixture) throws Exception {
        return DriverManager.getConnection(
            fixture.jdbcUrl(),
            fixture.username(),
            fixture.password()
        );
    }

    private static Set<String> tables(Connection connection) throws Exception {
        java.util.HashSet<String> values = new java.util.HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT table_name FROM information_schema.tables "
                     + "WHERE table_schema = DATABASE()"
             )) {
            while (result.next()) {
                values.add(result.getString(1));
            }
        }
        return values;
    }

    private static int scalar(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private static String text(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private static final class CapturingPublisher implements ServicePublisher {
        private WayfarerServices services;

        @Override
        public void publish(WayfarerServices published, WayfarerHealth health) {
            services = published;
        }

        @Override
        public void unpublish() {
            services = null;
        }
    }

    private static final class CapturingRegistrar implements PlayerIdentityListenerRegistrar {
        private final AtomicReference<PlayerIdentitySink> sink = new AtomicReference<>();

        @Override
        public AutoCloseable register(PlayerIdentitySink registered) {
            sink.set(registered);
            return () -> sink.set(null);
        }
    }
}
