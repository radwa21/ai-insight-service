package org.example.aiinsightservice.exception;

public class LlmServiceException extends RuntimeException {

    public LlmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmServiceException(String message) {
        super(message);
    }
}