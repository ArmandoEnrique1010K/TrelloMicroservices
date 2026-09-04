package com.trello.project.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.project.common.StandarizedApiExceptionResponse;
import com.trello.project.workspace.exception.WorkspaceAlreadyExistsException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    // Excepción general - status 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInternalServerError(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

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

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/business-rule-violation",
                "Business rule violation",
                status.value(),
                "The operation cannot be completed because it violates a business rule",
                null,
                ex.getMessage());

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

    // Excepción de espacio de trabajo existente - status 409
    @ExceptionHandler(WorkspaceAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleUserAlreadyExists(
            WorkspaceAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/workspace/already-exists",
                "Workspace already exists",
                status.value(),
                "A workspace with the provided name already exists",
                null,
                "Existe un espacio de trabajo con ese nombre");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Excepción de espacio de trabajo no encontrado - status 400
    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleWorkspaceNotFoundException(
            WorkspaceNotFoundException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/workspace-not-found",
                "Workspace not found",
                status.value(),
                "The workspace was not found in the system",
                null,
                "No se ha encontrado el espacio de trabajo");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

}
