package io.github.eariver.wayfarer.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PublicApiBoundaryTest {
    private static final Set<String> FORBIDDEN = Set.of(
        "java.sql.",
        "javax.sql.",
        "com.zaxxer.",
        "org.flywaydb.",
        "org.bukkit.",
        "io.papermc.",
        "net.kyori.",
        "io.github.eariver.wayfarer.core."
    );
    private static final List<Class<?>> API_TYPES = List.of(
        WayfarerAudit.class,
        WayfarerDatabase.class,
        WayfarerHealth.class,
        WayfarerItemIdentity.class,
        WayfarerLifecycleState.class,
        WayfarerServices.class,
        WayfarerTasks.class,
        WayfarerTransactions.class,
        WayfarerWaymark.class
    );

    @Test
    void publicContractsDoNotExposeJdbcPaperOrCoreImplementationTypes() {
        for (Class<?> apiType : API_TYPES) {
            inspect(apiType);
            for (Class<?> nested : apiType.getDeclaredClasses()) {
                inspect(nested);
            }
        }
    }

    private static void inspect(Class<?> type) {
        for (var method : type.getDeclaredMethods()) {
            reject(method.getGenericReturnType());
            for (Type parameter : method.getGenericParameterTypes()) {
                reject(parameter);
            }
        }
        if (type.isRecord()) {
            for (var component : type.getRecordComponents()) {
                reject(component.getGenericType());
            }
        }
    }

    private static void reject(Type type) {
        String name = type.getTypeName();
        assertFalse(
            FORBIDDEN.stream().anyMatch(name::contains),
            () -> "Forbidden public API type: " + name
        );
    }
}
