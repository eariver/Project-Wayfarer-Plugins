package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BukkitWaymarkProviderSourceTest {
    @Test
    void discoversVaultEconomyAndKeepsConcreteTypesInternal() {
        ServicesManager manager = mock(ServicesManager.class);
        Economy economy = mock(Economy.class);
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(manager.load(Economy.class)).thenReturn(economy);

        BukkitWaymarkProviderSource source = source(manager, player);
        WayfarerWaymarkProvider provider = source.discover().orElseThrow();

        assertInstanceOf(VaultWaymarkProvider.class, provider);
        assertSame(provider, source.discover().orElseThrow());
        assertSame(
            WayfarerWaymarkProvider.class.getClassLoader(),
            provider.getClass().getClassLoader()
        );
    }

    @Test
    void reportsAbsentVaultRegistrationWithoutCreatingFallbackProvider() {
        ServicesManager manager = mock(ServicesManager.class);

        assertTrue(source(manager, mock(OfflinePlayer.class)).discover().isEmpty());
    }

    @Test
    void closeRejectsRediscovery() {
        ServicesManager manager = mock(ServicesManager.class);
        when(manager.load(Economy.class)).thenReturn(mock(Economy.class));
        BukkitWaymarkProviderSource source = source(manager, mock(OfflinePlayer.class));
        source.discover().orElseThrow();

        source.close();

        assertTrue(source.discover().isEmpty());
    }

    private static BukkitWaymarkProviderSource source(
        ServicesManager manager,
        OfflinePlayer player
    ) {
        return new BukkitWaymarkProviderSource(
            manager,
            Runnable::run,
            ignored -> player,
            "RedisEconomy",
            ignored -> {}
        );
    }
}
