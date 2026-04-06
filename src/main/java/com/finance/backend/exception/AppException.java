package com.finance.backend.exception;

import org.springframework.http.HttpStatus;


public class AppException extends RuntimeException {

    /** The HTTP status to return to the client */
    private final HttpStatus status;

   
    public AppException(String message, HttpStatus status) {
        super(message);          // Pass message to RuntimeException
        this.status = status;    // Store the HTTP status
    }

    /** Getter for the HTTP status — used by GlobalExceptionHandler */
    public HttpStatus getStatus() {
        return status;
    }
}
