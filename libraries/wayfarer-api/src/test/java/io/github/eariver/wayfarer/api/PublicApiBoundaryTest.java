package io.github.eariver.wayfarer.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicApiBoundaryTest {
    private static final List<Class<?>> CONTRACTS = List.of(
        WayfarerServices.class,
        WayfarerDatabase.class,
        WayfarerAudit.class,
        WayfarerTransactions.class,
        WayfarerWaymark.class,
        WayfarerWaymarkProvider.class,
        WayfarerItemIdentity.class,
        WayfarerTasks.class,
        WayfarerHealth.class
    );
    private static final Set<String> FORBIDDEN = Set.of(
        "com.zaxxer",
        "org.flywaydb",
        "io.lettuce",
        "java.sql",
        "org.bukkit",
        "io.papermc",
        "redis.clients"
    );

    @Test
    void publicContractsExposeOnlyJdkAndWayfarerApiTypes() {
        for (Class<?> contract : CONTRACTS) {
            inspect(contract);
            for (Class<?> nested : contract.getDeclaredClasses()) {
                inspect(nested);
            }
        }
    }

    private static void inspect(Class<?> type) {
        requireAllowed(type);
        for (Method method : type.getDeclaredMethods()) {
            requireAllowed(method.getGenericReturnType());
            for (Type parameter : method.getGenericParameterTypes()) {
                requireAllowed(parameter);
            }
        }
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                requireAllowed(component.getGenericType());
            }
        }
    }

    private static void requireAllowed(Type type) {
        if (type instanceof ParameterizedType parameterized) {
            requireAllowed(parameterized.getRawType());
            for (Type argument : parameterized.getActualTypeArguments()) {
                requireAllowed(argument);
            }
            return;
        }
        if (type instanceof GenericArrayType array) {
            requireAllowed(array.getGenericComponentType());
            return;
        }
        if (type instanceof WildcardType wildcard) {
            for (Type bound : wildcard.getUpperBounds()) {
                requireAllowed(bound);
            }
            for (Type bound : wildcard.getLowerBounds()) {
                requireAllowed(bound);
            }
            return;
        }
        if (type instanceof TypeVariable<?> variable) {
            for (Type bound : variable.getBounds()) {
                requireAllowed(bound);
            }
            return;
        }
        assertTrue(type instanceof Class<?>, () -> "Unexpected public API type form: " + type);
        Class<?> value = (Class<?>) type;
        while (value.isArray()) {
            value = value.getComponentType();
        }
        if (value.isPrimitive()) {
            return;
        }
        String name = value.getName();
        assertTrue(
            FORBIDDEN.stream().noneMatch(name::startsWith),
            () -> "Forbidden public API type: " + name
        );
        assertTrue(
            name.startsWith("java.") || name.startsWith("io.github.eariver.wayfarer.api."),
            () -> "Unexpected public API type: " + name
        );
    }
}
