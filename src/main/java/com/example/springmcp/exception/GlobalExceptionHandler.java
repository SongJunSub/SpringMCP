package com.example.springmcp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKeyException(DuplicateKeyException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<String> handleUrlNotFoundException(UrlNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Add more exception handlers as needed

    /*
    @ExceptionHandler(org.springframework.ai.retry.NonTransientAiException.class)
    public ResponseEntity<String> handleNonTransientAiException(org.springframework.ai.retry.NonTransientAiException ex) {
        return new ResponseEntity<>("AI service error: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.ai.retry.TransientAiException.class)
    public ResponseEntity<String> handleTransientAiException(org.springframework.ai.retry.TransientAiException ex) {
        return new ResponseEntity<>("AI service is temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }
    */
}