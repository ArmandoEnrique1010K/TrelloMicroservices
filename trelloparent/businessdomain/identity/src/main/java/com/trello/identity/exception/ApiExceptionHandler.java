package com.trello.identity.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trello.identity.auth.exception.CustomBadCredentialsException;
import com.trello.identity.auth.exception.InvalidRefreshTokenException;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.common.StandarizedApiExceptionResponse;
import com.trello.identity.profile.exception.MismatchCheckPasswordException;
import com.trello.identity.token.exception.ConfirmedAccountException;
import com.trello.identity.token.exception.InvalidTokenException;
import com.trello.identity.token.exception.UnconfirmedAccountException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    // Mejorar el texto - Error interno del servidor 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInternalServerError(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        log.info(ex.toString());
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

    // Error de usuario (desde la entidad) no encontrada
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleUserNotFoundException(
            UserNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/user-not-found",
                "User not found",
                status.value(),
                "The user was not found in the system",
                null,
                "Ha ocurrido un error inesperado");

        return ResponseEntity.status(status).body(standarizedApiExceptionResponse);
    }

    // UsernameNotFoundException
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleUserNotFoundException(
            UsernameNotFoundException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse(
                "/errors/user-not-found",
                "User not found",
                status.value(),
                "The user was not found in the system",
                null,
                "Ha ocurrido un error inesperado");

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
    @ExceptionHandler(CustomBadCredentialsException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleCustomBadCredentialsException(
            CustomBadCredentialsException ex) {
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

    // Contraseña incorrecta cuando se crea un nuevo usuario
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

    // Contraseña incorrecta cuando se trata de verificar contraseña
    @ExceptionHandler(MismatchCheckPasswordException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMismatchCheckPassword(
            MismatchCheckPasswordException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/validation",
                "Invalid request",
                status.value(),
                "That is not your password",
                null,
                "Contraseña incorrecta");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Contraseña incorrecta cuando se trata de cambiar de contraseña
    @ExceptionHandler(MismatchUpdatePasswordException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMismatchUpdatePassword(
            MismatchUpdatePasswordException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/validation",
                "Invalid request",
                status.value(),
                "The new password confirmation does not match the new password",
                null,
                "Los campos de su nueva contraseña no coinciden");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // No puede utilizar su contraseña anterior como nueva contraseña
    @ExceptionHandler(MismatchSameOldPasswordException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMismatchSameOldPassword(
            MismatchSameOldPasswordException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/validation",
                "Invalid request",
                status.value(),
                "The new password must not be the same as the old password",
                null,
                "No puede utilizar esta contraseña");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Clasico error del token invalido o incorrecto
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInvalidToken(
            InvalidTokenException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/invalid-token",
                "Invalid Token",
                status.value(),
                "The entered token is incorrect or invalid",
                null,
                "Token invalido o incorrecto");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Error de la cuenta que ya fue validada
    @ExceptionHandler(ConfirmedAccountException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleConfirmedAccount(
            ConfirmedAccountException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/account/confirmed",
                "Confirmed Account",
                status.value(),
                "Your account has already been validated and cannot be validated again",
                null,
                "Su cuenta ya fue validada");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Error de la cuenta que aun no fue validada
    @ExceptionHandler(UnconfirmedAccountException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleUnconfirmedAccount(
            UnconfirmedAccountException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/account/unconfirmed",
                "Unconfirmed Account",
                status.value(),
                "You must validate your account to perform the desired operation",
                null,
                "Su cuenta aún no fue validada");

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                "/errors/authentication/invalid-refresh-token",
                "Invalid Refresh Token",
                status.value(),
                "The refresh token is invalid or has expired",
                null,
                "Ha ocurrido un error inesperado");

        return ResponseEntity
                .status(status)
                .body(response);

    }
}
