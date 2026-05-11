package com.example.ailabb1.service;

import com.example.ailabb1.exception.AiServiceException;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.junit5.WireMockExtension.newInstance;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "ai.api-key=test-key",
        "ai.model=test-model",

        "resilience4j.circuitbreaker.circuit-breaker-aspect-order=1",
        "resilience4j.retry.retry-aspect-order=2",

        // Retry: 3 försök totalt, men kort delay så testerna går snabbt
        "resilience4j.retry.instances.openRouterClient.max-attempts=3",
        "resilience4j.retry.instances.openRouterClient.wait-duration=10ms",
        "resilience4j.retry.instances.openRouterClient.retry-exceptions=com.example.ailabb1.exception.AiTemporaryException",

        // Circuit breaker: låga värden för att kunna testa snabbt
        "resilience4j.circuitbreaker.instances.openRouterClient.sliding-window-type=COUNT_BASED",
        "resilience4j.circuitbreaker.instances.openRouterClient.sliding-window-size=2",
        "resilience4j.circuitbreaker.instances.openRouterClient.minimum-number-of-calls=2",
        "resilience4j.circuitbreaker.instances.openRouterClient.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.openRouterClient.wait-duration-in-open-state=2s",
        "resilience4j.circuitbreaker.instances.openRouterClient.permitted-number-of-calls-in-half-open-state=1",
        "resilience4j.circuitbreaker.instances.openRouterClient.record-exceptions=com.example.ailabb1.exception.AiTemporaryException"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OpenRouterClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("ai.base-url", wireMock::baseUrl);
    }

    @Autowired
    private OpenRouterClient openRouterClient;

    @Test
    void chat_shouldReturnAnswer_whenOpenRouterRespondsSuccessfully() {
        wireMock.stubFor(post("/chat/completions")
                .willReturn(okJson("""
                        {
                          "choices": [
                            {
                              "message": {
                                "role": "assistant",
                                "content": "Hello world!"
                              }
                            }
                          ]
                        }
                        """)));

        String answer = openRouterClient.chat(List.of(
                new OpenRouterClient.OpenRouterMessage("user", "Say hello")
        ));

        assertThat(answer).isEqualTo("Hello world!");

        wireMock.verify(1, postRequestedFor(urlEqualTo("/chat/completions")));
    }

    @Test
    void chat_shouldRetryAndSucceed_whenOpenRouterFailsTwiceThenRespondsSuccessfully() {
        wireMock.stubFor(post("/chat/completions")
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503).withBody("Fail 1"))
                .willSetStateTo("First Failure"));

        wireMock.stubFor(post("/chat/completions")
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("First Failure")
                .willReturn(aResponse().withStatus(503).withBody("Fail 2"))
                .willSetStateTo("Second Failure"));

        wireMock.stubFor(post("/chat/completions")
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Second Failure")
                .willReturn(okJson("""
                        {
                          "choices": [
                            {
                              "message": {
                                "role": "assistant",
                                "content": "Recovered!"
                              }
                            }
                          ]
                        }
                        """)));

        String answer = openRouterClient.chat(List.of(
                new OpenRouterClient.OpenRouterMessage("user", "Hello")
        ));

        assertThat(answer).isEqualTo("Recovered!");

        wireMock.verify(3, postRequestedFor(urlEqualTo("/chat/completions")));

        assertThat(wireMock.findAll(postRequestedFor(urlEqualTo("/chat/completions"))))
                .hasSize(3);
    }

    @Test
    void chat_shouldUseFallback_whenOpenRouterKeepsReturningServiceUnavailable() {
        wireMock.stubFor(post("/chat/completions")
                .willReturn(aResponse().withStatus(503).withBody("Fail")));

        assertThatThrownBy(() -> openRouterClient.chat(List.of(
                new OpenRouterClient.OpenRouterMessage("user", "Hello")
        )))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("AI-tjänsten är tillfälligt otillgänglig");

        wireMock.verify(3, postRequestedFor(urlEqualTo("/chat/completions")));
    }

    @Test
    void chat_shouldOpenCircuitBreakerAndNotCallOpenRouter_whenFailuresReachThreshold() {
        wireMock.stubFor(post("/chat/completions")
                .willReturn(aResponse().withStatus(503).withBody("Fail")));

        for (int i = 0; i < 2; i++) {
            try {
                openRouterClient.chat(List.of(
                        new OpenRouterClient.OpenRouterMessage("user", "Hello")
                ));
            } catch (Exception ignored) {
                // Fyll circuit breaker-fönstret med fel.
            }
        }

        wireMock.resetRequests();

        assertThatThrownBy(() -> openRouterClient.chat(List.of(
                new OpenRouterClient.OpenRouterMessage("user", "Hello again")
        )))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("AI-tjänsten är tillfälligt otillgänglig");

        wireMock.verify(0, postRequestedFor(urlEqualTo("/chat/completions")));
    }

    @Test
    void chat_shouldNotRetry_whenOpenRouterReturnsBadRequest() {
        wireMock.stubFor(post("/chat/completions")
                .willReturn(aResponse().withStatus(400)));

        assertThatThrownBy(() -> openRouterClient.chat(List.of(
                new OpenRouterClient.OpenRouterMessage("user", "Hello")
        )))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("Kontrollera modellnamn");

        wireMock.verify(1, postRequestedFor(urlEqualTo("/chat/completions")));
    }

    @Test
    void chat_shouldNotRetry_whenOpenRouterReturnsUnauthorized() {
        wireMock.stubFor(post("/chat/completions")
                .willReturn(aResponse().withStatus(401)));

        assertThatThrownBy(() -> openRouterClient.chat(List.of(
                new OpenRouterClient.OpenRouterMessage("user", "Hello")
        )))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("Kontrollera API-nyckel");

        wireMock.verify(1, postRequestedFor(urlEqualTo("/chat/completions")));
    }
}
