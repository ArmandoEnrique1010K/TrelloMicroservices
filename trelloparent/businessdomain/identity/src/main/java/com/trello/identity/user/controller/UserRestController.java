package com.trello.identity.user.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trello.identity.common.StandarizedApiExceptionResponse;
import com.trello.identity.security.JwtUtils;
import com.trello.identity.user.dto.response.UserResponse;
import com.trello.identity.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "User API", description = "API para la gestión de usuarios")
@RestController
@RequestMapping("/user")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Obtiene una lista de usuarios", description = "Obtiene la lista de los primeros 10 usuarios por correo e ignorando los IDs enviados de los usuarios")
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Respuesta exitosa", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)), examples = {
                    @ExampleObject(name = "Lista de usuarios", summary = "Se obtuvo la lista de usuarios", value = """
                            [
                                {
                                    "email": "opiddington8@gmail.com",
                                    "firstName": "spiddington8",
                                    "id": "cbdd2b41-09b4-4e9b-adeb-79ce7dc0abb0",
                                    "lastName": "Piddington"
                                }
                            ]
                            """),
                    @ExampleObject(name = "Lista vacía, debe enviar 6 caracteres o no pasar un dominio de correo en el parametro email", summary = "No se encontraron usuarios", value = """
                            []
                            """) })),

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
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<UserResponse>> listAllUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) List<UUID> ids) {

        UUID userId = JwtUtils.getUserId(jwt);

        List<UUID> usersIds = new ArrayList<>();
        usersIds.add(userId);

        if (ids != null) {
            for (UUID id : ids) {
                usersIds.add(id);
            }
        }

        List<UserResponse> response = userService.listAllUsersByEmailExcludingIds(email, usersIds);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
