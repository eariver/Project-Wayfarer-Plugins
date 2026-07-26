package io.github.eariver.wayfarer.api;

import java.sql.Connection;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface WayfarerDatabase {
    <T> CompletionStage<T> read(Function<Connection, T> operation);
    <T> CompletionStage<T> transaction(Function<Connection, T> operation);
}
