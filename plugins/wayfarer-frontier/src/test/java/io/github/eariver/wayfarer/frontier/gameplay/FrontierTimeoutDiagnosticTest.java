package io.github.eariver.wayfarer.frontier.gameplay;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.frontier.application.EntryCycleRegistry;
import io.github.eariver.wayfarer.frontier.application.LaunchpadRepository;
import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.application.SafeEntryReadiness;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.integration.MviShareObservation;
import io.github.eariver.wayfarer.integration.leafgrapple.LeafGrappleBridge;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

final class FrontierTimeoutDiagnosticTest {
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-00000000c451");

    @Test
    void terminalTimeoutLogContainsCompleteBoundedObservationEvidence()
        throws Exception {
        AtomicReference<Runnable> polling = new AtomicReference<>();
        Logger logger = Logger.getLogger(
            FrontierTimeoutDiagnosticTest.class.getName()
        );
        logger.setUseParentHandlers(false);
        List<String> messages = new java.util.ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        logger.addHandler(handler);
        try {
            Fixture fixture = fixture(polling, logger);
            SafeEntryReadiness.Request request =
                fixture.readiness().request(PLAYER);
            long cycleId = fixture.entryCycles().beginExternalEntry(PLAYER);
            putEntryContext(fixture.runtime(), request, cycleId);

            invokePrivate(
                fixture.runtime(),
                "scheduleFingerprintReadiness",
                new Class<?>[] {
                    Player.class,
                    SafeEntryReadiness.Request.class,
                    int.class,
                    String.class
                },
                fixture.player(),
                request,
                3,
                "BOUNDED_FINGERPRINT"
            );

            for (int poll = 0; poll < SafeEntryReadiness.MAX_FINGERPRINT_OBSERVATIONS;
                poll++) {
                polling.get().run();
            }

            String timeout = messages.stream()
                .filter(message -> message.contains("decision=TIMEOUT"))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                    "terminal timeout diagnostic was not logged: " + messages
                ));
            assertTrue(timeout.contains("source=BOUNDED_FINGERPRINT"));
            assertTrue(timeout.contains("generation=" + request.generation()));
            assertTrue(timeout.contains("pollCount=40"));
            assertTrue(timeout.contains("visibleManagedItems=0"));
            assertTrue(timeout.contains("requiredManagedItems=3"));
            assertTrue(timeout.contains("fingerprint=1"));
            assertTrue(timeout.contains("decision=TIMEOUT"));
            assertTrue(!timeout.contains(PLAYER.toString()));
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void timeoutThenPublicMviMonitorSchedulesOneRestartOnTheSameCycle()
        throws Exception {
        AtomicReference<Runnable> polling = new AtomicReference<>();
        Logger logger = Logger.getLogger(
            FrontierTimeoutDiagnosticTest.class.getName() + ".late"
        );
        logger.setUseParentHandlers(false);
        Fixture fixture = fixture(polling, logger);
        SafeEntryReadiness.Request request =
            fixture.readiness().request(PLAYER);
        long cycleId = fixture.entryCycles().beginExternalEntry(PLAYER);
        putEntryContext(fixture.runtime(), request, cycleId);
        invokePrivate(
            fixture.runtime(),
            "scheduleFingerprintReadiness",
            new Class<?>[] {
                Player.class,
                SafeEntryReadiness.Request.class,
                int.class,
                String.class
            },
            fixture.player(),
            request,
            3,
            "BOUNDED_FINGERPRINT"
        );
        for (int poll = 0; poll < SafeEntryReadiness.MAX_FINGERPRINT_OBSERVATIONS;
            poll++) {
            polling.get().run();
        }

        MviShareObservation observation = new MviShareObservation(
            fixture.player(),
            "ReadOnlyShareHandlingEvent",
            "resource",
            "frontier_iris",
            0,
            1,
            false,
            true
        );
        invokePrivate(
            fixture.runtime(),
            "onMviObservation",
            new Class<?>[] {MviShareObservation.class},
            observation
        );
        invokePrivate(
            fixture.runtime(),
            "onMviObservation",
            new Class<?>[] {MviShareObservation.class},
            observation
        );

        assertTrue(fixture.lateRestart().get() != null);
        verify(fixture.scheduler(), times(1)).runTask(
            any(Plugin.class),
            any(Runnable.class)
        );
        fixture.lateRestart().get().run();
        assertTrue(fixture.entryCycles().isCurrent(PLAYER, cycleId));
        assertTrue(((Map<?, ?>) field(
            fixture.runtime(),
            "lateEntryContexts"
        )).isEmpty());
    }

