package com.example.ailabb1.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMemoryServiceTest {

    private final ChatMemoryService memoryService = new ChatMemoryService();

    @Test
    void shouldStoreAndReturnMessagesPerSession() {
        String sessionId = "test-123";

        var message = new OpenRouterClient.OpenRouterMessage("user", "hej");

        memoryService.addMessage(sessionId, message);

        List<OpenRouterClient.OpenRouterMessage> history =
                memoryService.getHistory(sessionId);

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().content()).isEqualTo("hej");
    }

    @Test
    void shouldKeepSeparateSessions() {
        memoryService.addMessage("session-1",
                new OpenRouterClient.OpenRouterMessage("user", "hej"));

        memoryService.addMessage("session-2",
                new OpenRouterClient.OpenRouterMessage("user", "hello"));

        assertThat(memoryService.getHistory("session-1"))
                .hasSize(1);

        assertThat(memoryService.getHistory("session-2"))
                .hasSize(1);
    }

    @Test
    void shouldLimitNumberOfMessagesPerSession() {
        String sessionId = "test-limit";

        // MAX_MESSAGES_PER_SESSION = 10
        for (int i = 0; i < 12; i++) {
            memoryService.addMessage(sessionId,
                    new OpenRouterClient.OpenRouterMessage("user", "msg-" + i));
        }

        List<OpenRouterClient.OpenRouterMessage> history =
                memoryService.getHistory(sessionId);

        assertThat(history).hasSize(10);
        assertThat(history.getFirst().content()).isEqualTo("msg-2");
    }

    @Test
    void shouldReturnEmptyListWhenNoSessionExists() {
        List<OpenRouterClient.OpenRouterMessage> history =
                memoryService.getHistory("does-not-exist");

        assertThat(history).isEmpty();
    }
}
