package com.trello.identity.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.common.StandarizedApiExceptionResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

    // Mejorar el texto - Error interno del servidor 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknownHostException(Exception ex) {
        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "TECHNICAL",
                "Internal server error",
                "1024",
                "Ha ocurrido un error",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(standarizedApiExceptionResponse);
    }

    // Ha roto las reglas de negocio - CONFLICT - status 409
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<?> handleBusinessRuleException(BusinessRuleException ex) {

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "BUSINESS",
                "Business rule violation",
                ex.getCode(), "Ha ocurrido un error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(standarizedApiExceptionResponse);
    }

    // Validacion de campos de formulario
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "VALIDATION",
                "Validation error",
                "VALIDATION_ERROR",
                "Complete los campos faltantes",
                "One or more fields are invalid",
                errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // CREDENCIALES INCORRECTAS
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredntialsException(BadCredentialsException ex) {

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "AUTHENTICATION",
                "Invalid Credentials",
                "1001",
                "Las credenciales son invalidas",
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    // El usuario existe
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleUserAlreadyExists(
            UserAlreadyExistsException exception) {

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/user/already-exists",
                "User already exists",
                exception.getCode(),
                "Ha ocurrido un error",
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

}
