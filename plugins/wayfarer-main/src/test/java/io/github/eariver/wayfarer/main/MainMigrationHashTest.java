package io.github.eariver.wayfarer.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class MainMigrationHashTest {
    @Test
    void migrationBytesMatchFixedBaselines() throws Exception {
        Map<String, String> expected = Map.of(
            "/db/migration/main/V001__growth_tool_schema.sql",
            "845738e92e7629f53f088c73ccdf6165e4a87fac4c50efb1690a365e8e7b1668",
            "/db/migration/main/V002__growth_tool_repair_recovery.sql",
            "695f2169556925c8ce3cfd256352f5cc4b70c3afd5d393aec80095bf44b05b87",
            "/db/migration/main/V003__growth_tool_physical_identity.sql",
            "90935b57c0f4485675b064e393448ade95538e66fb7800d97af086526c061021"
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
