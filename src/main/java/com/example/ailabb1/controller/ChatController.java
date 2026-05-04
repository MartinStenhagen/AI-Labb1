package com.example.ailabb1.controller;

import com.example.ailabb1.dto.ChatRequestDto;
import com.example.ailabb1.dto.ChatResponseDto;
import com.example.ailabb1.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Endpoint för att skicka meddelanden till AI-tjänsten")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(
            summary = "Skicka ett chattmeddelande",
            description = "Tar emot användarens fråga, vald personlighet och valfritt sessionId för minne."
    )
    @PostMapping
    public ChatResponseDto chat(@Valid @RequestBody ChatRequestDto request) {
        return chatService.chat(request);
    }
}
