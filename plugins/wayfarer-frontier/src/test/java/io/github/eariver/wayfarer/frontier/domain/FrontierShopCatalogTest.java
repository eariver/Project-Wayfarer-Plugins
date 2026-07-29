package io.github.eariver.wayfarer.frontier.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class FrontierShopCatalogTest {
    private final FrontierShopCatalog catalog = new FrontierShopCatalog();

    @Test
    void exposesOnlyV002OffersAndRejectsWaystoneBeforeDebit() {
        assertEquals(30, catalog.findV002("launchpad").orElseThrow().priceWaymark());
        assertEquals(200, catalog.findV002("firework_rocket").orElseThrow().priceWaymark());
        assertTrue(catalog.findV002("waystone_placement_tool").isEmpty());
        assertTrue(catalog.findV002("waystone").isEmpty());
    }
}
