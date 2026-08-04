package io.github.eariver.wayfarer.integration.leafgrapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

final class ReflectiveLeafGrappleBridgeTest {
    @Test
    void reportsMissingDisabledPlugin() {
        Plugin owner = mock(Plugin.class);
        Plugin leaf = mock(Plugin.class);
        when(leaf.isEnabled()).thenReturn(false);

        ReflectiveLeafGrappleBridge bridge = new ReflectiveLeafGrappleBridge(owner, leaf);
        assertEquals(LeafGrappleBridge.Capability.MISSING_PLUGIN, bridge.capability());
        assertThrows(IllegalStateException.class,
            () -> bridge.createHook(UUID.randomUUID(), 1));
    }

    @Test
    void rejectsUnexpectedVersionBeforeLookingAtClasses() {
        Plugin owner = mock(Plugin.class);
        Plugin leaf = mock(Plugin.class);
        PluginMeta meta = mock(PluginMeta.class);
        when(leaf.isEnabled()).thenReturn(true);
        when(leaf.getPluginMeta()).thenReturn(meta);
        when(meta.getVersion()).thenReturn("1.0.3");

        ReflectiveLeafGrappleBridge bridge = new ReflectiveLeafGrappleBridge(owner, leaf);
        assertEquals(LeafGrappleBridge.Capability.UNSUPPORTED_VERSION, bridge.capability());
    }

    @Test
    void rejectsLookalikePluginWithNoObservedPublicApi() {
        Plugin owner = mock(Plugin.class);
        Plugin leaf = mock(Plugin.class);
        PluginMeta meta = mock(PluginMeta.class);
        when(leaf.isEnabled()).thenReturn(true);
        when(leaf.getPluginMeta()).thenReturn(meta);
        when(meta.getVersion()).thenReturn("1.0.2");

        ReflectiveLeafGrappleBridge bridge = new ReflectiveLeafGrappleBridge(owner, leaf);
        assertEquals(LeafGrappleBridge.Capability.MISSING_API, bridge.capability());
    }
}
