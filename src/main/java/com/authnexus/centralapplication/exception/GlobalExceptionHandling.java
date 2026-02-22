package com.authnexus.centralapplication.exception;

import com.authnexus.centralapplication.domains.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandling {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 404);
        return ResponseEntity.status(404).body(errorResponse);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex){
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 400);
        return ResponseEntity.status(400).body(errorResponse);
    }

}
