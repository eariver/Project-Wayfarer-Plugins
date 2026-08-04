package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.ReissueOperation;
import java.util.Objects;

public record PrepareOutcome(
    PrepareResult result,
    ReissueOperation operation
) {
    public PrepareOutcome {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(operation, "operation");
    }
}
