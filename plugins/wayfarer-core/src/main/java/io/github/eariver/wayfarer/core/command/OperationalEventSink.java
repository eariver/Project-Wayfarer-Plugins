package io.github.eariver.wayfarer.core.command;

@FunctionalInterface
public interface OperationalEventSink {
    void record(String eventType);
}
