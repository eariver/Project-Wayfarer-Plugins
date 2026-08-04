package io.github.eariver.wayfarer.frontier.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class WorldGuardPlacementBridge {
    public boolean denied(Player player, Location location) {
        var localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        var query = WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .createQuery();
        return !query.testState(
            BukkitAdapter.adapt(location),
            localPlayer,
            Flags.BUILD
        );
    }
}
