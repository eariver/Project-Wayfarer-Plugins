package io.github.eariver.wayfarer.core.command;

public interface CommandAudience {
    boolean hasPermission(String permission);
    boolean console();
    void sendMessage(String message);
}
