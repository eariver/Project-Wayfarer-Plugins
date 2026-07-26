package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BukkitServicePublisherTest {
    @Test
    void publishesBothPublicContracts() {
        ServicesManager manager = mock(ServicesManager.class);
        JavaPlugin plugin = mock(JavaPlugin.class);
        WayfarerServices services = mock(WayfarerServices.class);
        WayfarerHealth health = mock(WayfarerHealth.class);
        BukkitServicePublisher publisher = new BukkitServicePublisher(manager, plugin);

        publisher.publish(services, health);

        verify(manager).register(
            WayfarerHealth.class,
            health,
            plugin,
            ServicePriority.Normal
        );
        verify(manager).register(
            WayfarerServices.class,
            services,
            plugin,
            ServicePriority.Normal
        );
    }

    @Test
    void unpublishIsIdempotent() {
        ServicesManager manager = mock(ServicesManager.class);
        JavaPlugin plugin = mock(JavaPlugin.class);
        BukkitServicePublisher publisher = new BukkitServicePublisher(manager, plugin);
        publisher.publish(mock(WayfarerServices.class), mock(WayfarerHealth.class));
        publisher.unpublish();
        assertDoesNotThrow(publisher::unpublish);
        verify(manager, times(1)).unregisterAll(plugin);
    }

    @Test
    void partialRegistrationFailureUnregistersPluginServices() {
        ServicesManager manager = mock(ServicesManager.class);
        JavaPlugin plugin = mock(JavaPlugin.class);
        doThrow(new IllegalStateException("expected"))
            .when(manager)
            .register(
                eq(WayfarerServices.class),
                any(WayfarerServices.class),
                eq(plugin),
                eq(ServicePriority.Normal)
            );
        BukkitServicePublisher publisher = new BukkitServicePublisher(manager, plugin);

        assertThrows(
            IllegalStateException.class,
            () -> publisher.publish(
                mock(WayfarerServices.class),
                mock(WayfarerHealth.class)
            )
        );
        verify(manager).unregisterAll(plugin);
    }
}
