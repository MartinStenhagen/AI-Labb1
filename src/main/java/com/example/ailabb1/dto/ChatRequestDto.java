package com.example.ailabb1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(

        @Schema(example = "coder")
        @NotBlank
        String personality,

        @Schema(example = "Hur skriver jag ut hello world i java?")
        @NotBlank
        String message,

        @Schema(example = "test-123")
        String sessionId
) {
}
