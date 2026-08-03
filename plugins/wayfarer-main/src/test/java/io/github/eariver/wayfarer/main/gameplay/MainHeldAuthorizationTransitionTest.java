package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.main.application.GrowthToolRepository;
import io.github.eariver.wayfarer.main.config.MainModuleConfig;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;
import java.util.Set;

final class MainHeldAuthorizationTransitionTest {
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-00000000a451");

    @Test
    void heldSlotChangeFailsClosedBeforeDeferredAuthorizationRuns()
        throws Exception {
        Fixture fixture = fixture();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(PLAYER);
        when(player.isOnline()).thenReturn(true);

        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        fixture.runtime().onHeldSlot(new PlayerItemHeldEvent(player, 0, 1));

        assertEquals(
            HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE,
            fixture.authorizations().get(PLAYER).state()
        );
    }

    @Test
    void handSwapFailsClosedBeforeDeferredAuthorizationRuns() {
        Fixture fixture = fixture();
        Player player = player();
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        PlayerSwapHandItemsEvent event = mock(PlayerSwapHandItemsEvent.class);
        when(event.getPlayer()).thenReturn(player);
        fixture.runtime().onSwapHands(event);

        assertUnavailable(fixture);
    }

    @Test
    void acceptedInventoryClickFailsClosedBeforeDeferredAuthorizationRuns() {
        Fixture fixture = fixture();
        Player player = player();
        InventoryView view = mock(InventoryView.class);
        Inventory top = mock(Inventory.class);
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getView()).thenReturn(view);
        when(view.getTopInventory()).thenReturn(top);
        when(top.getHolder()).thenReturn(null);
        when(top.getType()).thenReturn(null);
        when(event.getCurrentItem()).thenReturn(null);
        when(event.getCursor()).thenReturn(null);
        when(event.getHotbarButton()).thenReturn(-1);
        when(event.getClickedInventory()).thenReturn(null);
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        fixture.runtime().onInventoryClick(event);

        assertUnavailable(fixture);
    }

    @Test
    void acceptedInventoryDragFailsClosedBeforeDeferredAuthorizationRuns() {
        Fixture fixture = fixture();
        Player player = player();
        InventoryView view = mock(InventoryView.class);
        Inventory top = mock(Inventory.class);
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getView()).thenReturn(view);
        when(view.getTopInventory()).thenReturn(top);
        when(top.getHolder()).thenReturn(null);
        when(top.getType()).thenReturn(null);
        when(top.getSize()).thenReturn(27);
        when(event.getRawSlots()).thenReturn(Set.of());
        when(event.getOldCursor()).thenReturn(null);
        when(event.getNewItems()).thenReturn(Map.of());
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );

        fixture.runtime().onInventoryDrag(event);

        assertUnavailable(fixture);
    }

    @Test
    void dropPickupAndRespawnTransitionsFailClosed() {
        Fixture fixture = fixture();
        Player player = player();
        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        PlayerDropItemEvent drop = mock(PlayerDropItemEvent.class);
        when(drop.getPlayer()).thenReturn(player);
        fixture.runtime().onDrop(drop);
        assertUnavailable(fixture);

        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        EntityPickupItemEvent pickup = mock(EntityPickupItemEvent.class);
        when(pickup.getEntity()).thenReturn(player);
        fixture.runtime().onPickup(pickup);
        assertUnavailable(fixture);

        fixture.authorizations().put(
            PLAYER,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            )
        );
        PlayerRespawnEvent respawn = mock(PlayerRespawnEvent.class);
        when(respawn.getPlayer()).thenReturn(player);
        fixture.runtime().onRespawn(respawn);
        assertUnavailable(fixture);
    }

    private static Player player() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(PLAYER);
        when(player.isOnline()).thenReturn(true);
        return player;
    }

    private static void assertUnavailable(Fixture fixture) {
        assertEquals(
            HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE,
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
        WayfarerTasks tasks = mock(WayfarerTasks.class);
        WayfarerAudit audit = mock(WayfarerAudit.class);
        GrowthToolRepository repository = mock(GrowthToolRepository.class);

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(
            Logger.getLogger(MainHeldAuthorizationTransitionTest.class.getName())
        );
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getOnlinePlayers()).thenReturn(Collections.emptyList());
        when(config.checkpointInterval()).thenReturn(Duration.ofMinutes(5));
        when(services.tasks()).thenReturn(tasks);
        when(services.audit()).thenReturn(audit);
        when(services.serverId()).thenReturn("test-main");
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
        return new Fixture(runtime, authorizations(runtime));
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, HeldGrowthToolAuthorization> authorizations(
        MainGameplayRuntime runtime
    ) {
        try {
            Field field = MainGameplayRuntime.class.getDeclaredField(
                "heldAuthorizations"
            );
            field.setAccessible(true);
            return (ConcurrentHashMap<UUID, HeldGrowthToolAuthorization>)
                field.get(runtime);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    private record Fixture(
        MainGameplayRuntime runtime,
        Map<UUID, HeldGrowthToolAuthorization> authorizations
    ) {}
}
