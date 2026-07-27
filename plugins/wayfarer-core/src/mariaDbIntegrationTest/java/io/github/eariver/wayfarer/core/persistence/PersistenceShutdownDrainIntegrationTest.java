package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import io.github.eariver.wayfarer.core.CoreRuntime;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import io.github.eariver.wayfarer.testkit.MariaDbContainerFixture;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceShutdownDrainIntegrationTest {
    @Test
    void acceptedMariaDbWorkDrainsBeforeRuntimeClosesPool() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start();
             CoreConfig config = enabledConfig(fixture)) {
            List<String> warnings = new ArrayList<>();
            RecordingPublisher publisher = new RecordingPublisher();
            CoreRuntime runtime = new CoreRuntime(
                config,
                publisher,
                Runnable::run,
                Clock.systemUTC(),
                warnings::add,
                () -> false
            );
            runtime.enable();
            MariaDbPool pool = pool(runtime);
            InternalDatabase database = pool.internalDatabaseForTesting();
            CountDownLatch firstStarted = new CountDownLatch(1);
            CountDownLatch releaseFirst = new CountDownLatch(1);

            var first = database.transaction(connection -> {
                firstStarted.countDown();
                await(releaseFirst);
                insertAudit(connection, "00000000-0000-0000-0000-000000000101");
                return 1;
            }).toCompletableFuture();
            assertTrue(firstStarted.await(1, TimeUnit.SECONDS));
            var second = database.transaction(connection -> {
                insertAudit(connection, "00000000-0000-0000-0000-000000000102");
                return 2;
            }).toCompletableFuture();

            AtomicReference<Throwable> disableFailure = new AtomicReference<>();
            Thread disableThread = new Thread(() -> {
                try {
                    runtime.disable();
                } catch (Throwable failure) {
                    disableFailure.set(failure);
                }
            }, "Wayfarer-Integration-Disable");
            disableThread.start();
            assertTrue(database.awaitIntakeStopped(Duration.ofSeconds(1)));
            assertFalse(database.isAccepting());
            releaseFirst.countDown();
            disableThread.join(5_000);

            assertFalse(disableThread.isAlive());
            assertNull(disableFailure.get());
            assertEquals(1, first.get(1, TimeUnit.SECONDS));
            assertEquals(2, second.get(1, TimeUnit.SECONDS));
            assertEquals(2, auditEffectCount(fixture));
            assertTrue(pool.isClosed());
            assertEquals(1, publisher.unpublishCount);
            assertEquals(
                WayfarerHealth.Status.DISABLED,
                runtime.health().snapshot().components().get("MariaDB").status()
            );
            assertEquals(
                WayfarerHealth.Status.DISABLED,
                runtime.health().snapshot().components().get("Executor").status()
            );
            assertTrue(warnings.isEmpty());

            runtime.disable();
            assertEquals(1, publisher.unpublishCount);
        }
    }

    private static MariaDbPool pool(CoreRuntime runtime) throws ReflectiveOperationException {
        Field field = CoreRuntime.class.getDeclaredField("mariaDbPool");
        assertTrue(field.trySetAccessible());
        return (MariaDbPool) field.get(runtime);
    }

    private static void await(CountDownLatch latch) throws SQLException {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new SQLException("test latch timed out");
            }
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new SQLException("test latch was interrupted");
        }
    }

    private static void insertAudit(Connection connection, String eventId)
        throws SQLException {
        try (var statement = connection.prepareStatement(
            "INSERT INTO wf_core_audit "
                + "(event_id, event_type, subject_type, subject_id, server_id, "
                + "details_json, occurred_at) VALUES (?, 'DRAIN_TEST', 'TEST', "
                + "'shutdown', 'alpha-2-integration', '{\"drained\":true}', "
                + "CURRENT_TIMESTAMP(3))"
        )) {
            statement.setString(1, eventId);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private static int auditEffectCount(MariaDbContainerFixture fixture)
        throws SQLException {
        try (Connection connection = DriverManager.getConnection(
            fixture.jdbcUrl(),
            fixture.username(),
            fixture.password()
        );
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT COUNT(*) FROM wf_core_audit WHERE event_type = 'DRAIN_TEST'"
             )) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private static CoreConfig enabledConfig(MariaDbContainerFixture fixture) {
        return new CoreConfig(
            1,
            "alpha-2-drain-integration",
            Duration.ofSeconds(2),
            new CoreConfig.ExecutorSettings(1, "Wayfarer-Persistence-Drain-Integration"),
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
            new CoreConfig.MigrationSettings(true, List.of("db/migration/core")),
            new CoreConfig.WaymarkSettings(false, "RedisEconomy", Duration.ofSeconds(1))
        );
    }

    private static final class RecordingPublisher implements ServicePublisher {
        private int unpublishCount;

        @Override
        public void publish(WayfarerServices services, WayfarerHealth health) {}

        @Override
        public void unpublish() {
            unpublishCount++;
        }
    }
}
