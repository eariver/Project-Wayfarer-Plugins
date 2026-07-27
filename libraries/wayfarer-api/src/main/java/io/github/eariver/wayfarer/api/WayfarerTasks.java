package io.github.eariver.wayfarer.api;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface WayfarerTasks {
    <T> CompletionStage<T> database(Supplier<T> operation);

    <I, O> CompletionStage<TaskBridgeResult<O>> bridge(
        I immutableRequest,
        Function<? super I, ? extends O> asyncOperation,
        Predicate<? super O> mainThreadRevalidation,
        Consumer<? super O> mainThreadMutation
    );

    CompletionStage<Void> mainThread(Runnable operation);

    record TaskBridgeResult<T>(T immutableResult, boolean applied) {}
}
