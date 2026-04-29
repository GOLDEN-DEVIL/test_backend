package com.readit.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user attempts to access a resource they do not own
 * or are not permitted to view/modify.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String resource, String field, Object value) {
        super(String.format("Access denied to %s with %s: '%s'", resource, field, value));
    }

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
