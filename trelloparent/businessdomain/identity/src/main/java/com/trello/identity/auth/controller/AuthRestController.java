package com.trello.identity.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.identity.auth.dto.AccountRequest;
import com.trello.identity.auth.dto.AccountResponse;
import com.trello.identity.auth.service.AuthService;
import com.trello.identity.common.StandardizedApiExceptionResponse;
import com.trello.identity.exception.BusinessRuleException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "AUTH API", description = "This API server provides all the functionality for user authentication")
@RestController
@RequestMapping("/user")
public class AuthRestController {
    private final AuthService userService;

    public AuthRestController(AuthService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new User", description = "Registers a new user in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardizedApiExceptionResponse.class))),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessRuleException.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountRequest input) throws BusinessRuleException {
        AccountResponse response = userService.createAccount(input);
        return ResponseEntity.status(201).body(response);
    }
}
