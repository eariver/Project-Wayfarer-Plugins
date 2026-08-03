package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.main.application.GrowthToolRepository;
import io.github.eariver.wayfarer.main.application.GrowthSessionStore;
import io.github.eariver.wayfarer.main.config.MainModuleConfig;
import io.github.eariver.wayfarer.main.domain.EvolutionPlan;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.identity.GrowthToolPhysicalClaim;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;

final class MainAuthorityFailClosedOrderingTest {
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-00000000a462");
    private static final UUID TOOL =
        UUID.fromString("00000000-0000-0000-0000-00000000a463");
    private static final UUID OLD_INSTANCE =
        UUID.fromString("00000000-0000-0000-0000-00000000a464");
    private static final UUID NEW_INSTANCE =
        UUID.fromString("00000000-0000-0000-0000-00000000a465");
    private static final Instant NOW = Instant.parse("2026-08-03T00:00:00Z");

    @Test
    void revokeInvalidatesCachedAuthorizationBeforeDatabaseMutationCompletes() {
        Fixture fixture = fixture();
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        fixture.runtime().revoke(PLAYER);

        assertEquals(
            HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void refreshInvalidatesCachedAuthorizationBeforeAuthoritativeReadCompletes() {
        Fixture fixture = fixture();
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        fixture.runtime().refreshSessionFromAuthority(PLAYER);

        assertEquals(
            HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void reissueInvalidatesCachedAuthorizationBeforeDatabaseMutationCompletes() {
        Fixture fixture = fixture();
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        fixture.runtime().reissue(PLAYER);

        assertEquals(
            HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void reissueSuccessInstallsExactNewIdentityAndReauthorizesMainHand() {
        Fixture fixture = fixture();
        GrowthTool oldTool = tool(OLD_INSTANCE, 1, GrowthTool.DeliveryStatus.DELIVERED);
        GrowthTool rotated = oldTool.reissued(NEW_INSTANCE, NOW);
        GrowthTool delivered = new GrowthTool(
            rotated.toolId(),
            rotated.itemInstanceId(),
            rotated.ownerUuid(),
            rotated.instanceEpoch(),
            rotated.cumulativeProgressUnits(),
            rotated.branch(),
            rotated.status(),
            GrowthTool.DeliveryStatus.DELIVERED,
            rotated.storedDamage(),
            rotated.schemaVersion(),
            rotated.displayRevision(),
            rotated.lockVersion() + 1,
            rotated.updatedAt()
        );
        fixture.held().set(null);
        fixture.sessions().open(oldTool);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        when(fixture.repository().findByOwner(PLAYER))
            .thenReturn(Optional.of(oldTool));
        when(fixture.repository().replaceAuthority(
            any(GrowthTool.class),
            anyLong(),
            any(Instant.class)
        )).thenReturn(Optional.of(rotated));
        when(fixture.repository().findOrCreate(
            any(UUID.class),
            any(Instant.class)
        ))
            .thenReturn(delivered);

        CompletionStage<MainGameplayRuntime.AdminMutation> result =
            fixture.runtime().reissue(PLAYER);
        fixture.tasks().completeNext();
        when(fixture.repository().findByOwner(PLAYER))
            .thenReturn(Optional.of(delivered));
        fixture.tasks().completeNext();
        fixture.tasks().completeNext();

        assertEquals(
            MainGameplayRuntime.AdminMutation.APPLIED,
            result.toCompletableFuture().join()
        );
        GrowthTool current = fixture.runtime().current(PLAYER).orElseThrow();
        assertEquals(NEW_INSTANCE, current.itemInstanceId());
        assertEquals(2L, current.instanceEpoch());
        assertEquals(GrowthTool.Status.ACTIVE, current.status());
        assertEquals(
            HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void noChangeRestoresTheAuthoritativeRevokedState() {
        Fixture fixture = fixture();
        GrowthTool revoked = new GrowthTool(
            TOOL,
            OLD_INSTANCE,
            PLAYER,
            1,
            0,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.REVOKED,
            GrowthTool.DeliveryStatus.DELIVERED,
            0,
            1,
            1,
            4,
            NOW
        );
        fixture.held().set(physical(revoked));
        fixture.sessions().open(revoked);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        when(fixture.repository().findByOwner(PLAYER))
            .thenReturn(Optional.of(revoked));

        CompletionStage<MainGameplayRuntime.AdminMutation> result =
            fixture.runtime().revoke(PLAYER);
        fixture.tasks().completeNext();

        assertEquals(
            MainGameplayRuntime.AdminMutation.NO_CHANGE,
            result.toCompletableFuture().join()
        );
        assertEquals(revoked, fixture.runtime().current(PLAYER).orElseThrow());
        assertNotEquals(
            HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void conflictReloadsWinningAuthorityWithoutRestoringOldEpoch() {
        Fixture fixture = fixture();
        GrowthTool oldTool = tool(OLD_INSTANCE, 1, GrowthTool.DeliveryStatus.DELIVERED);
        GrowthTool winner = oldTool.reissued(NEW_INSTANCE, NOW);
        fixture.held().set(null);
        fixture.sessions().open(oldTool);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        when(fixture.repository().findByOwner(PLAYER))
            .thenReturn(Optional.of(oldTool));
        when(fixture.repository().replaceAuthority(
            any(GrowthTool.class),
            anyLong(),
            any(Instant.class)
        )).thenReturn(Optional.empty());

        CompletionStage<MainGameplayRuntime.AdminMutation> result =
            fixture.runtime().revoke(PLAYER);
        fixture.tasks().completeNext();
        when(fixture.repository().findByOwner(PLAYER))
            .thenReturn(Optional.of(winner));
        fixture.tasks().completeNext();

        assertEquals(
            MainGameplayRuntime.AdminMutation.CONFLICT,
            result.toCompletableFuture().join()
        );
        assertEquals(winner, fixture.runtime().current(PLAYER).orElseThrow());
        assertEquals(
            HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void recoverableDatabaseFailureRestoresCurrentAuthority() {
        Fixture fixture = fixture();
        GrowthTool winner = tool(NEW_INSTANCE, 2, GrowthTool.DeliveryStatus.DELIVERED);
        fixture.held().set(null);
        fixture.sessions().open(tool(OLD_INSTANCE, 1, GrowthTool.DeliveryStatus.DELIVERED));
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        when(fixture.repository().findByOwner(PLAYER))
            .thenReturn(Optional.of(winner));

        CompletionStage<MainGameplayRuntime.AdminMutation> result =
            fixture.runtime().revoke(PLAYER);
        fixture.tasks().failNext(new IllegalStateException("database unavailable"));
        fixture.tasks().completeNext();

        assertEquals(
            MainGameplayRuntime.AdminMutation.UNAVAILABLE,
            result.toCompletableFuture().join()
        );
        assertEquals(winner, fixture.runtime().current(PLAYER).orElseThrow());
        assertEquals(
            HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void refreshSuccessInstallsAndAuthorizesTheCurrentAuthority() {
        Fixture fixture = fixture();
        GrowthTool oldTool = tool(OLD_INSTANCE, 1, GrowthTool.DeliveryStatus.DELIVERED);
        GrowthTool current = tool(NEW_INSTANCE, 2, GrowthTool.DeliveryStatus.DELIVERED);
        fixture.held().set(null);
        fixture.sessions().open(oldTool);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        when(fixture.repository().findByOwner(PLAYER))
            .thenReturn(Optional.of(current));

        CompletionStage<Void> result =
            fixture.runtime().refreshSessionFromAuthority(PLAYER);
        fixture.tasks().completeNext();

        result.toCompletableFuture().join();
        assertEquals(current, fixture.runtime().current(PLAYER).orElseThrow());
        assertEquals(
            HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void unrecoverableRefreshFailureRemainsFailClosedAndDropsOldSession() {
        Fixture fixture = fixture();
        GrowthTool oldTool = tool(OLD_INSTANCE, 1, GrowthTool.DeliveryStatus.DELIVERED);
        fixture.held().set(null);
        fixture.sessions().open(oldTool);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        CompletionStage<Void> result =
            fixture.runtime().refreshSessionFromAuthority(PLAYER);
        fixture.tasks().failNext(new IllegalStateException("read failed"));
        fixture.tasks().failNext(new IllegalStateException("recovery failed"));

        result.toCompletableFuture().join();
        assertEquals(
            HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE,
            fixture.authorizations().get(PLAYER).state()
        );
        assertEquals(Optional.empty(), fixture.runtime().current(PLAYER));
    }

    @Test
    void staleValidCacheCannotAuthorizeDifferentItemAtSecurityBoundaries() {
        Fixture fixture = fixture();
        GrowthTool current = tool(OLD_INSTANCE, 1, GrowthTool.DeliveryStatus.DELIVERED);
        GrowthTool staleItemAuthority = tool(
            NEW_INSTANCE,
            2,
            GrowthTool.DeliveryStatus.DELIVERED
        );
        fixture.sessions().open(current);
        fixture.held().set(physical(staleItemAuthority));
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        BlockBreakEvent breakEvent = mock(BlockBreakEvent.class);
        when(breakEvent.getPlayer()).thenReturn(fixture.player());
        fixture.runtime().guardManagedBreak(breakEvent);

        PlayerInteractEvent interactEvent = mock(PlayerInteractEvent.class);
        when(interactEvent.getPlayer()).thenReturn(fixture.player());
        when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
        when(interactEvent.getHand()).thenReturn(EquipmentSlot.HAND);
        fixture.runtime().onInteract(interactEvent);

        assertFalse(
            fixture.runtime().switchBranch(
                fixture.player(),
                GrowthTool.Branch.SILK_TOUCH
            )
        );
        assertEquals(
            Optional.empty(),
            fixture.runtime().repairSnapshot(fixture.player(), current)
        );
        assertFalse(fixture.runtime().applyFullRepair(
            PLAYER,
            current.toolId(),
            current.instanceEpoch(),
            UUID.randomUUID()
        ));
        assertFalse(fixture.runtime().debug(fixture.player(), "durability-one"));
        verify(breakEvent).setCancelled(true);
        verify(interactEvent).setCancelled(true);
    }

    @Test
    void brokenToolBranchMutationRemainsDenied() {
        Fixture fixture = fixture();
        GrowthTool broken = new GrowthTool(
            TOOL,
            OLD_INSTANCE,
            PLAYER,
            1,
            0,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.BROKEN,
            GrowthTool.DeliveryStatus.DELIVERED,
            100,
            1,
            1,
            1,
            NOW
        );
        fixture.sessions().open(broken);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_BROKEN_OWNER
            )
        );

        assertFalse(
            fixture.runtime().switchBranch(
                fixture.player(),
                GrowthTool.Branch.SILK_TOUCH
            )
        );
    }

    @Test
    void ordinaryMainHandIsFullyAuthorizedAsNoManagedItem() throws Exception {
        Fixture fixture = fixture();
        fixture.held().set(null);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        invokePrivate(
            fixture.runtime(),
            "authorizeMainHand",
            new Class<?>[] {Player.class},
            fixture.player()
        );

        assertEquals(
            HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    private static Fixture fixture() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        MainModuleConfig config = mock(MainModuleConfig.class);
        WayfarerServices services = mock(WayfarerServices.class);
        WayfarerAudit audit = mock(WayfarerAudit.class);
        GrowthToolRepository repository = mock(GrowthToolRepository.class);
        ControlledTasks tasks = new ControlledTasks();
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        InventoryView openInventory = mock(InventoryView.class);
        java.util.concurrent.atomic.AtomicReference<ItemStack> held =
            new java.util.concurrent.atomic.AtomicReference<>();

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(
            Logger.getLogger(MainAuthorityFailClosedOrderingTest.class.getName())
        );
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getOnlinePlayers()).thenReturn(Collections.emptyList());
        when(server.getPlayer(PLAYER)).thenReturn(player);
        when(config.checkpointInterval()).thenReturn(Duration.ofMinutes(5));
        when(config.evolutionPlan()).thenReturn(EvolutionPlan.defaults());
        when(config.enchantmentCaps()).thenReturn(
            EvolutionPlan.EnchantmentCaps.defaults()
        );
        when(services.tasks()).thenReturn(tasks);
        when(services.audit()).thenReturn(audit);
        when(services.serverId()).thenReturn("test-main");
        when(audit.record(any(WayfarerAudit.AuditEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        when(player.getUniqueId()).thenReturn(PLAYER);
        when(player.isOnline()).thenReturn(true);
        when(player.getInventory()).thenReturn(inventory);
        when(player.getOpenInventory()).thenReturn(openInventory);
        when(openInventory.getCursor()).thenReturn(null);
        when(inventory.getItemInMainHand()).thenAnswer(ignored -> held.get());
        when(inventory.getStorageContents()).thenAnswer(
            ignored -> held.get() == null
                ? new ItemStack[0]
                : new ItemStack[] {held.get()}
        );
        when(inventory.getContents()).thenAnswer(
            ignored -> held.get() == null
                ? new ItemStack[0]
                : new ItemStack[] {held.get()}
        );
        when(inventory.getArmorContents()).thenReturn(new ItemStack[0]);
        when(inventory.getItemInOffHand()).thenReturn(null);
        when(scheduler.runTaskTimer(
            any(Plugin.class),
            any(Runnable.class),
            anyLong(),
            anyLong()
        )).thenReturn(mock(BukkitTask.class));

        MainGameplayRuntime runtime = new MainGameplayRuntime(
            plugin,
            config,
            services,
            repository,
            java.time.Clock.systemUTC()
        );
        return new Fixture(
            runtime,
            authorizations(runtime),
            sessions(runtime),
            tasks,
            repository,
            held,
            player
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, HeldGrowthToolAuthorization> authorizations(
        MainGameplayRuntime runtime
    ) {
        try {
            var field = MainGameplayRuntime.class.getDeclaredField(
                "heldAuthorizations"
            );
            field.setAccessible(true);
            return (Map<UUID, HeldGrowthToolAuthorization>) field.get(runtime);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    private static GrowthSessionStore sessions(MainGameplayRuntime runtime) {
        try {
            var field = MainGameplayRuntime.class.getDeclaredField("sessions");
            field.setAccessible(true);
            return (GrowthSessionStore) field.get(runtime);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    private static void invokePrivate(
        MainGameplayRuntime runtime,
        String name,
        Class<?>[] parameterTypes,
        Object... arguments
    ) throws Exception {
        var method = MainGameplayRuntime.class.getDeclaredMethod(
            name,
            parameterTypes
        );
        method.setAccessible(true);
        method.invoke(runtime, arguments);
    }

    private static GrowthTool tool(
        UUID itemInstanceId,
        long epoch,
        GrowthTool.DeliveryStatus deliveryStatus
    ) {
        return new GrowthTool(
            TOOL,
            itemInstanceId,
            PLAYER,
            epoch,
            0,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.ACTIVE,
            deliveryStatus,
            0,
            1,
            epoch,
            epoch,
            NOW
        );
    }

    private static ItemStack physical(GrowthTool tool) {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        Material material = mock(Material.class);
        when(material.getMaxDurability()).thenReturn((short) 100);
        Map<NamespacedKey, Object> values = Map.of(
            new NamespacedKey("wayfarer", "item_type"), "GROWTH_TOOL",
            new NamespacedKey("wayfarer", "item_instance_id"),
            tool.itemInstanceId().toString(),
            new NamespacedKey("wayfarer", "tool_id"),
            tool.toolId().toString(),
            new NamespacedKey("wayfarer", "owner_uuid"),
            tool.ownerUuid().toString(),
            new NamespacedKey("wayfarer", "tool_type"), GrowthTool.TOOL_TYPE,
            new NamespacedKey("wayfarer", "instance_epoch"),
            tool.instanceEpoch(),
            new NamespacedKey("wayfarer", "schema_version"),
            tool.schemaVersion(),
            new NamespacedKey("wayfarer", "display_revision"),
            tool.displayRevision()
        );
        PersistentDataContainer pdc = (PersistentDataContainer)
            java.lang.reflect.Proxy.newProxyInstance(
                PersistentDataContainer.class.getClassLoader(),
                new Class<?>[] {PersistentDataContainer.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("has")) {
                        Object value = values.get(args[0]);
                        return value != null && compatible(value, args[1]);
                    }
                    if (method.getName().equals("get")) {
                        Object value = values.get(args[0]);
                        return value != null && compatible(value, args[1])
                            ? value
                            : null;
                    }
                    if (method.getName().equals("toString")) {
                        return "growth-tool-test-pdc";
                    }
                    if (method.getName().equals("hashCode")) {
                        return System.identityHashCode(proxy);
                    }
                    if (method.getName().equals("equals")) {
                        return proxy == args[0];
                    }
                    return null;
                }
            );
        when(item.hasItemMeta()).thenReturn(true);
        when(item.getItemMeta()).thenReturn(meta);
        when(item.getType()).thenReturn(material);
        when(item.getEnchantmentLevel(any())).thenReturn(0);
        when(meta.getPersistentDataContainer()).thenReturn(pdc);
        when(meta.displayName()).thenReturn(
            GrowthToolDeliveryPresentation.displayName(
                GrowthToolPhysicalClaim.ItemType.GROWTH_TOOL
            )
        );
        return item;
    }

    private static boolean compatible(Object value, Object type) {
        return value instanceof String && type == PersistentDataType.STRING
            || value instanceof Long && type == PersistentDataType.LONG
            || value instanceof Integer && type == PersistentDataType.INTEGER;
    }

    private record Fixture(
        MainGameplayRuntime runtime,
        Map<UUID, HeldGrowthToolAuthorization> authorizations,
        GrowthSessionStore sessions,
        ControlledTasks tasks,
        GrowthToolRepository repository,
        java.util.concurrent.atomic.AtomicReference<ItemStack> held,
        Player player
    ) {}

    private static final class ControlledTasks implements WayfarerTasks {
        private final Deque<Pending<?>> pending = new ArrayDeque<>();

        @Override
        public <T> CompletionStage<T> database(Supplier<T> operation) {
            CompletableFuture<T> future = new CompletableFuture<>();
            pending.addLast(new Pending<>(future, operation));
            return future;
        }

        void completeNext() {
            if (pending.isEmpty()) {
                throw new AssertionError("No database operation is pending");
            }
            pending.removeFirst().complete();
        }

        void failNext(Throwable failure) {
            if (pending.isEmpty()) {
                throw new AssertionError("No database operation is pending");
            }
            pending.removeFirst().fail(failure);
        }

        @Override
        public <I, O> CompletionStage<TaskBridgeResult<O>> bridge(
            I immutableRequest,
            Function<? super I, ? extends O> asyncOperation,
            Predicate<? super O> mainThreadRevalidation,
            Consumer<? super O> mainThreadMutation
        ) {
            throw new UnsupportedOperationException("bridge");
        }

        @Override
        public CompletionStage<Void> mainThread(Runnable operation) {
            operation.run();
            return CompletableFuture.completedFuture(null);
        }

        private static final class Pending<T> {
            private final CompletableFuture<T> future;
            private final Supplier<T> operation;

            private Pending(CompletableFuture<T> future, Supplier<T> operation) {
                this.future = future;
                this.operation = operation;
            }

            private void complete() {
                try {
                    future.complete(operation.get());
                } catch (Throwable failure) {
                    future.completeExceptionally(failure);
                }
            }

            private void fail(Throwable failure) {
                future.completeExceptionally(failure);
            }
        }
    }
}
