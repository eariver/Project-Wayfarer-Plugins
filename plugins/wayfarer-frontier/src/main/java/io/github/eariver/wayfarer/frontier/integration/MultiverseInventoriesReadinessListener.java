package io.github.eariver.wayfarer.frontier.integration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.mvplugins.multiverse.inventories.event.GameModeChangeShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.ReadOnlyShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.WriteOnlyShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.WorldChangeShareHandlingEvent;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Optional public MVI integration. The class is only loaded and registered
 * when Multiverse-Inventories is enabled; the Frontier core path therefore
 * remains valid when the soft dependency is absent.
 */
public final class MultiverseInventoriesReadinessListener implements Listener {
    private final Consumer<MviShareObservation> observer;

    public MultiverseInventoriesReadinessListener(
        Consumer<MviShareObservation> observer
    ) {
        this.observer = Objects.requireNonNull(observer, "observer");
    }

    /*
     * ShareHandlingEvent is an abstract common base in MVI 5.3.5 and does
     * not expose Bukkit's static getHandlerList().  Bukkit therefore cannot
     * register a handler whose parameter is the base type.  Register each
     * concrete public event instead; the event payload is still consumed via
     * the public ShareHandlingEvent API.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadOnlyShareBegin(ReadOnlyShareHandlingEvent event) {
        observer.accept(observation(event, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onReadOnlyShareObserved(ReadOnlyShareHandlingEvent event) {
        observer.accept(observation(event, true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWriteOnlyShareBegin(WriteOnlyShareHandlingEvent event) {
        observer.accept(observation(event, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWriteOnlyShareObserved(WriteOnlyShareHandlingEvent event) {
        observer.accept(observation(event, true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChangeShareBegin(WorldChangeShareHandlingEvent event) {
        observer.accept(observation(event, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChangeShareObserved(WorldChangeShareHandlingEvent event) {
        observer.accept(observation(event, true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameModeChangeShareBegin(GameModeChangeShareHandlingEvent event) {
        observer.accept(observation(event, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChangeShareObserved(GameModeChangeShareHandlingEvent event) {
        observer.accept(observation(event, true));
    }

    private static MviShareObservation observation(
        ShareHandlingEvent event,
        boolean monitorPhase
    ) {
        String fromWorld = null;
        String toWorld = event.getPlayer().getWorld().getName();
        if (event instanceof WorldChangeShareHandlingEvent worldChange) {
            fromWorld = worldChange.getFromWorld();
            toWorld = worldChange.getToWorld();
        }
        return new MviShareObservation(
            event.getPlayer(),
            event.getClass().getSimpleName(),
            fromWorld,
            toWorld,
            event.getWriteProfiles().size(),
            event.getReadProfiles().size(),
            event.isCancelled(),
            monitorPhase
        );
    }

}
