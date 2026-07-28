package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.core.command.CommandAudience;
import io.github.eariver.wayfarer.core.command.HealthCommandHandler;
import io.github.eariver.wayfarer.core.command.TransactionCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BukkitHealthCommandTest {
    @Test
    void routesTransactionSubcommandsToTransactionHandler() {
        HealthCommandHandler health = mock(HealthCommandHandler.class);
        TransactionCommandHandler transactions = mock(TransactionCommandHandler.class);
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        String[] arguments = {
            "admin",
            "transaction",
            "inspect",
            "00000000-0000-0000-0000-000000000000"
        };
        when(transactions.execute(any(CommandAudience.class), same(arguments)))
            .thenReturn(true);

        new BukkitHealthCommand(health, transactions)
            .onCommand(sender, command, "wayfarer", arguments);

        verify(transactions).execute(any(CommandAudience.class), same(arguments));
        verifyNoInteractions(health);
    }

    @Test
    void routesOtherSubcommandsToHealthHandler() {
        HealthCommandHandler health = mock(HealthCommandHandler.class);
        TransactionCommandHandler transactions = mock(TransactionCommandHandler.class);
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        String[] arguments = {"admin", "health"};
        when(health.execute(any(CommandAudience.class), same(arguments))).thenReturn(true);

        new BukkitHealthCommand(health, transactions)
            .onCommand(sender, command, "wayfarer", arguments);

        verify(health).execute(any(CommandAudience.class), same(arguments));
        verifyNoInteractions(transactions);
    }
}
