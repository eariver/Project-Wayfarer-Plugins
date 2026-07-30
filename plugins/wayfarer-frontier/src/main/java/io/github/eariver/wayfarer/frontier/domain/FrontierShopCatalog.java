package io.github.eariver.wayfarer.frontier.domain;

import java.util.Map;
import java.util.Optional;

public final class FrontierShopCatalog {
    private final Map<String, Offer> offers;

    public FrontierShopCatalog() {
        this(Map.of(
        "launchpad", new Offer("launchpad", PendingDelivery.ItemType.LAUNCHPAD, 1, 30, 0),
        "firework_rocket", new Offer(
            "firework_rocket", PendingDelivery.ItemType.FIREWORK_ROCKET, 1, 200, 3
        )
        ));
    }

    public FrontierShopCatalog(Map<String, Offer> offers) {
        this.offers = Map.copyOf(offers);
        if (this.offers.isEmpty()
            || this.offers.entrySet().stream().anyMatch(entry ->
                !entry.getKey().equals(entry.getValue().offerId()))) {
            throw new IllegalArgumentException("Shop catalog is invalid");
        }
    }

    public Optional<Offer> findV002(String offerId) {
        return Optional.ofNullable(offers.get(offerId));
    }

    public record Offer(
        String offerId,
        PendingDelivery.ItemType itemType,
        int quantity,
        long priceWaymark,
        int flightDuration
    ) {
        public Offer(
            String offerId,
            PendingDelivery.ItemType itemType,
            int quantity,
            long priceWaymark
        ) {
            this(
                offerId,
                itemType,
                quantity,
                priceWaymark,
                itemType == PendingDelivery.ItemType.FIREWORK_ROCKET ? 3 : 0
            );
        }

        public Offer {
            if (offerId == null || offerId.isBlank() || itemType == null
                || quantity <= 0 || priceWaymark < 0 || flightDuration < 0
                || flightDuration > 3
                || (itemType == PendingDelivery.ItemType.FIREWORK_ROCKET
                    && flightDuration == 0)
                || (itemType != PendingDelivery.ItemType.FIREWORK_ROCKET
                    && flightDuration != 0)) {
                throw new IllegalArgumentException("Shop offer is invalid");
            }
        }
    }
}
