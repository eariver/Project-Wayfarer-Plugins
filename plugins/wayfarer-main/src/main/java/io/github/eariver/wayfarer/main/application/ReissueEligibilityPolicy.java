package io.github.eariver.wayfarer.main.application;

import java.util.Set;
import java.util.function.Predicate;

/** Pure allowlist and physical-region policy for the Bukkit adapter. */
public final class ReissueEligibilityPolicy {
    private static final Set<String> ALLOWED_WORLDS = Set.of(
        "resource",
        "resource_nether",
        "resource_end"
    );

    private ReissueEligibilityPolicy() {
    }

    public static boolean isAllowedWorld(String exactWorldName) {
        return ALLOWED_WORLDS.contains(exactWorldName);
    }

    public static <T> PhysicalItemPresence scan(
        T[] storage,
        T[] armor,
        T offhand,
        T cursor,
        Predicate<? super T> currentItem
    ) {
        return new PhysicalItemPresence(
            contains(storage, currentItem),
            contains(armor, currentItem),
            isCurrent(offhand, currentItem),
            isCurrent(cursor, currentItem)
        );
    }

    public static PhysicalItemPresence failClosedPresence() {
        return new PhysicalItemPresence(true, true, true, true);
    }

    private static <T> boolean contains(
        T[] items,
        Predicate<? super T> currentItem
    ) {
        if (items == null) {
            return false;
        }
        for (T item : items) {
            if (isCurrent(item, currentItem)) {
                return true;
            }
        }
        return false;
    }

    private static <T> boolean isCurrent(
        T item,
        Predicate<? super T> currentItem
    ) {
        return item != null && currentItem.test(item);
    }
}
