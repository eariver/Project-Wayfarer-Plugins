package io.github.eariver.wayfarer.core.config;

public interface ConfigView {
    boolean contains(String path);
    Object get(String path);
}
