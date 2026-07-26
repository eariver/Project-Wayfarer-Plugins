package io.github.eariver.wayfarer.core.task;

@FunctionalInterface
public interface MainThreadDispatcher {
    void dispatch(Runnable operation);
}
