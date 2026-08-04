package io.github.eariver.wayfarer.main.application;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/** Minimal action dispatcher for the player reissue command boundary. */
public final class ReissueCommandDispatcher {
    private ReissueCommandDispatcher() {
    }

    public static ReissueCommandParser.PlayerAction dispatchPlayer(
        String[] arguments,
        PlayerActionSink sink
    ) {
        Objects.requireNonNull(sink, "sink");
        ReissueCommandParser.PlayerAction action =
            ReissueCommandParser.playerAction(arguments);
        switch (action) {
            case QUOTE -> sink.quote();
            case CONFIRM -> sink.confirm();
            case INVALID -> {
                // The caller owns the usage message for invalid input.
            }
        }
        return action;
    }

    public interface PlayerActionSink {
        void quote();

        void confirm();
    }

    public static Optional<CompletionStage<ReissueCoordinator.Result>> dispatchRecovery(
        String[] arguments,
        RecoveryActionSink sink
    ) {
        Objects.requireNonNull(sink, "sink");
        var route = ReissueCommandParser.recoveryRoute(arguments);
        if (route.isEmpty()) {
            return Optional.empty();
        }
        ReissueCommandParser.RecoveryRoute parsed = route.orElseThrow();
        CompletionStage<ReissueCoordinator.Result> result = switch (parsed.action()) {
            case CONFIRM_PAYMENT -> sink.confirmPayment(parsed.reissueId());
            case RESUME_PAYMENT -> sink.resumePayment(parsed.reissueId());
            case RESUME_ROTATION -> sink.resumeRotation(parsed.reissueId());
            case MARK_FAILED -> sink.markFailed(
                parsed.reissueId(),
                parsed.sanitizedFailureCode()
            );
        };
        return Optional.of(Objects.requireNonNull(result, "result"));
    }

    public interface RecoveryActionSink {
        CompletionStage<ReissueCoordinator.Result> confirmPayment(UUID reissueId);

        CompletionStage<ReissueCoordinator.Result> resumePayment(UUID reissueId);

        CompletionStage<ReissueCoordinator.Result> resumeRotation(UUID reissueId);

        CompletionStage<ReissueCoordinator.Result> markFailed(
            UUID reissueId,
            String failureCode
        );
    }
}
