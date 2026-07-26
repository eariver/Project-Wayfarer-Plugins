package io.github.eariver.wayfarer.api;

import java.util.Map;

public interface WayfarerHealth {
    Status overall();
    Map<String, Status> components();

    enum Status {
        STARTING, HEALTHY, DEGRADED, FAILED, STOPPING
    }
}
