package com.example.ailabb1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class OpenRouterClient {

    private final WebClient webClient;
    private final String model;

    public OpenRouterClient(
            WebClient.Builder webClientBuilder,
            @Value("${ai.base-url}") String baseUrl,
            @Value("${ai.api-key}") String apiKey,
            @Value("${ai.model}") String model
    ) {
        this.model = model;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Retryable(
            includes = {
                    WebClientResponseException.TooManyRequests.class,
                    WebClientResponseException.ServiceUnavailable.class
            },
            maxRetries = 2,
            delay = 1000,
            multiplier = 2
    )
    public String chat(List<OpenRouterMessage> messages) {
        OpenRouterRequest request = new OpenRouterRequest(
                model,
                messages
        );

        OpenRouterResponse response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenRouterResponse.class)
                .block();

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Tomt svar från OpenRouter");
        }

        return response.choices().getFirst().message().content();
    }

    public record OpenRouterRequest(
            String model,
            List<OpenRouterMessage> messages
    ) {
    }

    public record OpenRouterMessage(
            String role,
            String content
    ) {
    }

    public record OpenRouterResponse(
            List<Choice> choices
    ) {
    }

    public record Choice(
            OpenRouterMessage message
    ) {
    }
}
