package com.example.fileprocessor.exception;

/** 404 for lookups... */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
