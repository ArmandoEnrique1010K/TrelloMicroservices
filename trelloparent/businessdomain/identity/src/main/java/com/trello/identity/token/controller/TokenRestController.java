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
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.security.JwtUtils;
import com.trello.identity.token.dto.request.TokenRequest;
import com.trello.identity.token.dto.response.common.SuccessfulSendConfirmAccountTokenResponse;
import com.trello.identity.token.exception.InvalidTokenException;
import com.trello.identity.token.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Token API", description = "API para la gestión de tokens de un solo uso (OTP)")
@RestController
@RequestMapping("/token")
public class TokenRestController {

    private final TokenService tokenService;

    public TokenRestController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Operation(summary = "Envia un token para validar la cuenta", description = "Envia un token de 6 digitos al correo del usuario para que pueda validar su cuenta")
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
    public ResponseEntity<SuccessfulResponse<?>> sendConfirmAccountToken(@AuthenticationPrincipal Jwt jwt)
            throws UserNotFoundException {
        UUID userId = JwtUtils.getUserId(jwt);

        SuccessfulResponse<?> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha enviado un token de validación a su correo");
        successfulResponse.setBody(null);

        tokenService.sendConfirmAccountToken(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);

    }

    @Operation(summary = "Valida el token para activar la cuenta", description = "Luego de validar el token, la cuenta del usuario será activada y podra realizar las operaciones")
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
    public ResponseEntity<SuccessfulResponse<?>> validateConfirmAccountToken(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TokenRequest input)
            throws InvalidTokenException, UserNotFoundException {
        UUID userId = JwtUtils.getUserId(jwt);
        SuccessfulResponse<?> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Token válido, su cuenta ha sido activada");
        successfulResponse.setBody(null);

        tokenService.validateConfirmAccountToken(userId, input);
        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }
}
