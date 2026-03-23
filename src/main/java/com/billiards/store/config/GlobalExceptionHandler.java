package com.billiards.store.config;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.billiards.store.common.base.ErrorResponse;
import com.billiards.store.common.exception.ResourceNotFoundException;

import tools.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        HttpStatus responseStatus = HttpStatus.NOT_FOUND;
        ErrorResponse error = new ErrorResponse(responseStatus.value(), ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(error, responseStatus);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Malformed JSON request";
        logger.info(ex.getCause().toString());
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();
            String fieldName = ifx.getPath().isEmpty() ? "unknown" : ifx.getPath().get(ifx.getPath().size() - 1).getPropertyName();
            if (ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
                message = String.format("Invalid value '%s' for field '%s'. Allowed values are: %s", 
                    ifx.getValue(),
                    fieldName,
                    Arrays.toString(ifx.getTargetType().getEnumConstants())
                );
            }
            else {
                message = String.format("Invalid value '%s' for field '%s'. Expected format: %s",
                    ifx.getValue(),
                    fieldName,
                    ifx.getTargetType().getSimpleName()
                );
            }
            
        }
        
        HttpStatus responseStatus = HttpStatus.BAD_REQUEST;
        ErrorResponse error = new ErrorResponse(responseStatus.value(), message, LocalDateTime.now());
        return new ResponseEntity<>(error, responseStatus);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        HttpStatus responseStatus = HttpStatus.UNAUTHORIZED;
        ErrorResponse error = new ErrorResponse(
            responseStatus.value(), 
            "Invalid username or password", 
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, responseStatus);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        HttpStatus responseStatus = HttpStatus.UNAUTHORIZED;
        ErrorResponse error = new ErrorResponse(
            responseStatus.value(), 
            ex.getMessage(), 
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, responseStatus);
    }
}
