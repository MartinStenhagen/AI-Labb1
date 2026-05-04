package com.example.ailabb1.service;

import com.example.ailabb1.dto.ChatRequestDto;
import com.example.ailabb1.dto.ChatResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final OpenRouterClient openRouterClient;
    private final PersonalityService personalityService;
    private final ChatMemoryService chatMemoryService;

    public ChatService(
            OpenRouterClient openRouterClient,
            PersonalityService personalityService,
            ChatMemoryService chatMemoryService
    ) {
        this.openRouterClient = openRouterClient;
        this.personalityService = personalityService;
        this.chatMemoryService = chatMemoryService;
    }

    public ChatResponseDto chat(ChatRequestDto request) {
        String sessionId = request.sessionId();

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        String systemPrompt = personalityService.getSystemPrompt(request.personality());

        List<OpenRouterClient.OpenRouterMessage> messages = new ArrayList<>();
        messages.add(new OpenRouterClient.OpenRouterMessage("system", systemPrompt));
        messages.addAll(chatMemoryService.getHistory(sessionId));
        messages.add(new OpenRouterClient.OpenRouterMessage("user", request.message()));

        String answer = openRouterClient.chat(messages);

        chatMemoryService.addMessage(
                sessionId,
                new OpenRouterClient.OpenRouterMessage("user", request.message())
        );

        chatMemoryService.addMessage(
                sessionId,
                new OpenRouterClient.OpenRouterMessage("assistant", answer)
        );

        return new ChatResponseDto(answer, sessionId);
    }
}
