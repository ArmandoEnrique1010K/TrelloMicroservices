package com.trello.workflow.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.workflow.board.exception.BoardAlreadyExistsException;
import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.label.exception.LabelAlreadyExistsException;

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

    // Excepcion de que existe el tablero
    @ExceptionHandler(BoardAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBoardAlreadyExistsException(
            BoardAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/board/already-exists",
                "Board already exists",
                status.value(),
                "A board with the provided ID already exists",
                null,
                "Ya existe el tablero");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Excepción producida cuando una operación viola una restricción
    // de integridad de la base de datos, como una clave primaria,
    // clave foránea, restricción UNIQUE o NOT NULL.

    // Aunque no se va a utilizar...
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {

        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/database/integrity-violation",
                "Data integrity violation",
                status.value(),
                "The request violates a database constraint",
                null,
                "La operación viola una restricción de la base de datos");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Excepcion de tablero no encontrado
    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBoardNotFoundException(
            BoardNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/board-not-found",
                "Board not found",
                status.value(),
                "The board was not found in the system",
                null,
                "No se ha encontrado el tablero");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de operación no realizada porque el usuario no tiene el rol
    // permitido
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleForbiddenOperationException(
            ForbiddenOperationException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/forbidden-operation",
                "Forbidden operation",
                status.value(),
                "The user does not have the required role to perform the operation",
                null,
                "Ha ocurrido un error");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de autor no correspondiente porque la tarea, nota, etc..., no
    // corresponde al mismo autor que la creo (el usuario autenticado)
    @ExceptionHandler(MismatchedAuthorException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMismatchedAuthorException(
            MismatchedAuthorException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/mismached-author",
                "Mismached Author",
                status.value(),
                "The user is not the author of the requested resource",
                null,
                "Ha ocurrido un error");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de tarea no encontrada - 404
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleTaskNotFoundException(
            TaskNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/task-not-found",
                "Task not found",
                status.value(),
                "The task was not found in the system",
                null,
                "No se ha encontrado la tarea");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de nota no encontrada - 404
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleNoteNotFoundException(
            NoteNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/note-not-found",
                "Note not found",
                status.value(),
                "The note was not found in the system",
                null,
                "No se ha encontrado la nota");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de etiqueta o label no encontrado - 404
    @ExceptionHandler(LabelNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleLabelNotFoundException(
            LabelNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/label-not-found",
                "Label not found",
                status.value(),
                "The label was not found in the system",
                null,
                "No se ha encontrado la etiqueta");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Error de que el nombre de la etiqueta ya existe
    @ExceptionHandler(LabelAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleLabelAlreadyExistsException(
            LabelAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/label/already-exists",
                "Label already exists",
                status.value(),
                "A label with the provided content already exists in this board",
                null,
                "Ya existe una etiqueta con ese contenido");

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
