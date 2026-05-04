package com.example.ailabb1.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMemoryService {

    private static final int MAX_MESSAGES_PER_SESSION = 10;

    private final Map<String, List<OpenRouterClient.OpenRouterMessage>> memory = new ConcurrentHashMap<>();

    public List<OpenRouterClient.OpenRouterMessage> getHistory(String sessionId) {
        return new ArrayList<>(memory.getOrDefault(sessionId, List.of()));
    }

    public void addMessage(String sessionId, OpenRouterClient.OpenRouterMessage message) {
        List<OpenRouterClient.OpenRouterMessage> messages =
                memory.computeIfAbsent(sessionId, key -> new ArrayList<>());

        messages.add(message);

        if (messages.size() > MAX_MESSAGES_PER_SESSION) {
            messages.removeFirst();
        }
    }
}
