package io.github.eariver.wayfarer.core.command;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface OperationalEventSink {
    CompletionStage<Void> record(OperationalEvent event);
}
