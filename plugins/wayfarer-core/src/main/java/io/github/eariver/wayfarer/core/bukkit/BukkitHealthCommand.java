package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.core.command.HealthCommandHandler;
import io.github.eariver.wayfarer.core.command.TransactionCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public final class BukkitHealthCommand implements CommandExecutor {
    private final HealthCommandHandler handler;
    private final TransactionCommandHandler transactionHandler;

    public BukkitHealthCommand(
        HealthCommandHandler handler,
        TransactionCommandHandler transactionHandler
    ) {
        this.handler = Objects.requireNonNull(handler, "handler");
        this.transactionHandler = Objects.requireNonNull(
            transactionHandler,
            "transactionHandler"
        );
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] arguments
    ) {
        BukkitCommandAudience audience = new BukkitCommandAudience(sender);
        if (arguments.length >= 2
            && "admin".equalsIgnoreCase(arguments[0])
            && "transaction".equalsIgnoreCase(arguments[1])) {
            return transactionHandler.execute(audience, arguments);
        }
        return handler.execute(audience, arguments);
    }
}
