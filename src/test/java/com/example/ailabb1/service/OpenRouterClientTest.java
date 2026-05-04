package com.example.ailabb1.service;

import com.example.ailabb1.exception.AiTemporaryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.junit5.WireMockExtension.newInstance;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "ai.api-key=test-key",
        "ai.model=test-model"
})
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
    }

    @Test
    void chat_shouldRetry_whenOpenRouterReturnsServiceUnavailable() {
        wireMock.stubFor(post("/chat/completions")
                .inScenario("retry scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(serviceUnavailable())
                .willSetStateTo("second attempt"));

        wireMock.stubFor(post("/chat/completions")
                .inScenario("retry scenario")
                .whenScenarioStateIs("second attempt")
                .willReturn(serviceUnavailable())
                .willSetStateTo("third attempt"));

        wireMock.stubFor(post("/chat/completions")
                .inScenario("retry scenario")
                .whenScenarioStateIs("third attempt")
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
    }

    @Test
    void chat_shouldThrowTemporaryException_whenOpenRouterKeepsReturning429() {
        wireMock.stubFor(post("/chat/completions")
                .willReturn(aResponse().withStatus(429)));

        assertThatThrownBy(() -> openRouterClient.chat(List.of(
                new OpenRouterClient.OpenRouterMessage("user", "Hello")
        )))
                .isInstanceOf(AiTemporaryException.class)
                .hasMessageContaining("För många anrop");

        wireMock.verify(3, postRequestedFor(urlEqualTo("/chat/completions")));
    }
}
