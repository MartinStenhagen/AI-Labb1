package com.example.ailabb1.exception;

public class AiTemporaryException extends AiServiceException {

    public AiTemporaryException(String message) {
        super(message);
    }

    public AiTemporaryException(String message, Throwable cause) {
        super(message, cause);
    }
}