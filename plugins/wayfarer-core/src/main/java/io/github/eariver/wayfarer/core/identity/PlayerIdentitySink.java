package io.github.eariver.wayfarer.core.identity;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface PlayerIdentitySink {
    CompletionStage<Void> observe(PlayerIdentityObservation observation);
}
