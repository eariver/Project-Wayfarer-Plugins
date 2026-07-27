package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.core.command.CommandAudience;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import io.github.eariver.wayfarer.core.command.OperationalEvent;

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
    public Optional<UUID> actorUuid() {
        return sender instanceof Player player
            ? Optional.of(player.getUniqueId())
            : Optional.empty();
    }

    @Override
    public OperationalEvent.AudienceKind audienceKind() {
        if (sender instanceof Player) {
            return OperationalEvent.AudienceKind.PLAYER;
        }
        if (sender instanceof ConsoleCommandSender) {
            return OperationalEvent.AudienceKind.CONSOLE;
        }
        return OperationalEvent.AudienceKind.OTHER;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }
}