    private static Fixture fixture(
        AtomicReference<Runnable> polling,
        Logger logger
    ) {
        JavaPlugin plugin = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        WayfarerServices services = mock(WayfarerServices.class);
        WayfarerTasks tasks = mock(WayfarerTasks.class);
        WayfarerAudit audit = mock(WayfarerAudit.class);
        TraversalLoadoutRepository loadouts = mock(
            TraversalLoadoutRepository.class
        );
        LaunchpadRepository launchpads = mock(LaunchpadRepository.class);
        LeafGrappleBridge leafGrapple = mock(LeafGrappleBridge.class);
        Player player = mock(Player.class);
        World world = mock(World.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        BukkitTask task = mock(BukkitTask.class);
        AtomicReference<Runnable> lateRestart = new AtomicReference<>();

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(logger);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getOnlinePlayers()).thenReturn(Collections.emptyList());
        when(server.getPlayer(PLAYER)).thenReturn(player);
        when(pluginManager.isPluginEnabled(anyString())).thenReturn(false);
        when(scheduler.runTaskTimer(
            any(Plugin.class),
            any(Runnable.class),
            anyLong(),
            anyLong()
        )).thenAnswer(invocation -> {
            polling.set(invocation.getArgument(1));
            return task;
        });
        when(scheduler.runTask(
            any(Plugin.class),
            any(Runnable.class)
        )).thenAnswer(invocation -> {
            lateRestart.set(invocation.getArgument(1));
            return task;
        });
        when(services.tasks()).thenReturn(tasks);
        when(services.audit()).thenReturn(audit);
        when(services.serverId()).thenReturn("test-frontier");
        when(launchpads.findActive(100_000)).thenReturn(List.of());
        when(tasks.database(any())).thenAnswer(invocation ->
            CompletableFuture.completedFuture(
                invocation.<java.util.function.Supplier<?>>getArgument(0).get()
            )
        );
        when(player.getUniqueId()).thenReturn(PLAYER);
        when(player.isOnline()).thenReturn(true);
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("frontier_iris");
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getStorageContents()).thenReturn(new ItemStack[0]);
        when(inventory.getArmorContents()).thenReturn(new ItemStack[0]);
        when(inventory.getItemInOffHand()).thenReturn(null);
        when(player.getItemOnCursor()).thenReturn(null);

        FrontierGameplayRuntime runtime = new FrontierGameplayRuntime(
            plugin,
            defaultConfig(),
            services,
            loadouts,
            launchpads,
            leafGrapple,
            Clock.systemUTC()
        );
        return new Fixture(
            runtime,
            player,
            field(runtime, "entryReadiness"),
            field(runtime, "entryCycles"),
            scheduler,
            lateRestart
        );
    }

    private static FrontierModuleConfig defaultConfig() {
        var stream = FrontierTimeoutDiagnosticTest.class.getResourceAsStream(
            "/config.yml"
        );
        if (stream == null) {
            throw new AssertionError("Default Frontier config is missing");
        }
        return FrontierModuleConfig.load(
            YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
            )
        );
    }

    @SuppressWarnings("unchecked")
    private static void putEntryContext(
        FrontierGameplayRuntime runtime,
        SafeEntryReadiness.Request request,
        long cycleId
    ) throws Exception {
        Class<?> contextType = null;
        for (Class<?> nested : FrontierGameplayRuntime.class.getDeclaredClasses()) {
            if (nested.getSimpleName().equals("EntryContext")) {
                contextType = nested;
                break;
            }
        }
        if (contextType == null) {
            throw new AssertionError("EntryContext is missing");
        }
        Constructor<?> constructor = contextType.getDeclaredConstructor(
            SafeEntryReadiness.Request.class,
            int.class,
            long.class
        );
        constructor.setAccessible(true);
        Object context = constructor.newInstance(request, 3, cycleId);
        Map<UUID, Object> contexts = (Map<UUID, Object>) field(
            runtime,
            "entryContexts"
        );
        contexts.put(PLAYER, context);
    }

    private static void invokePrivate(
        FrontierGameplayRuntime runtime,
        String name,
        Class<?>[] parameterTypes,
        Object... arguments
    ) throws Exception {
        Method method = FrontierGameplayRuntime.class.getDeclaredMethod(
            name,
            parameterTypes
        );
        method.setAccessible(true);
        method.invoke(runtime, arguments);
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(
        FrontierGameplayRuntime runtime,
        String name
    ) {
        try {
            Field field = FrontierGameplayRuntime.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(runtime);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    private record Fixture(
        FrontierGameplayRuntime runtime,
        Player player,
        SafeEntryReadiness readiness,
        EntryCycleRegistry entryCycles,
        BukkitScheduler scheduler,
        AtomicReference<Runnable> lateRestart
    ) {}
}
