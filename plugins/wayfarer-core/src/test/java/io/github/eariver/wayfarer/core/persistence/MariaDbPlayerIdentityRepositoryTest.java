package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class MariaDbPlayerIdentityRepositoryTest {
    @Test
    void rejectsInvalidNameAndServerBeforeDatabaseSubmission() {
        InternalDatabase database = mock(InternalDatabase.class);
        MariaDbPlayerIdentityRepository repository =
            new MariaDbPlayerIdentityRepository(database);
        UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        assertThrows(
            IllegalArgumentException.class,
            () -> repository.upsert(new PlayerIdentityObservation(
                playerId,
                "invalid name",
                "test-server",
                Instant.EPOCH
            ))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> repository.upsert(new PlayerIdentityObservation(
                playerId,
                "PlayerOne",
                "invalid server",
                Instant.EPOCH
            ))
        );
        verifyNoInteractions(database);
    }
}
