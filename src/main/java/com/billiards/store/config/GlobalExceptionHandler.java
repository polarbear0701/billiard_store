package com.billiards.store.config;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.billiards.store.common.base.ErrorResponse;
import com.billiards.store.common.exception.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        HttpStatus responseStatus = HttpStatus.NOT_FOUND;
        ErrorResponse error = new ErrorResponse(responseStatus.value(), ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(error, responseStatus);
    }
}
