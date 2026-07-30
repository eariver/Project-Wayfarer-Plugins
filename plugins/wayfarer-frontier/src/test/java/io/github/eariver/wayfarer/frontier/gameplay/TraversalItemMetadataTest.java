package io.github.eariver.wayfarer.frontier.gameplay;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

final class TraversalItemMetadataTest {
    @Test
    void elytraMetadataIsUnbreakable() {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        when(item.getItemMeta()).thenReturn(meta);

        FrontierGameplayRuntime.configureUnbreakable(item);

        verify(meta).setUnbreakable(true);
        verify(item).setItemMeta(meta);
    }

    @Test
    void rocketMetadataIsExactlyFlightDurationThree() {
        ItemStack item = mock(ItemStack.class);
        FireworkMeta meta = mock(FireworkMeta.class);
        when(item.getItemMeta()).thenReturn(meta);

        FrontierGameplayRuntime.configureRocket(item, 3);

        verify(meta).setPower(3);
        verify(item).setItemMeta(meta);
        assertThrows(
            IllegalArgumentException.class,
            () -> FrontierGameplayRuntime.configureRocket(item, 2)
        );
    }
}
