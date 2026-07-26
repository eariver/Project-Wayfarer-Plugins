package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.core.command.HealthCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public final class BukkitHealthCommand implements CommandExecutor {
    private final HealthCommandHandler handler;

    public BukkitHealthCommand(HealthCommandHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] arguments
    ) {
        return handler.execute(new BukkitCommandAudience(sender), arguments);
    }
}
