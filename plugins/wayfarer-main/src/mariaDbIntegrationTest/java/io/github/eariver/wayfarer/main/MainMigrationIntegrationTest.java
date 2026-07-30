package io.github.eariver.wayfarer.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.testkit.MariaDbContainerFixture;
import io.github.eariver.wayfarer.main.persistence.JdbcRepairOperationRepository;
import io.github.eariver.wayfarer.main.domain.RepairOperation;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

final class MainMigrationIntegrationTest {
    @Test
    void repairRepositoryPersistsTerminalFailureWithCas() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            coreFlyway(fixture).migrate();
            flyway(fixture).migrate();
            UUID player = UUID.randomUUID();
            UUID tool = UUID.randomUUID();
            try (Connection connection = connection(fixture);
                 PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO wf_main_growth_tool "
                         + "(tool_id,owner_uuid,tool_type) VALUES (?,?,'PICKAXE')"
                 )) {
                insert.setString(1, tool.toString());
                insert.setString(2, player.toString());
                insert.executeUpdate();
            }
            var dataSource = new org.mariadb.jdbc.MariaDbDataSource(
                fixture.jdbcUrl()
            );
            dataSource.setUser(fixture.username());
            dataSource.setPassword(fixture.password());
            JdbcRepairOperationRepository repository =
                new JdbcRepairOperationRepository(dataSource);
            RepairOperation prepared = repository.prepare(
                "repair:integration:1",
                player,
                tool,
                1,
                100,
                Instant.now()
            );
            RepairOperation claimed = repository.claimPayment(
                prepared.repairId(),
                prepared.lockVersion(),
                Instant.now()
            ).orElseThrow();
            RepairOperation failed = repository.failed(
                claimed.repairId(),
                claimed.lockVersion(),
                "INSUFFICIENT_FUNDS",
                Instant.now()
            ).orElseThrow();
            assertEquals(RepairOperation.State.FAILED, failed.state());
            assertEquals("INSUFFICIENT_FUNDS", failed.failureCode());
            assertTrue(repository.claimPayment(
                failed.repairId(),
                failed.lockVersion(),
                Instant.now()
            ).isEmpty());
        }
    }
    @Test
    void migratesCoreThenMainInSameEmptySchemaAndRepeats() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            assertEquals(3, coreFlyway(fixture).migrate().migrationsExecuted);
            Flyway flyway = flyway(fixture);
            assertEquals(2, flyway.migrate().migrationsExecuted);
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertEquals(0, coreFlyway(fixture).migrate().migrationsExecuted);
            try (Connection connection = connection(fixture)) {
                Set<String> tables = tables(connection);
                assertTrue(tables.containsAll(Set.of(
                    "flyway_schema_history",
                    "wf_main_flyway_schema_history",
                    "wf_main_growth_tool",
                    "wf_main_repair_operation",
                    "wf_core_transaction"
                )));
                assertTrue(tables.stream().noneMatch(name ->
                    name.startsWith("wf_frontier_")));
                assertEquals(3, successfulMigrations(
                    connection,
                    "flyway_schema_history"
                ));
                assertEquals(3, successfulMigrations(
                    connection,
                    "wf_main_flyway_schema_history"
                ));
            }
        }
    }

    @Test
    void upgradesV001SchemaWithoutChangingV001() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            assertEquals(3, coreFlyway(fixture).migrate().migrationsExecuted);
            Flyway first = Flyway.configure()
                .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
                .locations("classpath:db/migration/main")
                .table("wf_main_flyway_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .target("1")
                .load();
            assertEquals(1, first.migrate().migrationsExecuted);
            assertEquals(1, flyway(fixture).migrate().migrationsExecuted);
            try (Connection connection = connection(fixture)) {
                assertTrue(tables(connection).contains("wf_main_repair_operation"));
            }
        }
    }

    @Test
    void migrationFailureIsExceptionalAndNotRecordedSuccessful() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            Flyway broken = Flyway.configure()
                .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
                .locations(
                    "classpath:db/migration/main",
                    "classpath:db/migration/main-failure"
                )
                .table("wf_main_flyway_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
            coreFlyway(fixture).migrate();
            flyway(fixture).migrate();
            assertThrows(RuntimeException.class, broken::migrate);
            try (Connection connection = connection(fixture)) {
                assertEquals(3, successfulMigrations(
                    connection,
                    "wf_main_flyway_schema_history"
                ));
            }
        }
    }

    private static Flyway flyway(MariaDbContainerFixture fixture) {
        return Flyway.configure()
            .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
            .locations("classpath:db/migration/main")
            .table("wf_main_flyway_schema_history")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .load();
    }

    private static Flyway coreFlyway(MariaDbContainerFixture fixture) {
        return Flyway.configure()
            .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
            .locations("classpath:db/migration/core")
            .load();
    }

    private static Connection connection(MariaDbContainerFixture fixture) throws Exception {
        return DriverManager.getConnection(
            fixture.jdbcUrl(), fixture.username(), fixture.password()
        );
    }

    private static Set<String> tables(Connection connection) throws Exception {
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

    private static int successfulMigrations(
        Connection connection,
        String historyTable
    ) throws Exception {
        if (!historyTable.matches("[a-z_]{3,64}")) {
            throw new IllegalArgumentException("Invalid history table");
        }
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT COUNT(*) FROM " + historyTable + " WHERE success = 1"
             )) {
            result.next();
            return result.getInt(1);
        }
    }
}
