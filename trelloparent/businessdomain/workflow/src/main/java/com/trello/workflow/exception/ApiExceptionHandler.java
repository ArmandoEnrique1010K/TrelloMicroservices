package com.trello.workflow.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.common.StandarizedApiExceptionResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    // Excepción general - status 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInternalServerError(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        log.error(ex.getMessage());
        log.error("Error inesperado", ex);

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/internal-server-error",
                "Internal server error",
                status.value(),
                "An unexpected error occurred while processing the request",
                null,
                "Ha ocurrido un error inesperado");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de violación de reglas de negocio - status 404
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBusinessRuleException(BusinessRuleException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        log.error(ex.getMessage());
        log.error("Error de lógica de negocio", ex);

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/business-rule-violation",
                "Business rule violation",
                status.value(),
                "The operation cannot be completed because it violates a business rule",
                null,
                "Ha ocurrido un error inesperado");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de validación de campos de formulario - status 400
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

    // Excepción de permiso de acceso no encontrado
    @ExceptionHandler(BoardAccessNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBoardAccessNotFoundException(
            BoardAccessNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/board-access-not-found",
                "Board access not found",
                status.value(),
                "The board access was not found in the system",
                null,
                "No se ha encontrado el permiso de acceso al tablero");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepcion de que existe el permiso de acceso
    @ExceptionHandler(BoardAccessAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBoardAccessAlreadyExistsException(
            BoardAccessAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/board-access/already-exists",
                "Board access already exists",
                status.value(),
                "A board access with the provided board ID and user ID already exists",
                null,
                "Ya existe un permiso de acceso al tablero");

        return ResponseEntity
                .status(status)
                .body(response);
    }

}
