package com.store.microservices.order_service.dto;

/**
 * Custom exception for user-related errors in the Order Service.
 */
public class UserException extends RuntimeException {
    /**
     * Constructs a new UserException with the specified detail message.
     *
     * @param message the detail message
     */
    public UserException(String message) {
        super(message);
    }
}
