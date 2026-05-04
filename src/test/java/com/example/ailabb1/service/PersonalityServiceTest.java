package com.example.ailabb1.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonalityServiceTest {

    private final PersonalityService personalityService = new PersonalityService();

    @Test
    void getSystemPrompt_shouldReturnCoderPrompt_whenPersonalityIsCoder() {
        String result = personalityService.getSystemPrompt("coder");

        assertThat(result)
                .contains("programmeringslärare");
    }

    @Test
    void getSystemPrompt_shouldReturnHelperPrompt_whenPersonalityIsHelper() {
        String result = personalityService.getSystemPrompt("helper");

        assertThat(result)
                .contains("hjälpsam");
    }

    @Test
    void getSystemPrompt_shouldReturnPiratePrompt_whenPersonalityIsPirate() {
        String result = personalityService.getSystemPrompt("pirate");

        assertThat(result)
                .contains("pirat");
    }

    @Test
    void getSystemPrompt_shouldThrowException_whenPersonalityIsUnknown() {
        assertThatThrownBy(() -> personalityService.getSystemPrompt("banana"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Okänd personality");
    }

    @Test
    void getSystemPrompt_shouldIgnoreCase_whenPersonalityHasUppercaseLetters() {
        String result = personalityService.getSystemPrompt("CoDeR");

        assertThat(result).contains("programmeringslärare");
    }
}
