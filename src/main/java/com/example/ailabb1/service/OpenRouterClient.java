package com.example.ailabb1.service;

import com.example.ailabb1.exception.AiServiceException;
import com.example.ailabb1.exception.AiTemporaryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class OpenRouterClient {

    private final RestClient restClient;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    public OpenRouterClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retryable(
            includes = AiTemporaryException.class,
            maxRetries = 2,
            delay = 1000,
            multiplier = 2
    )
    public String chat(List<OpenRouterMessage> messages) {
        try {
            OpenRouterRequest request = new OpenRouterRequest(model, messages);

            OpenRouterResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "AI-Labb1")
                    .body(request)
                    .retrieve()
                    .body(OpenRouterResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AiServiceException("Tomt svar från AI-tjänsten.");
            }

            return response.choices().getFirst().message().content();

        } catch (RestClientResponseException e) {

            int status = e.getStatusCode().value();

            if (status == 429) {
                throw new AiTemporaryException(
                        "För många anrop till AI-tjänsten. Vänta en stund och försök igen.",
                        e
                );
            }

            if (status == 503) {
                throw new AiTemporaryException(
                        "AI-tjänsten är tillfälligt otillgänglig. Försök igen strax.",
                        e
                );
            }

            if (status >= 500) {
                throw new AiTemporaryException(
                        "AI-tjänsten svarade med ett tillfälligt serverfel.",
                        e
                );
            }

            if (status == 400) {
                throw new AiServiceException(
                        "AI-tjänsten nekade begäran. Kontrollera modellnamn och request-format.",
                        e
                );
            }

            throw new AiServiceException(
                    "AI-tjänsten svarade med felstatus: " + e.getStatusCode(),
                    e
            );

        } catch (AiServiceException e) {
            throw e;

        } catch (Exception e) {
            throw new AiServiceException(
                    "Kunde inte kontakta AI-tjänsten.",
                    e
            );
        }
    }

    // --- DTOs (records funkar perfekt här) ---

    public record OpenRouterRequest(
            String model,
            List<OpenRouterMessage> messages
    ) {}

    public record OpenRouterMessage(
            String role,
            String content
    ) {}

    public record OpenRouterResponse(
            List<Choice> choices
    ) {}

    public record Choice(
            Message message
    ) {}

    public record Message(
            String role,
            String content
    ) {}
}
