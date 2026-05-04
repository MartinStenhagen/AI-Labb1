package com.example.ailabb1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatResponseDto(

        @Schema(example = "För att skriva ut Hello World i java kan du använda dig av system.out...")
        String answer,

        @Schema(example = "test-123")
        String sessionId
) {
}
