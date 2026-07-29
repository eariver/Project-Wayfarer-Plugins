package io.github.eariver.wayfarer.frontier.domain;

import java.util.Map;
import java.util.Optional;

public final class FrontierShopCatalog {
    private static final Map<String, Offer> V002_OFFERS = Map.of(
        "launchpad", new Offer("launchpad", PendingDelivery.ItemType.LAUNCHPAD, 1, 30),
        "firework_rocket", new Offer(
            "firework_rocket", PendingDelivery.ItemType.FIREWORK_ROCKET, 1, 200
        )
    );

    public Optional<Offer> findV002(String offerId) {
        return Optional.ofNullable(V002_OFFERS.get(offerId));
    }

    public record Offer(
        String offerId,
        PendingDelivery.ItemType itemType,
        int quantity,
        long priceWaymark
    ) {
        public Offer {
            if (offerId == null || offerId.isBlank() || itemType == null
                || quantity <= 0 || priceWaymark < 0) {
                throw new IllegalArgumentException("Shop offer is invalid");
            }
        }
    }
}
