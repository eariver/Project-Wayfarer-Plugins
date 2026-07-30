package io.github.eariver.wayfarer.frontier.gameplay;

import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LaunchpadRuntimeIndex {
    private final ConcurrentHashMap<Launchpad.Location, UUID> active =
        new ConcurrentHashMap<>();

    public void activate(Launchpad launchpad) {
        active.put(launchpad.location(), launchpad.launchpadId());
    }

    public boolean contains(Launchpad.Location location) {
        return active.containsKey(location);
    }

    public UUID idAt(Launchpad.Location location) {
        return active.get(location);
    }

    public boolean deactivate(
        Launchpad.Location location,
        UUID launchpadId
    ) {
        return active.remove(location, launchpadId);
    }

    public Launchpad.Location locationOf(UUID launchpadId) {
        return active.entrySet().stream()
            .filter(entry -> entry.getValue().equals(launchpadId))
            .map(java.util.Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    public void clear() {
        active.clear();
    }
}
