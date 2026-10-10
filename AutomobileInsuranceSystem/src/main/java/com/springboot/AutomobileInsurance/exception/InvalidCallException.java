package com.springboot.AutomobileInsurance.exception;

public class InvalidCallException extends RuntimeException {
    public InvalidCallException(String message) {
        super(message);
    }
}
