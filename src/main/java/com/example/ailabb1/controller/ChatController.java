package com.example.ailabb1.controller;

import com.example.ailabb1.dto.ChatRequestDto;
import com.example.ailabb1.dto.ChatResponseDto;
import com.example.ailabb1.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponseDto chat(@Valid @RequestBody ChatRequestDto request) {
        return chatService.chat(request);
    }
}
