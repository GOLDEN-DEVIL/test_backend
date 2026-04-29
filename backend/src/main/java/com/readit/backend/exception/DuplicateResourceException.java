package com.readit.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to create a resource that already exists
 * (e.g. duplicate username or email during registration).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: '%s'", resource, field, value));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
