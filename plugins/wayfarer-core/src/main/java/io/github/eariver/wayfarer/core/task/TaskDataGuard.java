package io.github.eariver.wayfarer.core.task;

import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;

final class TaskDataGuard {
    private static final Set<Class<?>> SCALARS = Set.of(
        Boolean.class,
        Byte.class,
        Character.class,
        Double.class,
        Duration.class,
        Float.class,
        Instant.class,
        Integer.class,
        Long.class,
        Short.class,
        String.class,
        UUID.class
    );

    private TaskDataGuard() {}

    static void requireImmutable(Object value, String field) {
        inspect(value, field, new IdentityHashMap<>());
    }

    private static void inspect(
        Object value,
        String field,
        IdentityHashMap<Object, Boolean> visited
    ) {
        if (value == null) {
            return;
        }
        Class<?> type = value.getClass();
        if (SCALARS.contains(type) || type.isEnum()) {
            rejectBukkit(type, field);
            return;
        }
        rejectBukkit(type, field);
        if (!type.isRecord()) {
            throw new IllegalArgumentException(
                field + " must be a JDK-only immutable scalar, enum, or record"
            );
        }
        if (visited.put(value, Boolean.TRUE) != null) {
            return;
        }
        for (RecordComponent component : type.getRecordComponents()) {
            requireImmutableType(
                component.getType(),
                field + "." + component.getName(),
                new HashSet<>()
            );
            try {
                inspect(
                    component.getAccessor().invoke(value),
                    field + "." + component.getName(),
                    visited
                );
            } catch (ReflectiveOperationException failure) {
                throw new IllegalArgumentException(field + " record cannot be inspected");
            }
        }
    }

    private static void requireImmutableType(
        Class<?> type,
        String field,
        Set<Class<?>> visited
    ) {
        rejectBukkit(type, field);
        if (type.isPrimitive() || SCALARS.contains(type) || type.isEnum()) {
            return;
        }
        if (!type.isRecord() || !visited.add(type)) {
            if (!type.isRecord()) {
                throw new IllegalArgumentException(
                    field + " must use an immutable record component type"
                );
            }
            return;
        }
        for (RecordComponent component : type.getRecordComponents()) {
            requireImmutableType(
                component.getType(),
                field + "." + component.getName(),
                visited
            );
        }
    }

    private static void rejectBukkit(Class<?> type, String field) {
        String packageName = type.getPackageName();
        if (packageName.equals("org.bukkit") || packageName.startsWith("org.bukkit.")) {
            throw new IllegalArgumentException(field + " must not retain a Bukkit object");
        }
    }
}
