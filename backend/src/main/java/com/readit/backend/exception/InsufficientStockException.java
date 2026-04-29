package com.readit.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a book does not have enough inventory to fulfill an order.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String bookTitle, int requested, int available) {
        super(String.format("Insufficient inventory for book '%s': requested %d, available %d",
                bookTitle, requested, available));
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
