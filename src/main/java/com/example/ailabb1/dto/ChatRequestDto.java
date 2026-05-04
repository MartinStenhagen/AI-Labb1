package com.example.ailabb1.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(
        @NotBlank String personality,
        @NotBlank String message,
        String sessionId
) {
}
