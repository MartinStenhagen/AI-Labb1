package com.example.ailabb1.controller;

import com.example.ailabb1.dto.ChatResponseDto;
import com.example.ailabb1.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void chat_shouldReturnResponse_whenRequestIsValid() throws Exception {
        when(chatService.chat(any()))
                .thenReturn(new ChatResponseDto("Hello world!", "test-123"));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "personality": "helper",
                                  "message": "Säg hello world",
                                  "sessionId": "test-123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Hello world!"))
                .andExpect(jsonPath("$.sessionId").value("test-123"));
    }

    @Test
    void chat_shouldReturnBadRequest_whenMessageIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "personality": "helper",
                                  "sessionId": "test-123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
