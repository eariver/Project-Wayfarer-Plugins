package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.core.command.CommandAudience;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Objects;

final class BukkitCommandAudience implements CommandAudience {
    private final CommandSender sender;

    BukkitCommandAudience(CommandSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public boolean console() {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }
}
