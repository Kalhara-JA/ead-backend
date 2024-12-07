package com.store.microservices.order_service.dto;

public class UserException extends RuntimeException{
    public UserException(String message){
        super(message);
    }
}
