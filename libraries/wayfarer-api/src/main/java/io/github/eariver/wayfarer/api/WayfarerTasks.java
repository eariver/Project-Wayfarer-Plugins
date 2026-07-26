package io.github.eariver.wayfarer.api;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface WayfarerTasks {
    <T> CompletionStage<T> database(Supplier<T> operation);
    CompletionStage<Void> mainThread(Runnable operation);
}
