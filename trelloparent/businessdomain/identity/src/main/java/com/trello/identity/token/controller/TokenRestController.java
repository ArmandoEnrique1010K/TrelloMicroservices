package com.trello.identity.token.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.identity.common.StandarizedApiExceptionResponse;
import com.trello.identity.common.SuccessfulResponse;
import com.trello.identity.exception.MismatchSameOldPasswordException;
import com.trello.identity.exception.MismatchUpdatePasswordException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.profile.dto.response.common.SuccessfulUpdatePasswordResponse;
import com.trello.identity.security.JwtUtils;
import com.trello.identity.token.dto.request.ResetPasswordRequest;
import com.trello.identity.token.dto.request.SendPasswordResetTokenRequest;
import com.trello.identity.token.dto.request.ValidateConfirmAccountTokenRequest;
import com.trello.identity.token.dto.request.ValidatePasswordResetTokenRequest;
import com.trello.identity.token.dto.response.ValidatePasswordResetTokenResponse;
import com.trello.identity.token.dto.response.common.SuccessfulResetPasswordResponse;
import com.trello.identity.token.dto.response.common.SuccessfulSendConfirmAccountTokenResponse;
import com.trello.identity.token.dto.response.common.SuccessfulSendPasswordResetTokenResponse;
import com.trello.identity.token.dto.response.common.SuccessfulValidateConfirmAccountTokenResponse;
import com.trello.identity.token.exception.ConfirmedAccountException;
import com.trello.identity.token.exception.InvalidTokenException;
import com.trello.identity.token.exception.UnconfirmedAccountException;
import com.trello.identity.token.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Token API", description = "API para la gestión de tokens de un solo uso (OTP)")
@RestController
@RequestMapping("/token")
public class TokenRestController {

    private final TokenService tokenService;

    public TokenRestController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Operation(summary = "Envia un token para validar la cuenta", description = "Envia un token de 6 digitos al correo del usuario para que pueda validar su cuenta")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envio correcto del token de 6 digitos al correo del usuario", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", implementation = SuccessfulSendConfirmAccountTokenResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha enviado un token de validación a su correo"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "El usuario no esta autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Authentication is required to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error inesperado",
                        "status": 401,
                        "title": "Unauthorized",
                        "type": "/errors/authentication/not-authenticated"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "La cuenta del usuario ya fue validada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Your account has already been validated and cannot be validated again",
                        "fields": null,
                        "instance": null,
                        "message": "Su cuenta ya fue validada",
                        "status": 401,
                        "title": "Confirmed Account",
                        "type": "/errors/account/confirmed"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "El usuario no se encuentra en el sistema", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The user was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se pudo encontrar al usuario",
                        "status": 404,
                        "title": "User not found",
                        "type": "/errors/user-not-found"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @PostMapping("/send/confirmAccount")
    public ResponseEntity<SuccessfulResponse<SuccessfulSendConfirmAccountTokenResponse>> sendConfirmAccountToken(
            @AuthenticationPrincipal Jwt jwt)
            throws UserNotFoundException, ConfirmedAccountException {
        UUID userId = JwtUtils.getUserId(jwt);

        SuccessfulResponse<SuccessfulSendConfirmAccountTokenResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha enviado un token de validación a su correo");
        successfulResponse.setBody(null);

        tokenService.sendConfirmAccountToken(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);

    }

