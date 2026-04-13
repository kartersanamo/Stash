package com.kartersanamo.stash.api.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager {
    private final Map<UUID, PendingInput> pendingInputs = new HashMap<>();

    public void await(UUID playerId, String prompt, Consumer<String> callback) {
        pendingInputs.put(playerId, new PendingInput(prompt, callback));
    }

    public PendingInput get(UUID playerId) {
        return pendingInputs.get(playerId);
    }

    public void clear(UUID playerId) {
        pendingInputs.remove(playerId);
    }

    public record PendingInput(String prompt, Consumer<String> callback) {
    }
}
