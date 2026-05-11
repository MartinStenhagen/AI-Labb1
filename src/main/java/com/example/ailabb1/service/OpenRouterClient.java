package com.example.ailabb1.service;

import com.example.ailabb1.config.AiProperties;
import com.example.ailabb1.exception.AiServiceException;
import com.example.ailabb1.exception.AiTemporaryException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.List;

@Service
public class OpenRouterClient {

    private final RestClient restClient;
    private final AiProperties aiProperties;

    public OpenRouterClient(RestClient restClient, AiProperties aiProperties) {
        this.restClient = restClient;
        this.aiProperties = aiProperties;
    }

    @CircuitBreaker(name = "openRouterClient", fallbackMethod = "fallback")
    @Retry(name = "openRouterClient")
    public String chat(List<OpenRouterMessage> messages) {
        try {
            OpenRouterRequest request = new OpenRouterRequest(
                    aiProperties.model(),
                    messages,
                    0.3
            );

            OpenRouterResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.apiKey())
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "AI-Labb1")
                    .body(request)
                    .retrieve()
                    .body(OpenRouterResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AiServiceException("Tomt svar från AI-tjänsten.");
            }

            return response.choices().getFirst().message().content();

        } catch (RestClientResponseException exception) {
            throw mapOpenRouterException(exception);

        } catch (AiServiceException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new AiTemporaryException(
                    "Kunde inte kontakta AI-tjänsten. Det kan bero på nätverk eller timeout.",
                    exception
            );
        }
    }

    public String fallback(List<OpenRouterMessage> messages, Throwable throwable) {

        Throwable cause = throwable.getCause();

        if (throwable instanceof AiServiceException exception
                && !(throwable instanceof AiTemporaryException)) {
            throw exception;
        }

        if (cause instanceof AiServiceException exception
                && !(cause instanceof AiTemporaryException)) {
            throw exception;
        }

        throw new AiServiceException(
                "AI-tjänsten är tillfälligt otillgänglig efter flera försök.",
                throwable
        );
    }

    private RuntimeException mapOpenRouterException(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();

        if (status == 429) {
            String retryAfter = exception.getResponseHeaders() != null
                    ? exception.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER)
                    : null;

            String message = retryAfter == null
                    ? "För många anrop till AI-tjänsten. Vänta en stund och försök igen."
                    : "För många anrop till AI-tjänsten. Försök igen efter cirka " + retryAfter + " sekunder.";

            return new AiTemporaryException(message, exception);
        }

        if (status == 408 || status == 502 || status == 503 || status == 504) {
            return new AiTemporaryException(
                    "AI-tjänsten är tillfälligt otillgänglig eller svarade för långsamt.",
                    exception
            );
        }

        if (status == 500) {
            return new AiTemporaryException(
                    "AI-tjänsten svarade med ett tillfälligt serverfel.",
                    exception
            );
        }

        if (status == 400) {
            return new AiServiceException(
                    "AI-tjänsten nekade begäran. Kontrollera modellnamn och request-format.",
                    exception
            );
        }

        if (status == 401 || status == 403) {
            return new AiServiceException(
                    "AI-tjänsten nekade åtkomst. Kontrollera API-nyckel och behörigheter.",
                    exception
            );
        }

        if (status == 404) {
            return new AiServiceException(
                    "AI-modellen eller endpointen kunde inte hittas.",
                    exception
            );
        }

        if (status >= 400 && status < 500) {
            return new AiServiceException(
                    "AI-tjänsten nekade begäran med status: " + exception.getStatusCode(),
                    exception
            );
        }

        return new AiServiceException(
                "AI-tjänsten svarade med oväntad status: " + exception.getStatusCode(),
                exception
        );
    }

    public record OpenRouterRequest(
            String model,
            List<OpenRouterMessage> messages,
            double temperature
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