    @Operation(summary = "Valida el token para activar la cuenta", description = "Luego de validar el token, la cuenta del usuario será activada y podra realizar las operaciones")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", implementation = SuccessfulSendConfirmAccountTokenResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Token válido, su cuenta ha sido activada"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "token": "El token es obligatorio"
                        },
                        "instance": null,
                        "message": "Complete los campos indicados",
                        "status": 400,
                        "title": "Invalid request",
                        "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "El usuario no esta autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Authentication is required to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error inesperado",
                        "status": 401,
                        "title": "Unauthorized",
                        "type": "/errors/authentication/not-authenticated"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "La cuenta del usuario ya fue validada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Your account has already been validated and cannot be validated again",
                        "fields": null,
                        "instance": null,
                        "message": "Su cuenta ya fue validada",
                        "status": 401,
                        "title": "Confirmed Account",
                        "type": "/errors/account/confirmed"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "El token es incorrecto o invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The entered token is incorrect or invalid",
                        "fields": null,
                        "instance": null,
                        "message": "Token invalido o incorrecto",
                        "status": 401,
                        "title": "Invalid token",
                        "type": "/errors/invalid-token"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "El usuario no se encuentra en el sistema", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The user was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se pudo encontrar al usuario",
                        "status": 404,
                        "title": "User not found",
                        "type": "/errors/user-not-found"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @PostMapping("/validate/confirmAccount")
    public ResponseEntity<SuccessfulResponse<SuccessfulValidateConfirmAccountTokenResponse>> validateConfirmAccountToken(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ValidateConfirmAccountTokenRequest input)
            throws InvalidTokenException, UserNotFoundException, ConfirmedAccountException {
        UUID userId = JwtUtils.getUserId(jwt);
        SuccessfulResponse<SuccessfulValidateConfirmAccountTokenResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Token válido, su cuenta ha sido activada");
        successfulResponse.setBody(null);

        tokenService.validateConfirmAccountToken(userId, input);
        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Envia un token para cambiar la contraseña", description = "Envia un token de 6 digitos al correo del usuario para que pueda cambiar su contraseña si no recuerda su contraseña anterior")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envio correcto del token de 6 digitos al correo del usuario", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", implementation = SuccessfulSendConfirmAccountTokenResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha enviado un token de validación a su correo"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "email": "El correo no tiene el formato adecuado"
                        },
                        "instance": null,
                        "message": "Complete los campos indicados",
                        "status": 400,
                        "title": "Invalid request",
                        "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "La cuenta del usuario aún no fue validada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "You must validate your account to perform the desired operation",
                        "fields": null,
                        "instance": null,
                        "message": "Su cuenta aún no fue validada",
                        "status": 401,
                        "title": "Unconfirmed Account",
                        "type": "/errors/account/unconfirmed"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "El usuario no se encuentra en el sistema", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The user was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se pudo encontrar al usuario",
                        "status": 404,
                        "title": "User not found",
                        "type": "/errors/user-not-found"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @PostMapping("/send/passwordReset")
    public ResponseEntity<SuccessfulResponse<SuccessfulSendPasswordResetTokenResponse>> sendPasswordResetToken(
            @Valid @RequestBody SendPasswordResetTokenRequest input)
            throws UserNotFoundException, UnconfirmedAccountException {

        SuccessfulResponse<SuccessfulSendPasswordResetTokenResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha enviado un token de validación a su correo");
        successfulResponse.setBody(null);

        tokenService.sendPasswordResetToken(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    @Operation(summary = "Valida el token para cambiar la contraseña", description = "Luego de validar el token, recibira un UUID para que pueda cambiar su contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", implementation = SuccessfulSendConfirmAccountTokenResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Token válido, puede reestablecer su contraseña"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "email": "El correo es obligatorio",
                            "token": "El token es obligatorio"
                        },
                        "instance": null,
                        "message": "Complete los campos indicados",
                        "status": 400,
                        "title": "Invalid request",
                        "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "La cuenta del usuario aún no fue validada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "You must validate your account to perform the desired operation",
                        "fields": null,
                        "instance": null,
                        "message": "Su cuenta aún no fue validada",
                        "status": 401,
                        "title": "Unconfirmed Account",
                        "type": "/errors/account/unconfirmed"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "El token es incorrecto o invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The entered token is incorrect or invalid",
                        "fields": null,
                        "instance": null,
                        "message": "Token invalido o incorrecto",
                        "status": 401,
                        "title": "Invalid token",
                        "type": "/errors/invalid-token"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "El usuario no se encuentra en el sistema", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The user was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se pudo encontrar al usuario",
                        "status": 404,
                        "title": "User not found",
                        "type": "/errors/user-not-found"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @PostMapping("/validate/passwordReset")
    public ResponseEntity<SuccessfulResponse<ValidatePasswordResetTokenResponse>> validatePasswordResetToken(
            @Valid @RequestBody ValidatePasswordResetTokenRequest input)
            throws InvalidTokenException, UserNotFoundException, UnconfirmedAccountException {

        ValidatePasswordResetTokenResponse response = tokenService.validatePasswordResetToken(input);

        SuccessfulResponse<ValidatePasswordResetTokenResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Token válido, puede reestablecer su contraseña");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Actualiza la contraseña del usuario", description = "Reestablece la contraseña del usuario en la base de datos si el usuario no recuerda su contraseña anterior")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualización correcta de la contraseña del usuario", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", implementation = SuccessfulUpdatePasswordResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha reestablecido su contraseña"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "One or more request fields are invalid",
                      "fields": {
                        "newPasswordConfirmation": "Confirme su nueva contraseña",
                        "newPassword": "La nueva contraseña debe contener al menos una mayúscula, un número y un símbolo",
                        "resetToken": "El token de reinicio es obligatorio"
                      },
                      "instance": null,
                      "message": "Complete los campos indicados",
                      "status": 400,
                      "title": "Invalid request",
                      "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Las contraseñas no coinciden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                          "detail": "The new password confirmation does not match the new password",
                          "fields": null,
                          "instance": null,
                          "message": "Los campos de su nueva contraseña no coinciden",
                          "status": 400,
                          "title": "Invalid request",
                          "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "No puede utilizar su contraseña anterior como nueva contraseña", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                          "detail": "The new password must not be the same as the old password",
                          "fields": null,
                          "instance": null,
                          "message": "No puede utilizar esta contraseña",
                          "status": 400,
                          "title": "Invalid request",
                          "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "La cuenta del usuario aun no fue validada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "You must validate your account to perform the desired operation",
                        "fields": null,
                        "instance": null,
                        "message": "Su cuenta aún no fue validada",
                        "status": 401,
                        "title": "Unconfirmed Account",
                        "type": "/errors/account/unconfirmed"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @PutMapping("/resetPassword")
    public ResponseEntity<SuccessfulResponse<SuccessfulResetPasswordResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest input) throws UserNotFoundException,
            UnconfirmedAccountException, MismatchUpdatePasswordException, MismatchSameOldPasswordException {

        SuccessfulResponse<SuccessfulResetPasswordResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha reestablecido su contraseña");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);

    }
}
