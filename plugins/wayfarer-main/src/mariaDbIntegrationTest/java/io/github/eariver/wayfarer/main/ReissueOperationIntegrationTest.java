package io.github.eariver.wayfarer.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.main.application.PrepareOutcome;
import io.github.eariver.wayfarer.main.application.PrepareResult;
import io.github.eariver.wayfarer.main.application.ReissueOperationRepository;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.domain.ReissueOperation;
import io.github.eariver.wayfarer.main.domain.RepairOperation;
import io.github.eariver.wayfarer.main.persistence.JdbcGrowthToolRepository;
import io.github.eariver.wayfarer.main.persistence.JdbcRepairOperationRepository;
import io.github.eariver.wayfarer.main.persistence.JdbcReissueOperationRepository;
import io.github.eariver.wayfarer.testkit.MariaDbContainerFixture;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

final class ReissueOperationIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");

    @Test
    void reissueRoundTripIsKindScopedAndIdempotent() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            migrate(fixture);
            UUID player = UUID.randomUUID();
            UUID tool = insertTool(fixture, player);
            var dataSource = dataSource(fixture);
            ReissueOperationRepository repository =
                new JdbcReissueOperationRepository(dataSource);
            ReissueOperation prepared = operation(player, tool, UUID.randomUUID());

            PrepareOutcome created = repository.prepare(prepared, NOW);
            assertEquals(PrepareResult.CREATED, created.result());
            assertEquals(prepared.reissueId(), created.operation().reissueId());
            assertEquals(
                created.operation(),
                repository.findByIdempotency(prepared.idempotencyKey()).orElseThrow()
            );
            assertEquals(
                created.operation(),
                repository.findActiveByTool(tool).orElseThrow()
            );

            ReissueOperation claimed = repository.claimPayment(
                prepared.reissueId(),
                prepared.lockVersion(),
                NOW
            ).orElseThrow();
            UUID transaction = UUID.randomUUID();
            ReissueOperation paid = repository.paymentCommitted(
                claimed.reissueId(),
                transaction,
                claimed.lockVersion(),
                NOW.plusSeconds(1)
            ).orElseThrow();
            assertEquals(transaction, paid.transactionId());
            assertNotNull(paid.paymentCommittedAt());

            ReissueOperation pending = repository.pendingDelivery(
                paid.reissueId(),
                paid.lockVersion(),
                NOW.plusSeconds(2)
            ).orElseThrow();
            assertEquals(ReissueOperation.State.PENDING_DELIVERY, pending.state());
            assertTrue(repository.findActiveByTool(tool).isEmpty());
            assertTrue(repository.delivered(
                pending.reissueId(),
                pending.lockVersion(),
                NOW.plusSeconds(3)
            ));
            assertEquals(
                ReissueOperation.State.DELIVERED,
                repository.find(prepared.reissueId()).orElseThrow().state()
            );

            ReissueOperation replayInput = operation(player, tool, UUID.randomUUID());
            PrepareOutcome replay = repository.prepare(
                new ReissueOperation(
                    replayInput.reissueId(),
                    prepared.idempotencyKey(),
                    player,
                    tool,
                    prepared.expectedItemInstanceId(),
                    replayInput.newItemInstanceId(),
                    prepared.instanceEpoch(),
                    prepared.evolutionCount(),
                    prepared.configRevision(),
                    prepared.amountWaymark(),
                    ReissueOperation.State.PREPARED,
                    null,
                    null,
                    null,
                    0
                ),
                NOW
            );
            assertEquals(PrepareResult.EXISTING, replay.result());
            assertEquals(prepared.reissueId(), replay.operation().reissueId());

            JdbcRepairOperationRepository repair =
                new JdbcRepairOperationRepository(dataSource);
            RepairOperation repairOperation = repair.prepare(
                "repair:kind:integration",
                player,
                tool,
                1,
                100,
                NOW
            );
            assertTrue(repair.find(repairOperation.repairId()).isPresent());
            assertTrue(repository.find(repairOperation.repairId()).isEmpty());
        }
    }

    @Test
    void unknownAndMarkerCasPreserveTransactionIdentityAndRecoveryCandidates() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            migrate(fixture);
            UUID player = UUID.randomUUID();
            UUID tool = insertTool(fixture, player);
            ReissueOperationRepository repository =
                new JdbcReissueOperationRepository(dataSource(fixture));
            ReissueOperation prepared = operation(player, tool, UUID.randomUUID());
            ReissueOperation claimed = repository.prepare(prepared, NOW).operation();
            claimed = repository.claimPayment(
                claimed.reissueId(),
                claimed.lockVersion(),
                NOW
            ).orElseThrow();
            UUID transaction = UUID.randomUUID();
            ReissueOperation unknown = repository.unknown(
                claimed.reissueId(),
                claimed.lockVersion(),
                transaction,
                "PAYMENT_IN_FLIGHT",
                NOW.plusSeconds(1)
            ).orElseThrow();
            assertEquals(transaction, unknown.transactionId());
            assertEquals(ReissueOperation.State.UNKNOWN, unknown.state());
            assertTrue(repository.reopenPayment(
                unknown.reissueId(),
                unknown.lockVersion(),
                NOW
            ).isEmpty());

            assertTrue(repository.unknown(
                unknown.reissueId(),
                unknown.lockVersion(),
                UUID.randomUUID(),
                "CONFLICT",
                NOW
            ).isEmpty());
            ReissueOperation unchanged = repository.find(unknown.reissueId()).orElseThrow();
            assertEquals(transaction, unchanged.transactionId());

            ReissueOperation committed = repository.confirmPaymentCommittedFromUnknown(
                unknown.reissueId(),
                transaction,
                unknown.lockVersion(),
                NOW.plusSeconds(2),
                NOW.plusSeconds(2)
            ).orElseThrow();
            assertNotNull(committed.paymentCommittedAt());
            assertEquals(transaction, committed.transactionId());

            ReissueOperation secondPrepared = operation(player, tool, UUID.randomUUID());
            PrepareOutcome second = repository.prepare(secondPrepared, NOW);
            assertEquals(PrepareResult.IN_FLIGHT, second.result());

            UUID secondPlayer = UUID.randomUUID();
            UUID secondTool = insertTool(fixture, secondPlayer);
            ReissueOperation abandonedInput = operation(
                secondPlayer,
                secondTool,
                UUID.randomUUID()
            );
            ReissueOperation abandoned = repository.prepare(abandonedInput, NOW).operation();
            assertTrue(repository.findRecoveryCandidates().stream().anyMatch(
                value -> value.reissueId().equals(abandoned.reissueId())
            ));
            ReissueOperation abandonedResult = repository.abandoned(
                abandoned.reissueId(),
                abandoned.lockVersion(),
                NOW.plusSeconds(3)
            ).orElseThrow();
            assertEquals(ReissueOperation.State.ABANDONED, abandonedResult.state());
            assertTrue(repository.findActiveByTool(secondTool).isEmpty());
        }
    }

    @Test
    void paymentCommittedUnknownCanBeReopenedOnlyWithPaymentEvidence() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            migrate(fixture);
            UUID player = UUID.randomUUID();
            UUID tool = insertTool(fixture, player);
            ReissueOperationRepository repository =
                new JdbcReissueOperationRepository(dataSource(fixture));

            ReissueOperation prepared = repository.prepare(
                operation(player, tool, UUID.randomUUID()),
                NOW
            ).operation();
            ReissueOperation claimed = repository.claimPayment(
                prepared.reissueId(),
                prepared.lockVersion(),
                NOW
            ).orElseThrow();
            UUID transaction = UUID.randomUUID();
            ReissueOperation paid = repository.paymentCommitted(
                claimed.reissueId(),
                transaction,
                claimed.lockVersion(),
                NOW.plusSeconds(1)
            ).orElseThrow();
            ReissueOperation unknown = repository.unknown(
                paid.reissueId(),
                paid.lockVersion(),
                transaction,
                "ROTATION_COMMIT_UNKNOWN",
                NOW.plusSeconds(2)
            ).orElseThrow();

            assertEquals(ReissueOperation.State.UNKNOWN, unknown.state());
            assertEquals(transaction, unknown.transactionId());
            assertNotNull(unknown.paymentCommittedAt());
            assertTrue(repository.findActiveByTool(tool).isPresent());
            assertTrue(repository.reopenToPaymentCommitted(
                unknown.reissueId(),
                unknown.lockVersion() + 1,
                NOW.plusSeconds(3)
            ).isEmpty());

            ReissueOperation reopened = repository.reopenToPaymentCommitted(
                unknown.reissueId(),
                unknown.lockVersion(),
                NOW.plusSeconds(3)
            ).orElseThrow();
            assertEquals(ReissueOperation.State.PAYMENT_COMMITTED, reopened.state());
            assertEquals(transaction, reopened.transactionId());
            assertNotNull(reopened.paymentCommittedAt());
            assertNull(reopened.failureCode());
            assertTrue(repository.findActiveByTool(tool).isPresent());
            assertTrue(repository.reopenToPaymentCommitted(
                reopened.reissueId(),
                reopened.lockVersion(),
                NOW.plusSeconds(4)
            ).isEmpty());

            UUID markerlessTool = insertTool(fixture, UUID.randomUUID());
            ReissueOperation markerlessPrepared = repository.prepare(
                operation(UUID.randomUUID(), markerlessTool, UUID.randomUUID()),
                NOW
            ).operation();
            ReissueOperation markerlessClaimed = repository.claimPayment(
                markerlessPrepared.reissueId(),
                markerlessPrepared.lockVersion(),
                NOW
            ).orElseThrow();
            ReissueOperation markerlessUnknown = repository.unknown(
                markerlessClaimed.reissueId(),
                markerlessClaimed.lockVersion(),
                UUID.randomUUID(),
                "PAYMENT_UNKNOWN",
                NOW
            ).orElseThrow();
            assertTrue(repository.reopenToPaymentCommitted(
                markerlessUnknown.reissueId(),
                markerlessUnknown.lockVersion(),
                NOW
            ).isEmpty());

            UUID transactionlessTool = insertTool(fixture, UUID.randomUUID());
            UUID transactionlessPlayer = UUID.randomUUID();
            ReissueOperation transactionlessPrepared = repository.prepare(
                operation(transactionlessPlayer, transactionlessTool, UUID.randomUUID()),
                NOW
            ).operation();
            ReissueOperation transactionlessClaimed = repository.claimPayment(
                transactionlessPrepared.reissueId(),
                transactionlessPrepared.lockVersion(),
                NOW
            ).orElseThrow();
            ReissueOperation transactionlessUnknown = repository.unknown(
                transactionlessClaimed.reissueId(),
                transactionlessClaimed.lockVersion(),
                null,
                "PAYMENT_UNKNOWN",
                NOW
            ).orElseThrow();
            assertTrue(repository.reopenToPaymentCommitted(
                transactionlessUnknown.reissueId(),
                transactionlessUnknown.lockVersion(),
                NOW
            ).isEmpty());
        }
    }

    @Test
    void stateGuardAndPaidChecksAreEnforcedByMariaDb() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            migrate(fixture);
            UUID player = UUID.randomUUID();
            UUID tool = insertTool(fixture, player);
            ReissueOperationRepository repository =
                new JdbcReissueOperationRepository(dataSource(fixture));
            ReissueOperation prepared = repository.prepare(
                operation(player, tool, UUID.randomUUID()),
                NOW
            ).operation();

            assertThrows(Exception.class, () -> update(
                fixture,
                "UPDATE wf_main_repair_operation SET state='PAYMENT_COMMITTED' "
                    + "WHERE repair_id=?",
                prepared.reissueId().toString()
            ));
            assertThrows(Exception.class, () -> update(
                fixture,
                "UPDATE wf_main_repair_operation SET active_guard=NULL "
                    + "WHERE repair_id=?",
                prepared.reissueId().toString()
            ));
            assertThrows(Exception.class, () -> update(
                fixture,
                "UPDATE wf_main_repair_operation SET state='PENDING_DELIVERY',"
                    + "transaction_id=?,payment_committed_at=?,active_guard=? "
                    + "WHERE repair_id=?",
                UUID.randomUUID().toString(),
                java.sql.Timestamp.from(NOW),
                tool.toString(),
                prepared.reissueId().toString()
            ));
        }
    }

    private static ReissueOperation operation(
        UUID player,
        UUID tool,
        UUID newItemInstanceId
    ) {
        return new ReissueOperation(
            UUID.randomUUID(),
            "main-reissue:quote-" + UUID.randomUUID().toString().replace("-", ""),
            player,
            tool,
            UUID.randomUUID(),
            newItemInstanceId,
            1,
            3,
            "main-test",
            363,
            ReissueOperation.State.PREPARED,
            null,
            null,
            null,
            0
        );
    }

    private static void migrate(MariaDbContainerFixture fixture) {
        Flyway.configure()
            .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
            .locations("classpath:db/migration/core")
            .load()
            .migrate();
        Flyway.configure()
            .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
            .locations("classpath:db/migration/main")
            .table("wf_main_flyway_schema_history")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .load()
            .migrate();
    }

    private static UUID insertTool(
        MariaDbContainerFixture fixture,
        UUID player
    ) throws Exception {
        UUID tool = UUID.randomUUID();
        try (Connection connection = connection(fixture);
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO wf_main_growth_tool "
                     + "(tool_id,current_item_instance_id,owner_uuid,tool_type) "
                     + "VALUES (?,?,?,'PICKAXE')"
             )) {
            insert.setString(1, tool.toString());
            insert.setString(2, UUID.randomUUID().toString());
            insert.setString(3, player.toString());
            insert.executeUpdate();
        }
        return tool;
    }

    private static org.mariadb.jdbc.MariaDbDataSource dataSource(
        MariaDbContainerFixture fixture
    ) throws Exception {
        org.mariadb.jdbc.MariaDbDataSource dataSource =
            new org.mariadb.jdbc.MariaDbDataSource(fixture.jdbcUrl());
        dataSource.setUser(fixture.username());
        dataSource.setPassword(fixture.password());
        return dataSource;
    }

    private static Connection connection(MariaDbContainerFixture fixture) throws Exception {
        return DriverManager.getConnection(
            fixture.jdbcUrl(),
            fixture.username(),
            fixture.password()
        );
    }

    private static void update(
        MariaDbContainerFixture fixture,
        String sql,
        Object... values
    ) throws Exception {
        try (Connection connection = connection(fixture);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }
}
