package com.trello.identity.profile.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.identity.common.StandarizedApiExceptionResponse;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.profile.dto.CheckPasswordRequest;
import com.trello.identity.profile.dto.ProfileResponse;
import com.trello.identity.profile.dto.UpdatePasswordRequest;
import com.trello.identity.profile.exception.MismatchCheckPasswordException;
import com.trello.identity.profile.exception.MismatchSameOldPasswordException;
import com.trello.identity.profile.exception.MismatchUpdatePasswordException;
import com.trello.identity.profile.service.ProfileService;
import com.trello.identity.security.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Profile API", description = "API para la gestión de perfil y constraseña del usuario")
@RestController
@RequestMapping("/profile")
public class ProfileRestController {

    private final ProfileService profileService;

    public ProfileRestController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // No hace falta documentar un endpoint relacionado a si el usuario es eliminado
    // desde la base de datos mientras esta autenticado en la aplicacion
    @Operation(summary = "Obtiene el perfil del usuario", description = "Obtiene los datos del perfil del usuario autenticadoa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile of the current user", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
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
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        UUID userId = JwtUtils.getUserId(jwt);

        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.status(200).body(response);
    }

    @Operation(summary = "Verifica la contraseña del usuario", description = "Verifica la contraseña actual del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Correct password"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "One or more request fields are invalid",
                      "fields": {
                        "currentPassword": "La contraseña es obligatoria"
                      },
                      "instance": null,
                      "message": "Complete los campos indicados",
                      "status": 400,
                      "title": "Invalid request",
                      "type": "/errors/validation"
                    }
                    """))),

            @ApiResponse(responseCode = "400", description = "Incorrect password", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "That is not your password",
                        "fields": null,
                        "instance": null,
                        "message": "Contraseña incorrecta",
                        "status": 400,
                        "title": "Invalid request",
                        "type": "/errors/validation"
                    }
                    """))),

            @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
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
    @PostMapping("/checkPassword")
    public ResponseEntity<?> checkPassword(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckPasswordRequest input)
            throws UserNotFoundException, MismatchCheckPasswordException {
        UUID userId = JwtUtils.getUserId(jwt);

        profileService.checkPassword(userId, input);
        return ResponseEntity.status(204).body(null);
    }

    @Operation(summary = "Actualiza la contraseña del usuario", description = "Actualiza la contraseña del usuario en la base de datos si este recuerda su contraseña anterior")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "One or more request fields are invalid",
                      "fields": {
                        "newPasswordConfirmation": "Confirme su nueva contraseña",
                        "newPassword": "La nueva contraseña debe contener al menos una mayúscula, un número y un símbolo",
                        "currentPassword": "Su contraseña anterior es obligatoria"
                      },
                      "instance": null,
                      "message": "Complete los campos indicados",
                      "status": 400,
                      "title": "Invalid request",
                      "type": "/errors/validation"
                    }
                    """))),

            @ApiResponse(responseCode = "400", description = "Incorrect old password", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "That is not your password",
                        "fields": null,
                        "instance": null,
                        "message": "Contraseña incorrecta",
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
                          "message": "Su nueva contraseña no coincide",
                          "status": 400,
                          "title": "Invalid request",
                          "type": "/errors/validation"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
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
    @PutMapping("updatePassword")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdatePasswordRequest input)
            throws UserNotFoundException, MismatchCheckPasswordException, MismatchUpdatePasswordException,
            MismatchSameOldPasswordException {
        UUID userId = JwtUtils.getUserId(jwt);

        profileService.updatePassword(userId, input);
        return ResponseEntity.status(204).body(null);
    }

}
