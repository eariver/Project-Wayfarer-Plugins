package io.github.eariver.wayfarer.core.config;

import java.util.Map;

public final class MapConfigView implements ConfigView {
    private final Map<String, ?> values;

    public MapConfigView(Map<String, ?> values) {
        this.values = Map.copyOf(values);
    }

    @Override
    public boolean contains(String path) {
        return values.containsKey(path);
    }

    @Override
    public Object get(String path) {
        return values.get(path);
    }
}
