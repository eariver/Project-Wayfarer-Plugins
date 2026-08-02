package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

final class MainGameplayRuntimePersistenceTest {
    private static final NamespacedKey KEY =
        new NamespacedKey("wayfarer", "schema_version");

    @Test
    void readsIntegerTagWithoutRequestingItAsLongFirst() {
        PersistentDataContainer pdc = containerWith(PersistentDataType.INTEGER, 3);

        assertEquals(3L, MainGameplayRuntime.number(pdc, KEY));
    }

    @Test
    void readsLongTag() {
        PersistentDataContainer pdc = containerWith(PersistentDataType.LONG, 7L);

        assertEquals(7L, MainGameplayRuntime.number(pdc, KEY));
    }

    @Test
    void returnsSentinelWhenNumericTagIsAbsent() {
        PersistentDataContainer pdc = containerWith(null, null);

        assertEquals(Long.MIN_VALUE, MainGameplayRuntime.number(pdc, KEY));
    }

    private static PersistentDataContainer containerWith(
        PersistentDataType<?, ?> type,
        Object value
    ) {
        return (PersistentDataContainer) Proxy.newProxyInstance(
            PersistentDataContainer.class.getClassLoader(),
            new Class<?>[] {PersistentDataContainer.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "has" -> type != null && type == args[1];
                case "get" -> type == args[1] ? value : null;
                case "toString" -> "test-pdc";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );
    }
}
