package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.common.secret.SecretValue;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

final class AuditSanitizer {
    static final int MAX_DETAILS_BYTES = 16 * 1024;
    private static final Set<String> SENSITIVE_MARKERS = Set.of(
        "authorization", "cookie", "credential", "jdbc", "password", "secret",
        "token", "url", "username", "uri"
    );
    private static final Pattern FORBIDDEN_VALUE = Pattern.compile(
        "(?i)(?:jdbc:[a-z0-9]+:|rediss?://|authorization\\s*[:=]|bearer\\s+)"
    );

    private AuditSanitizer() {}

    static String validate(String detailsJson, SecretValue... secrets) {
        if (detailsJson == null) {
            return null;
        }
        if (detailsJson.isBlank()) {
            throw new AuditValidationException("Audit details must be valid JSON");
        }
        if (detailsJson.getBytes(StandardCharsets.UTF_8).length > MAX_DETAILS_BYTES) {
            throw new AuditValidationException("Audit details exceed the size limit");
        }
        String redacted = detailsJson;
        for (SecretValue secret : secrets) {
            if (secret != null) {
                String next = secret.redact(redacted);
                if (!next.equals(redacted)) {
                    throw new AuditValidationException("Audit details contain sensitive data");
                }
            }
        }
        JsonScanner scanner = new JsonScanner(detailsJson, secrets);
        scanner.scan();
        return detailsJson;
    }

    static void validateText(String value, SecretValue... secrets) {
        if (FORBIDDEN_VALUE.matcher(value).find()) {
            throw new AuditValidationException("Audit field contains sensitive data");
        }
        for (SecretValue secret : secrets) {
            if (secret != null && !secret.redact(value).equals(value)) {
                throw new AuditValidationException("Audit field contains sensitive data");
            }
        }
    }

    private static boolean sensitiveKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        if (normalized.contains("authorization")
            || normalized.contains("cookie")
            || normalized.contains("credential")
            || normalized.contains("jdbc")
            || normalized.contains("password")
            || normalized.contains("secret")
            || normalized.contains("token")
            || normalized.contains("username")) {
            return true;
        }
        for (String token : normalized.split("[^a-z0-9]+")) {
            if (SENSITIVE_MARKERS.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static final class JsonScanner {
        private final String input;
        private final SecretValue[] secrets;
        private int offset;

        private JsonScanner(String input, SecretValue[] secrets) {
            this.input = input;
            this.secrets = secrets.clone();
        }

        void scan() {
            whitespace();
            value();
            whitespace();
            if (offset != input.length()) {
                invalid();
            }
        }

        private void value() {
            whitespace();
            if (offset >= input.length()) {
                invalid();
            }
            switch (input.charAt(offset)) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> checkValue(string());
                case 't' -> literal("true");
                case 'f' -> literal("false");
                case 'n' -> literal("null");
                default -> number();
            }
        }

        private void object() {
            offset++;
            whitespace();
            if (take('}')) {
                return;
            }
            while (true) {
                whitespace();
                if (offset >= input.length() || input.charAt(offset) != '"') {
                    invalid();
                }
                String key = string();
                if (sensitiveKey(key)) {
                    throw new AuditValidationException(
                        "Audit details contain a sensitive key"
                    );
                }
                whitespace();
                require(':');
                value();
                whitespace();
                if (take('}')) {
                    return;
                }
                require(',');
            }
        }

        private void array() {
            offset++;
            whitespace();
            if (take(']')) {
                return;
            }
            while (true) {
                value();
                whitespace();
                if (take(']')) {
                    return;
                }
                require(',');
            }
        }

        private String string() {
            require('"');
            StringBuilder decoded = new StringBuilder();
            while (offset < input.length()) {
                char current = input.charAt(offset++);
                if (current == '"') {
                    return decoded.toString();
                }
                if (current < 0x20) {
                    invalid();
                }
                if (current != '\\') {
                    decoded.append(current);
                    continue;
                }
                if (offset >= input.length()) {
                    invalid();
                }
                char escaped = input.charAt(offset++);
                switch (escaped) {
                    case '"', '\\', '/' -> decoded.append(escaped);
                    case 'b' -> decoded.append('\b');
                    case 'f' -> decoded.append('\f');
                    case 'n' -> decoded.append('\n');
                    case 'r' -> decoded.append('\r');
                    case 't' -> decoded.append('\t');
                    case 'u' -> decoded.append(unicode());
                    default -> invalid();
                }
            }
            invalid();
            return "";
        }

        private char unicode() {
            if (offset + 4 > input.length()) {
                invalid();
            }
            try {
                char value = (char) Integer.parseInt(input.substring(offset, offset + 4), 16);
                offset += 4;
                return value;
            } catch (NumberFormatException failure) {
                invalid();
                return 0;
            }
        }

        private void checkValue(String value) {
            if (FORBIDDEN_VALUE.matcher(value).find()) {
                throw new AuditValidationException("Audit details contain sensitive data");
            }
            for (SecretValue secret : secrets) {
                if (secret != null && !secret.redact(value).equals(value)) {
                    throw new AuditValidationException(
                        "Audit details contain sensitive data"
                    );
                }
            }
        }

        private void number() {
            int start = offset;
            take('-');
            if (take('0')) {
                // A zero integer part is complete.
            } else {
                digits(true);
            }
            if (take('.')) {
                digits(true);
            }
            if (take('e') || take('E')) {
                if (!take('+')) {
                    take('-');
                }
                digits(true);
            }
            if (start == offset) {
                invalid();
            }
        }

        private void digits(boolean required) {
            int start = offset;
            while (offset < input.length() && Character.isDigit(input.charAt(offset))) {
                offset++;
            }
            if (required && start == offset) {
                invalid();
            }
        }

        private void literal(String literal) {
            if (!input.startsWith(literal, offset)) {
                invalid();
            }
            offset += literal.length();
        }

        private void whitespace() {
            while (offset < input.length()
                && (input.charAt(offset) == ' '
                    || input.charAt(offset) == '\t'
                    || input.charAt(offset) == '\r'
                    || input.charAt(offset) == '\n')) {
                offset++;
            }
        }

        private boolean take(char expected) {
            if (offset < input.length() && input.charAt(offset) == expected) {
                offset++;
                return true;
            }
            return false;
        }

        private void require(char expected) {
            if (!take(expected)) {
                invalid();
            }
        }

        private static void invalid() {
            throw new AuditValidationException("Audit details must be valid JSON");
        }
    }
}
