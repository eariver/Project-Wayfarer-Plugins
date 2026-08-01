package io.github.eariver.wayfarer.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class MainPermissionDescriptorTest {
    @Test
    void declaresExactPermissionDefaultsAndUmbrellaChildren() throws Exception {
        String yaml = resourceText();

        assertPermission(yaml, "wayfarer.main.use", "true");
        assertPermission(yaml, "wayfarer.main.admin.*", "op");
        assertPermission(yaml, "wayfarer.main.admin.read", "false");
        assertPermission(yaml, "wayfarer.main.admin.delivery", "false");
        assertPermission(yaml, "wayfarer.main.admin.modify", "false");
        assertPermission(yaml, "wayfarer.main.admin.reconcile", "false");
        assertPermission(yaml, "wayfarer.main.debug", "op");

        assertEquals(
            Set.of(
                "wayfarer.main.admin.read: true",
                "wayfarer.main.admin.delivery: true",
                "wayfarer.main.admin.modify: true",
                "wayfarer.main.admin.reconcile: true"
            ),
            children(yaml, "wayfarer.main.admin.*")
        );
        assertFalse(hasTopLevelNode(yaml, "wayfarer.main.admin"));
        assertFalse(commandBlock(yaml).stream()
            .anyMatch(line -> line.trim().startsWith("permission:")));
    }

    private static void assertPermission(
        String yaml,
        String permission,
        String defaultValue
    ) {
        List<String> block = block(yaml, permission);
        assertTrue(block.stream().anyMatch(
            line -> line.equals("    default: " + defaultValue)
        ), permission);
        assertTrue(block.stream().anyMatch(
            line -> line.startsWith("    description: ")
                && line.length() > "    description: ".length()
        ), permission);
    }

    private static Set<String> children(String yaml, String permission) {
        return new HashSet<>(block(yaml, permission).stream()
            .filter(line -> line.startsWith("      "))
            .map(String::trim)
            .filter(line -> line.endsWith(": true"))
            .toList());
    }

    private static boolean hasTopLevelNode(String yaml, String node) {
        return normalizedLines(yaml).stream().anyMatch(
            line -> line.equals("  " + node + ":")
        );
    }

    private static List<String> commandBlock(String yaml) {
        return block(yaml, "wayfarer-main");
    }

    private static List<String> block(String yaml, String node) {
        List<String> lines = normalizedLines(yaml);
        String marker = "  " + node + ":";
        int start = lines.indexOf(marker);
        assertTrue(start >= 0, "missing descriptor node " + node);
        int end = lines.size();
        for (int index = start + 1; index < lines.size(); index++) {
            if (lines.get(index).startsWith("  ")
                && !lines.get(index).startsWith("    ")) {
                end = index;
                break;
            }
        }
        return lines.subList(start, end);
    }

    private static List<String> normalizedLines(String yaml) {
        return Arrays.asList(yaml.replace("\r\n", "\n").split("\n"));
    }

    private static String resourceText() throws Exception {
        try (InputStream stream = MainPermissionDescriptorTest.class
            .getResourceAsStream("/plugin.yml")) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
