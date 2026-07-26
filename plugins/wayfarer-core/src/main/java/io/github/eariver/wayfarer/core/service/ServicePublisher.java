package io.github.eariver.wayfarer.core.service;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;

public interface ServicePublisher {
    void publish(WayfarerServices services, WayfarerHealth health);
    void unpublish();
}
