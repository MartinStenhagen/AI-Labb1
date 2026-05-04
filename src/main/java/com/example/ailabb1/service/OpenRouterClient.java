package com.example.ailabb1.service;

import com.example.ailabb1.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
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

        try {
            OpenRouterResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenRouterResponse.class)
                    .block(Duration.ofSeconds(25));

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AiServiceException("Tomt svar från AI-tjänsten.");
            }

            return response.choices().getFirst().message().content();

        } catch (WebClientResponseException.TooManyRequests exception) {
            throw new AiServiceException("För många anrop till AI-tjänsten. Vänta en stund och försök igen.", exception);

        } catch (WebClientResponseException.ServiceUnavailable exception) {
            throw new AiServiceException("AI-tjänsten är tillfälligt otillgänglig. Försök igen strax.", exception);

        } catch (WebClientResponseException.BadRequest exception) {
            throw new AiServiceException("AI-tjänsten nekade begäran. Kontrollera vald modell och request-format.", exception);

        } catch (WebClientResponseException exception) {
            throw new AiServiceException("AI-tjänsten svarade med felstatus: " + exception.getStatusCode(), exception);

        } catch (Exception exception) {
            throw new AiServiceException("Kunde inte kontakta AI-tjänsten eller så tog anropet för lång tid.", exception);
        }
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
