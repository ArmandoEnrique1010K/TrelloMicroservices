package com.trello.identity.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.identity.auth.dto.AccountRequest;
import com.trello.identity.auth.dto.AccountResponse;
import com.trello.identity.auth.dto.AuthenticationRequest;
import com.trello.identity.auth.dto.AuthenticationResponse;
import com.trello.identity.auth.exception.CustomBadCredentialsException;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.auth.service.AuthService;
import com.trello.identity.common.StandarizedApiExceptionResponse;
import com.trello.identity.exception.BusinessRuleException;
import com.trello.identity.exception.UserNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "AUTH API", description = "This API server provides all the functionality for user authentication")
@RestController
@RequestMapping("/auth")
public class AuthRestController {
    private final AuthService userService;

    public AuthRestController(AuthService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new User", description = "Registers a new user in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            // Una forma de obtener el value (valor de ejemplo) es tomando el código
            // generado como respuesta desde la UI de Swagger
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "firstName": "El nombre debe tener entre 3 y 25 caracteres",
                            "password": "La contraseña debe contener al menos una mayúscula, un número y un símbolo",
                            "email": "El correo debe pertenecer a Gmail, Hotmail u Outlook"
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
                        "detail": "The password confirmation does not match the password",
                        "fields": null,
                        "instance": null,
                        "message": "Las contraseñas no coinciden",
                        "status": 400,
                        "title": "Invalid request",
                        "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "An account with the provided email already exists",
                        "fields": null,
                        "instance": null,
                        "message": "Ya existe una cuenta asociada a este correo",
                        "status": 409,
                        "title": "User already exists",
                        "type": "/errors/user/already-exists"
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
                    """)))
    })
    @PostMapping("/createAccount")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest input)
            throws UserNotFoundException, MismatchPasswordException, UserAlreadyExistsException {
        AccountResponse response = userService.createAccount(input);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Log in a user", description = "Log in with the user's credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ha iniciado sesión en la aplicación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                            {
                      "detail": "One or more request fields are invalid",
                      "fields": {
                        "password": "La contraseña es obligatoria",
                        "email": "El correo es obligatorio"
                      },
                      "instance": null,
                      "message": "Complete los campos indicados",
                      "status": 400,
                      "title": "Invalid request",
                      "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "The provided credentials are invalid",
                      "fields": null,
                      "instance": null,
                      "message": "El email o la contraseña son incorrectos",
                      "status": 401,
                      "title": "Authentication failed",
                      "type": "/errors/authentication/invalid-credentials"
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
                    """)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest input)
            throws UserNotFoundException, BusinessRuleException, CustomBadCredentialsException {
        AuthenticationResponse response = userService.login(input);
        return ResponseEntity.status(200).body(response);
    }
}
