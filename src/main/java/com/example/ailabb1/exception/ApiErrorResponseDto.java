package com.example.ailabb1.exception;

import java.time.Instant;

public record ApiErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
