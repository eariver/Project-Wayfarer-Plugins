package io.github.eariver.wayfarer.frontier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class FrontierMigrationHashTest {
    @Test
    void migrationBytesMatchFixedBaselines() throws Exception {
        Map<String, String> expected = Map.of(
            "/db/migration/frontier/V001__worlds_beyond_schema.sql",
            "e63a9a3d2c6daac52e0e7a43374755a8d11908ad4da1494f711a8d1a1eb182d1",
            "/db/migration/frontier/V002__purchase_and_launchpad_recovery.sql",
            "d3656098fd22c1e93be8722c2f35b0f811e34796ee70ba196d7a1f1ceb31acf7"
        );
        for (var entry : expected.entrySet()) {
            try (InputStream stream = getClass().getResourceAsStream(entry.getKey())) {
                assertNotNull(stream, entry.getKey());
                String actual = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())
                );
                assertEquals(entry.getValue(), actual, entry.getKey());
            }
        }
    }
}
