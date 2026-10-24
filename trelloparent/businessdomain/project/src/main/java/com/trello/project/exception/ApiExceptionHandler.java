package com.trello.project.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.common.StandarizedApiExceptionResponse;
import com.trello.project.invitation.exception.InvitationAlreadyExistsException;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.workspace.exception.WorkspaceAlreadyExistsException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    // -----------------------------------------------
    // Excepciones de nivel general
    // -----------------------------------------------

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

    // Excepción de violación de reglas de negocio - status 409
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBusinessRuleException(BusinessRuleException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

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

    // Excepción global de microservicio caido
    // ServiceUnavailableException
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleServiceUnavailableException(
            ServiceUnavailableException ex) {

        // Estado 503 - El servicio no esta disponible
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/service-unavailable",
                "Service unavailable",
                status.value(),
                "One o more services has fallen",
                null,
                // Este mensaje no se va a mostrar al usuario
                "Ha ocurrido un error");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // -----------------------------------------------
    // Excepciones relacionadas a espacio de trabajo
    // -----------------------------------------------

    // Excepción de espacio de trabajo existente - status 409
    @ExceptionHandler(WorkspaceAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleWorkspaceAlreadyExists(
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

    // Excepción de espacio de trabajo no encontrado - status 404
    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleWorkspaceNotFoundException(
            WorkspaceNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/workspace-not-found",
                "Workspace not found",
                status.value(),
                "The workspace was not found in the system",
                null,
                "No se ha encontrado el espacio de trabajo");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // -----------------------------------------------
    // Excepciones relacionadas a tablero
    // -----------------------------------------------

    // Excepción de tablero existente - status 409
    @ExceptionHandler(BoardAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBoardAlreadyExists(
            BoardAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/board/already-exists",
                "Board already exists",
                status.value(),
                "A board with the provided name already exists",
                null,
                "Existe un tablero con ese nombre");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Excepción de tablero no encontrado - status 404
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

    // -----------------------------------------------
    // Excepciones relacionadas a invitación
    // -----------------------------------------------

    // Excepción de invitación existente
    @ExceptionHandler(InvitationAlreadyExistsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInvitationAlreadyExists(
            InvitationAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/invitation/already-exists",
                "Invitation already exists",
                status.value(),
                "An invitation to the user from this board already exists",
                null,
                "Existe una invitación al usuario que proviene de este tablero");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Excepción de invitación no encontrada
    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInvitationNotFoundException(
            InvitationNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/invitation-not-found",
                "Invitation not found",
                status.value(),
                "The invitation was not found in the system",
                null,
                "No se ha encontrado la invitación");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de invitación que ya fue confirmada
    @ExceptionHandler(InvitationConfirmedException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInvitationConfirmedException(
            InvitationConfirmedException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/invitation-confirmed",
                "Invitation confirmed",
                status.value(),
                "The invitation has already been confirmed",
                null,
                "La invitación ya ha sido confirmada");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // -----------------------------------------------
    // Excepciones relacionadas a miembro
    // -----------------------------------------------

    // Excepción de miembro no encontrado
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMemberNotFoundException(
            MemberNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/member-not-found",
                "Member not found",
                status.value(),
                "The member was not found in the system",
                null,
                "No se ha encontrado el miembro");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de miembro inactivo
    @ExceptionHandler(MemberInactiveException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMemberInactiveException(
            MemberInactiveException ex) {

        // Estado 410 - Ya no esta disponible
        HttpStatus status = HttpStatus.GONE;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/member-inactive",
                "Member inactive",
                status.value(),
                "The member has been removed",
                null,
                "Ha ocurrido un error");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // -----------------------------------------------
    // Excepciones relacionadas a operación
    // -----------------------------------------------

    // Excepción de operación pendiente de realizar
    @ExceptionHandler(OperationCompensatedException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleOperationCompensatedException(
            OperationCompensatedException ex) {

        // Estado 202 - Operación pendiente de recuperación
        HttpStatus status = HttpStatus.ACCEPTED;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/operation/compensated",
                "Operation compensated",
                status.value(),
                "The operation has been left in a pending state because one of the services has gone down",
                null,
                // Este mensaje se va a mostrar al usuario
                "La operación quedo en un estado pendiente, vuelva a intentarlo en un momento");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de operación no realizada
    @ExceptionHandler(OperationManualReviewException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleOperationManualReviewException(
            OperationManualReviewException ex) {

        // Estado 500 - Error interno del servidor, ponganse en contacto
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/operation/manual-review",
                "Operation manual review",
                status.value(),
                "The operation requires careful review by the app administrator; please get in touch by sending an email to example@gmail.com",
                null,
                // Este mensaje no se va a mostrar al usuario
                "Ha ocurrido un error, pongase en contacto con el administrador");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de operación no encontrada
    @ExceptionHandler(OperationNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleOperationNotFoundException(
            OperationNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/operation/not-found",
                "Operation not found",
                status.value(),
                "The operation was not found in the system",
                null,
                // Este mensaje se va a mostrar al usuario
                "Ha ocurrido un error");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepción de operación que no tiene un estado "PENDING" o relacionado
    @ExceptionHandler(OperationNotPendingException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleOperationNotPendingException(
            OperationNotPendingException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/operation/not-pending",
                "Operation not pending",
                status.value(),
                "The operation does not have a 'PENDING' or relationated status",
                null,
                // Este mensaje se va a mostrar al usuario
                "Ha ocurrido un error");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // Excepcion de operacion en conflicto
    @ExceptionHandler(OperationConflictException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleOperationConflictException(
            OperationConflictException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/operation/conflict",
                "Operation in conflict",
                status.value(),
                "The data related to the operation do not match the requested data",
                null,
                "Ha ocurrido un error");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

}
