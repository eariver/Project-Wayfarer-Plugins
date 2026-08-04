package io.github.eariver.wayfarer.frontier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.testkit.MariaDbContainerFixture;
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.domain.DeathIdentitySnapshot;
import io.github.eariver.wayfarer.frontier.domain.DeathPersistResult;
import io.github.eariver.wayfarer.frontier.domain.DeliveryCompletion;
import io.github.eariver.wayfarer.frontier.domain.FrontierShopCatalog;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import io.github.eariver.wayfarer.frontier.persistence.JdbcFrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.persistence.JdbcLaunchpadRepository;
import io.github.eariver.wayfarer.frontier.persistence.JdbcTraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;
import java.time.Duration;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

final class FrontierMigrationIntegrationTest {
    @Test
    void purchaseDeliveryAndLaunchClaimRepositoriesUseDurableCas() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            coreFlyway(fixture).migrate();
            flyway(fixture).migrate();
            var dataSource = new org.mariadb.jdbc.MariaDbDataSource(
                fixture.jdbcUrl()
            );
            dataSource.setUser(fixture.username());
            dataSource.setPassword(fixture.password());
            JdbcFrontierPurchaseRepository purchases =
                new JdbcFrontierPurchaseRepository(dataSource);
            Instant now = Instant.now();
            UUID player = UUID.randomUUID();
            JdbcTraversalLoadoutRepository loadouts =
                new JdbcTraversalLoadoutRepository(
                    dataSource,
                    new FrontierModuleConfig.LoadoutDefinition(2, true, false)
                );
            var loadout = loadouts.findOrCreate(player, now);
            var initial = loadouts.ensureInitialDeliveries(loadout, now);
            assertEquals(4, initial.size());
            assertEquals(3, initial.stream()
                .filter(value -> value.identity() != null)
                .count());
            assertTrue(loadouts.reissuePermanent(
                player,
                TraversalIdentity.ItemType.ELYTRA,
                now.plusSeconds(1)
            ));
            var reissued = loadouts.pending(player);
            assertEquals(4, reissued.size());
            assertEquals(1, reissued.stream()
                .filter(value -> value.identity() != null)
                .filter(value ->
                    value.identity().itemType()
                        == TraversalIdentity.ItemType.ELYTRA
                        && value.identity().instanceEpoch() == 2
                ).count());
            assertTrue(reissued.stream()
                .filter(value -> value.identity() != null)
                .filter(value ->
                    value.identity().itemType()
                        != TraversalIdentity.ItemType.ELYTRA
                ).allMatch(value ->
                    value.identity().instanceEpoch() == 1
                ));

            FrontierPurchaseRepository.Purchase prepared = purchases.prepare(
                "purchase:integration:1",
                player,
                new FrontierShopCatalog().findV002("launchpad").orElseThrow(),
                now
            );
            FrontierPurchaseRepository.Purchase claimed = purchases.claimPayment(
                prepared.purchaseId(),
                prepared.lockVersion(),
                now
            ).orElseThrow();
            UUID transaction = UUID.randomUUID();
            assertTrue(purchases.markPaymentCommitted(
                claimed.purchaseId(),
                transaction,
                claimed.lockVersion(),
                now
            ));
            FrontierPurchaseRepository.Purchase paid = purchases.find(
                claimed.purchaseId()
            ).orElseThrow();
            UUID deliveryId = UUID.randomUUID();
            PendingDelivery delivery = new PendingDelivery(
                deliveryId,
                player,
                "worlds_beyond",
                PendingDelivery.ItemType.LAUNCHPAD,
                1,
                "delivery:integration:1",
                PendingDelivery.State.PENDING,
                0,
                now
            );
            FrontierPurchaseRepository.Purchase pending =
                purchases.attachPendingDelivery(
                    paid.purchaseId(),
                    paid.lockVersion(),
                    delivery,
                    now
                ).orElseThrow();
            assertTrue(purchases.markDelivered(
                pending.purchaseId(),
                deliveryId,
                pending.lockVersion(),
                now
            ));
            assertEquals(
                FrontierPurchaseRepository.State.DELIVERED,
                purchases.find(pending.purchaseId()).orElseThrow().state()
            );

