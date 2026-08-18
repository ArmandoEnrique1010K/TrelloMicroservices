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
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.auth.service.AuthService;
import com.trello.identity.common.StandarizedApiExceptionResponse;
import com.trello.identity.exception.BusinessRuleException;

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
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "type": "/errors/validation",
                      "title": "Invalid request",
                      "status": 400,
                      "detail": "One or more request fields are invalid",
                      "instance": null,
                      "message": "Complete los campos indicados",
                      "fields": {
                        "firstName": "Su nombre es obligatorio",
                        "email": "El correo no tiene el formato adecuado",
                        "password": "La contraseña debe contener al menos una mayúscula, un número y un símbolo"
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Las contraseñas no coinciden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "type": "/errors/validation",
                      "title": "Invalid request",
                      "status": 400,
                      "detail": "The password confirmation does not match the password",
                      "instance": null,
                      "message": "Las contraseñas no coinciden"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "type": "/errors/user/already-exists",
                      "title": "User already exists",
                      "status": 409,
                      "detail": "An account with the provided email already exists",
                      "instance": null,
                      "message": "Ya existe una cuenta asociada a este correo"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "type": "/errors/internal-server-error",
                      "title": "Internal server error",
                      "status": 500,
                      "detail": "An unexpected error occurred while processing the request",
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado"
                    }
                    """)))
    })
    @PostMapping("/createAccount")
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountRequest input)
            throws MismatchPasswordException, UserAlreadyExistsException {
        AccountResponse response = userService.createAccount(input);
        return ResponseEntity.status(201).body(response);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ha iniciado sesión en la aplicación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "type": "/errors/validation",
                      "title": "Invalid request",
                      "status": 400,
                      "detail": "One or more request fields are invalid",
                      "instance": null,
                      "message": "Complete los campos indicados",
                      "fields": {
                        "email": "El correo es obligatorio",
                        "password": "La contraseña es obligatoria"
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "type": "/errors/authentication/invalid-credentials",
                      "title": "Authentication failed",
                      "status": 401,
                      "detail": "The provided credentials are invalid",
                      "instance": null,
                      "message": "El email o la contraseña son incorrectos"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "type": "/errors/internal-server-error",
                      "title": "Internal server error",
                      "status": 500,
                      "detail": "An unexpected error occurred while processing the request",
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado"
                    }
                    """)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest input)
            throws BusinessRuleException {
        AuthenticationResponse response = userService.login(input);
        return ResponseEntity.status(200).body(response);
    }
}
