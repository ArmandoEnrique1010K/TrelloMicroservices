package com.trello.identity.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.identity.common.StandardizedApiExceptionResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

    // Mejorar el texto
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknownHostException(Exception ex) {
        StandardizedApiExceptionResponse standarizedApiExceptionResponse = new StandardizedApiExceptionResponse(
                "TECHNICAL",
                "Internal server error",
                "1024",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(standarizedApiExceptionResponse);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<?> handleBusinessRuleException(BusinessRuleException ex) {

        StandardizedApiExceptionResponse standarizedApiExceptionResponse = new StandardizedApiExceptionResponse(
                "BUSINESS",
                "Business rule violation",
                ex.getCode(), ex.getMessage());

        return ResponseEntity.status(ex.getHttpStatus()).body(standarizedApiExceptionResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardizedApiExceptionResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        StandardizedApiExceptionResponse response = new StandardizedApiExceptionResponse(
                "VALIDATION",
                "Validation error",
                "VALIDATION_ERROR",
                "One or more fields are invalid",
                errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