            UUID launchpadId = UUID.randomUUID();
            try (Connection connection = connection(fixture);
                 PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO wf_frontier_launchpad "
                         + "(launchpad_id,world_id,x,y,z,yaw,placer_uuid,"
                         + "max_uses_at_creation,created_at,expires_at,definition_id,"
                         + "state,schema_version) VALUES "
                         + "(?,'frontier_iris',1,64,1,0,?,3,?,?,"
                         + "'default','ACTIVE',1)"
                 )) {
                insert.setString(1, launchpadId.toString());
                insert.setString(2, player.toString());
                insert.setTimestamp(3, java.sql.Timestamp.from(now));
                insert.setTimestamp(4, java.sql.Timestamp.from(
                    now.plus(Duration.ofDays(30))
                ));
                insert.executeUpdate();
            }
            JdbcLaunchpadRepository launchpads =
                new JdbcLaunchpadRepository(dataSource);
            assertEquals(1, launchpads.countActive(player, now));
            assertEquals(
                java.util.List.of(launchpadId),
                launchpads.findActive(100).stream()
                    .map(Launchpad::launchpadId)
                    .toList()
            );
            assertFalse(launchpads.create(
                new Launchpad(
                    UUID.randomUUID(),
                    new Launchpad.Location("frontier_iris", 2, 64, 2),
                    0,
                    player,
                    0,
                    3,
                    now,
                    null,
                    now.plus(Duration.ofDays(30)),
                    "default",
                    Launchpad.State.ACTIVE,
                    1,
                    0
                ),
                1,
                now
            ));
            var firstClaim = launchpads.claimForUse(
                launchpadId,
                now.plusSeconds(5),
                now
            ).orElseThrow();
            assertTrue(launchpads.releaseUseClaim(
                launchpadId,
                firstClaim.lockVersion(),
                now
            ));
            Launchpad secondClaim = launchpads.claimForUse(
                launchpadId,
                now.plusSeconds(5),
                now
            ).orElseThrow();
            assertTrue(launchpads.releaseUseClaim(
                launchpadId,
                secondClaim.lockVersion(),
                now
            ));
            Launchpad breakable = launchpads.find(launchpadId).orElseThrow();
            assertTrue(launchpads.remove(
                launchpadId,
                breakable.lockVersion(),
                Launchpad.State.PLAYER_BROKEN,
                now
            ));
            assertFalse(launchpads.remove(
                launchpadId,
                breakable.lockVersion(),
                Launchpad.State.PLAYER_BROKEN,
                now
            ));
            UUID itemInstanceId = UUID.randomUUID();
            Launchpad fromItem = launchpad(
                UUID.randomUUID(),
                player,
                3,
                64,
                3,
                now
            );
            assertTrue(launchpads.createFromItem(
                fromItem,
                itemInstanceId,
                0,
                now
            ));
            assertFalse(launchpads.createFromItem(
                launchpad(
                    UUID.randomUUID(),
                    player,
                    4,
                    64,
                    4,
                    now
                ),
                itemInstanceId,
                0,
                now
            ));
            assertTrue(launchpads.rollbackCreatedPlacement(
                fromItem,
                itemInstanceId,
                now
            ));
            assertTrue(launchpads.createFromItem(
                launchpad(
                    UUID.randomUUID(),
                    player,
                    4,
                    64,
                    4,
                    now
                ),
                itemInstanceId,
                0,
                now
            ));
        }
    }
    @Test
    void deathPersistAndPermanentCompletionContracts() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            coreFlyway(fixture).migrate();
            flyway(fixture).migrate();
            var dataSource = new org.mariadb.jdbc.MariaDbDataSource(fixture.jdbcUrl());
            dataSource.setUser(fixture.username());
            dataSource.setPassword(fixture.password());
            JdbcTraversalLoadoutRepository loadouts =
                new JdbcTraversalLoadoutRepository(
                    dataSource,
                    new FrontierModuleConfig.LoadoutDefinition(2, true, false)
                );
            Instant now = Instant.parse("2026-08-01T12:00:00Z");
            UUID player = UUID.randomUUID();
            var loadout = loadouts.findOrCreate(player, now);
            List<PendingDelivery> initial =
                loadouts.ensureInitialDeliveries(loadout, now);
            assertEquals(4, initial.size());
            PendingDelivery elytra = initial.stream()
                .filter(value -> value.itemType() == PendingDelivery.ItemType.ELYTRA)
                .findFirst()
                .orElseThrow();
            assertEquals(
                DeliveryCompletion.TRANSITIONED_TO_DELIVERED,
                loadouts.markPermanentDelivered(elytra.deliveryId(), now)
            );
            assertEquals(
                DeliveryCompletion.ALREADY_DELIVERED,
                loadouts.markPermanentDelivered(
                    elytra.deliveryId(),
                    now.plusSeconds(1)
                )
            );
            DeathIdentitySnapshot match = new DeathIdentitySnapshot(
                player,
                TraversalIdentity.ItemType.ELYTRA,
                elytra.identity().itemInstanceId(),
                1,
                1,
                TraversalIdentity.WORLDS_BEYOND
            );
            assertEquals(
                DeathPersistResult.REOPENED_TO_PENDING,
                loadouts.persistDeathSnapshot(match, now.plusSeconds(2))
            );
            assertEquals(
                DeathPersistResult.ALREADY_PENDING,
                loadouts.persistDeathSnapshot(match, now.plusSeconds(3))
            );
            assertEquals(
                DeathPersistResult.STALE_SKIPPED,
                loadouts.persistDeathSnapshot(
                    new DeathIdentitySnapshot(
                        player,
                        TraversalIdentity.ItemType.ELYTRA,
                        UUID.randomUUID(),
                        99,
                        1,
                        TraversalIdentity.WORLDS_BEYOND
                    ),
                    now.plusSeconds(4)
                )
            );
            PendingDelivery pending = loadouts.pending(player).stream()
                .filter(value -> value.itemType() == PendingDelivery.ItemType.ELYTRA)
                .findFirst()
                .orElseThrow();
            assertEquals(
                DeliveryCompletion.TRANSITIONED_TO_DELIVERED,
                loadouts.markPermanentDelivered(
                    pending.deliveryId(),
                    now.plusSeconds(5)
                )
            );
            loadouts.reopenAbsentPermanents(
                player,
                EnumSet.of(TraversalIdentity.ItemType.ELYTRA),
                now.plusSeconds(6)
            );
            assertEquals(1, loadouts.pending(player).stream()
                .filter(value -> value.itemType() == PendingDelivery.ItemType.ELYTRA)
                .count());
            assertTrue(loadouts.reissuePermanent(
                player,
                TraversalIdentity.ItemType.ELYTRA,
                now.plusSeconds(7)
            ));
            PendingDelivery reissued = loadouts.pending(player).stream()
                .filter(value -> value.itemType() == PendingDelivery.ItemType.ELYTRA)
                .findFirst()
                .orElseThrow();
            assertEquals(2, reissued.identity().instanceEpoch());
            try (Connection connection = connection(fixture);
                 PreparedStatement cancel = connection.prepareStatement(
                     "UPDATE wf_frontier_pending_delivery SET state='CANCELLED' "
                         + "WHERE delivery_id=?"
                 )) {
                cancel.setString(1, reissued.deliveryId().toString());
                cancel.executeUpdate();
            }
            assertEquals(
                DeliveryCompletion.CANCELLED,
                loadouts.markPermanentDelivered(
                    reissued.deliveryId(),
                    now.plusSeconds(8)
                )
            );
            assertEquals(
                DeathPersistResult.CANCELLED_OR_OBSOLETE,
                loadouts.persistDeathSnapshot(
                    new DeathIdentitySnapshot(
                        player,
                        TraversalIdentity.ItemType.ELYTRA,
                        reissued.identity().itemInstanceId(),
                        2,
                        1,
                        TraversalIdentity.WORLDS_BEYOND
                    ),
                    now.plusSeconds(9)
                )
            );
            assertEquals(
                DeliveryCompletion.NOT_FOUND,
                loadouts.markPermanentDelivered(UUID.randomUUID(), now.plusSeconds(10))
            );
            UUID launchpadDelivery = UUID.nameUUIDFromBytes(
                ("frontier-initial-launchpad:" + player)
                    .getBytes(StandardCharsets.UTF_8)
            );
            assertEquals(
                DeliveryCompletion.TRANSITIONED_TO_DELIVERED,
                loadouts.markConsumableDelivered(
                    launchpadDelivery,
                    now.plusSeconds(11)
                )
            );
            assertEquals(
                DeliveryCompletion.ALREADY_DELIVERED,
                loadouts.markConsumableDelivered(
                    launchpadDelivery,
                    now.plusSeconds(12)
                )
            );
            try (Connection connection = connection(fixture);
                 PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO wf_frontier_pending_delivery "
                         + "(delivery_id,player_uuid,theme_id,item_type,quantity,"
                         + "idempotency_key,payload_json,state,attempts,created_at,"
                         + "updated_at) VALUES (?,?,?,'ELYTRA',1,?,?,'PENDING',0,?,?)"
                 )) {
                UUID badId = UUID.randomUUID();
                insert.setString(1, badId.toString());
                insert.setString(2, player.toString());
                insert.setString(3, TraversalIdentity.WORLDS_BEYOND);
                insert.setString(4, "bad-malformed:" + badId);
                insert.setString(5, "{\"item_instance_id\":\"bad\"}");
                insert.setTimestamp(6, java.sql.Timestamp.from(now));
                insert.setTimestamp(7, java.sql.Timestamp.from(now));
                insert.executeUpdate();
                assertEquals(
                    DeliveryCompletion.MALFORMED_PAYLOAD,
                    loadouts.markPermanentDelivered(badId, now.plusSeconds(13))
                );
            }
            PendingDelivery hook = loadouts.pending(player).stream()
                .filter(value ->
                    value.itemType() == PendingDelivery.ItemType.GRAPPLING_HOOK
                )
                .findFirst()
                .orElseThrow();
            try (Connection connection = connection(fixture);
                 PreparedStatement delete = connection.prepareStatement(
                     "DELETE FROM wf_frontier_pending_delivery WHERE delivery_id=?"
                 )) {
                delete.setString(1, hook.deliveryId().toString());
                delete.executeUpdate();
            }
            assertEquals(
                DeathPersistResult.PENDING_CREATED,
                loadouts.persistDeathSnapshot(
                    new DeathIdentitySnapshot(
                        player,
                        TraversalIdentity.ItemType.GRAPPLING_HOOK,
                        UUID.nameUUIDFromBytes(
                            (player + ":GRAPPLING_HOOK:1")
                                .getBytes(StandardCharsets.UTF_8)
                        ),
                        1,
                        1,
                        TraversalIdentity.WORLDS_BEYOND
                    ),
                    now.plusSeconds(15)
                )
            );
            JdbcTraversalLoadoutRepository reloaded =
                new JdbcTraversalLoadoutRepository(
                    dataSource,
                    new FrontierModuleConfig.LoadoutDefinition(2, true, false)
                );
            assertTrue(reloaded.pending(player).stream().anyMatch(value ->
                value.itemType() == PendingDelivery.ItemType.GRAPPLING_HOOK
                    && value.state() == PendingDelivery.State.PENDING
            ));
        }
    }

    @Test
    void sameEpochDeliveredPendingCycleAndStaleSkipped() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            coreFlyway(fixture).migrate();
            flyway(fixture).migrate();
            var dataSource = new org.mariadb.jdbc.MariaDbDataSource(fixture.jdbcUrl());
            dataSource.setUser(fixture.username());
            dataSource.setPassword(fixture.password());
            JdbcTraversalLoadoutRepository loadouts =
                new JdbcTraversalLoadoutRepository(
                    dataSource,
                    new FrontierModuleConfig.LoadoutDefinition(2, true, false)
                );
            Instant now = Instant.now();
            UUID player = UUID.randomUUID();
            loadouts.ensureInitialDeliveries(
                loadouts.findOrCreate(player, now),
                now
            );
            PendingDelivery navigation = loadouts.pending(player).stream()
                .filter(value ->
                    value.itemType() == PendingDelivery.ItemType.NAVIGATION
                )
                .findFirst()
                .orElseThrow();
            for (int cycle = 0; cycle < 3; cycle++) {
                assertEquals(
                    DeliveryCompletion.TRANSITIONED_TO_DELIVERED,
                    loadouts.markPermanentDelivered(
                        navigation.deliveryId(),
                        now.plusSeconds(cycle * 2L + 1L)
                    )
                );
                assertEquals(
                    DeathPersistResult.REOPENED_TO_PENDING,
                    loadouts.persistDeathSnapshot(
                        new DeathIdentitySnapshot(
                            player,
                            TraversalIdentity.ItemType.NAVIGATION,
                            navigation.identity().itemInstanceId(),
                            1,
                            1,
                            TraversalIdentity.WORLDS_BEYOND
                        ),
                        now.plusSeconds(cycle * 2L + 2L)
                    )
                );
            }
            assertEquals(
                DeathPersistResult.STALE_SKIPPED,
                loadouts.persistDeathSnapshot(
                    new DeathIdentitySnapshot(
                        player,
                        TraversalIdentity.ItemType.NAVIGATION,
                        navigation.identity().itemInstanceId(),
                        1,
                        2,
                        TraversalIdentity.WORLDS_BEYOND
                    ),
                    now.plusSeconds(20)
                )
            );
        }
    }

    @Test
    void migratesCoreThenFrontierInSameEmptySchemaAndRepeats() throws Exception {
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
                    "wf_frontier_flyway_schema_history",
                    "wf_core_transaction",
                    "wf_frontier_theme_player_state",
                    "wf_frontier_item_instance",
                    "wf_frontier_pending_delivery",
                    "wf_frontier_launchpad",
                    "wf_frontier_launchpad_history",
                    "wf_frontier_purchase",
                    "wf_frontier_placement_transaction"
                )));
                assertTrue(tables.stream().noneMatch(name ->
                    name.startsWith("wf_main_")));
                assertEquals(3, successfulMigrations(
                    connection,
                    "flyway_schema_history"
                ));
                assertEquals(3, successfulMigrations(
                    connection,
                    "wf_frontier_flyway_schema_history"
                ));
            }
        }
    }

    @Test
    void upgradesV001SchemaAndKeepsOwnershipIsolatedFromMain() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            assertEquals(3, coreFlyway(fixture).migrate().migrationsExecuted);
            Flyway first = Flyway.configure()
                .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
                .locations("classpath:db/migration/frontier")
                .table("wf_frontier_flyway_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .target("1")
                .load();
            assertEquals(1, first.migrate().migrationsExecuted);
            assertEquals(1, flyway(fixture).migrate().migrationsExecuted);
            try (Connection connection = connection(fixture)) {
                Set<String> tables = tables(connection);
                assertTrue(tables.contains("wf_frontier_purchase"));
                assertTrue(tables.contains("wf_core_transaction"));
                assertTrue(tables.stream().noneMatch(name -> name.startsWith("wf_main_")));
            }
        }
    }

    @Test
    void migrationFailureIsExceptionalAndNotRecordedSuccessful() throws Exception {
        try (MariaDbContainerFixture fixture = MariaDbContainerFixture.start()) {
            Flyway broken = Flyway.configure()
                .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
                .locations(
                    "classpath:db/migration/frontier",
                    "classpath:db/migration/frontier-failure"
                )
                .table("wf_frontier_flyway_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
            coreFlyway(fixture).migrate();
            flyway(fixture).migrate();
            assertThrows(RuntimeException.class, broken::migrate);
            try (Connection connection = connection(fixture)) {
                assertEquals(3, successfulMigrations(
                    connection,
                    "wf_frontier_flyway_schema_history"
                ));
            }
        }
    }

    private static Flyway flyway(MariaDbContainerFixture fixture) {
        return Flyway.configure()
            .dataSource(fixture.jdbcUrl(), fixture.username(), fixture.password())
            .locations("classpath:db/migration/frontier")
            .table("wf_frontier_flyway_schema_history")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .load();
    }

    private static Launchpad launchpad(
        UUID id,
        UUID player,
        int x,
        int y,
        int z,
        Instant now
    ) {
        return new Launchpad(
            id,
            new Launchpad.Location("frontier_iris", x, y, z),
            0,
            player,
            0,
            3,
            now,
            null,
            now.plus(Duration.ofDays(30)),
            "frontier-v1",
            Launchpad.State.ACTIVE,
            1,
            0
        );
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
