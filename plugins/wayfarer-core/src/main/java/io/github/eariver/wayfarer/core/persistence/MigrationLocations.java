package io.github.eariver.wayfarer.core.persistence;

import java.util.List;

public final class MigrationLocations {
    private MigrationLocations() {}

    public static List<String> canonicalize(List<String> locations) {
        return locations.stream().map(MigrationLocations::canonicalize).toList();
    }

    public static String canonicalize(String location) {
        if (location == null || location.isBlank()) {
            throw new PersistenceException("Migration location must not be blank");
        }
        String path = location.trim();
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isBlank()
            || path.contains("..")
            || path.indexOf('\\') >= 0
            || path.indexOf(':') >= 0) {
            throw new PersistenceException("Migration location is invalid");
        }
        return "classpath:" + path;
    }
}
