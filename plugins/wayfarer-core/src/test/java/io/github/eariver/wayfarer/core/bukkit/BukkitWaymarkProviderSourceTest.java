package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BukkitWaymarkProviderSourceTest {
    @Test
    void discoversOnlyTheSharedApiProviderIdentity() {
        ServicesManager manager = mock(ServicesManager.class);
        WayfarerWaymarkProvider provider = mock(WayfarerWaymarkProvider.class);
        when(manager.load(WayfarerWaymarkProvider.class)).thenReturn(provider);

        BukkitWaymarkProviderSource source = new BukkitWaymarkProviderSource(manager);

        assertSame(provider, source.discover().orElseThrow());
        assertSame(
            WayfarerWaymarkProvider.class.getClassLoader(),
            source.discover().orElseThrow().getClass().getClassLoader()
        );
    }

    @Test
    void reportsAbsentRegistrationWithoutCreatingFallbackProvider() {
        ServicesManager manager = mock(ServicesManager.class);

        assertTrue(new BukkitWaymarkProviderSource(manager).discover().isEmpty());
    }
}
