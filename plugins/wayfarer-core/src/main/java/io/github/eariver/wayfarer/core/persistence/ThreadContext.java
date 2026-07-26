package io.github.eariver.wayfarer.core.persistence;

@FunctionalInterface
public interface ThreadContext {
    boolean isMainThread();
}
