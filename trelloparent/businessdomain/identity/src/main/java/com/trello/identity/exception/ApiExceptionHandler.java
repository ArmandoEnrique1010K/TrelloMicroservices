package com.trello.identity.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.common.StandarizedApiExceptionResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

    // Mejorar el texto - Error interno del servidor 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInternalServerError(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/internal-server-error",
                "Internal server error",
                status.value(),
                "An unexpected error occurred while processing the request",
                null,
                "Ha ocurrido un error inesperado");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Ha roto las reglas de negocio - CONFLICT - status 409
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBusinessRuleException(BusinessRuleException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/business-rule-violation",
                "Business rule violation",
                status.value(),
                "The operation cannot be completed because it violates a business rule",
                null,
                ex.getMessage());

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Validacion de campos de formulario
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/validation",
                "Invalid request",
                status.value(),
                "One or more request fields are invalid",
                null,
                "Complete los campos indicados",
                errors);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // CREDENCIALES INCORRECTAS
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBadCredentialsException(BadCredentialsException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/authentication/invalid-credentials",
                "Authentication failed",
                status.value(),
                "The provided credentials are invalid",
                null,
                "El email o la contraseña son incorrectos");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // El usuario existe
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleUserAlreadyExists(
            UserAlreadyExistsException exception) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/user/already-exists",
                "User already exists",
                status.value(),
                "An account with the provided email already exists",
                null,
                "Ya existe una cuenta asociada a este correo");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(MismatchPasswordException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMismatchPassword(
            MismatchPasswordException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/validation",
                "Invalid request",
                status.value(),
                "The password confirmation does not match the password",
                null,
                "Las contraseñas no coinciden");

        return ResponseEntity
                .status(status)
                .body(response);
    }

}
