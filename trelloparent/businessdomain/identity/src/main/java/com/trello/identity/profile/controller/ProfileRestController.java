package com.trello.identity.profile.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.identity.common.StandarizedApiExceptionResponse;
import com.trello.identity.common.SuccessfulResponse;
import com.trello.identity.exception.MismatchSameOldPasswordException;
import com.trello.identity.exception.MismatchUpdatePasswordException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.profile.dto.request.CheckPasswordRequest;
import com.trello.identity.profile.dto.request.UpdatePasswordRequest;
import com.trello.identity.profile.dto.response.ProfileResponse;
import com.trello.identity.profile.dto.response.common.SuccessfulUpdatePasswordResponse;
import com.trello.identity.profile.exception.MismatchCheckPasswordException;
import com.trello.identity.profile.service.ProfileService;
import com.trello.identity.security.JwtUtils;

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
    @Operation(summary = "Obtiene el perfil del usuario", description = "Obtiene los datos del perfil del usuario autenticado")
    // Icono de candado - significa que requiere autenticación
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene el perfil del usuario actual", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
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
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt)
            throws UserNotFoundException {
        UUID userId = JwtUtils.getUserId(jwt);

        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Verifica la contraseña del usuario", description = "Verifica la contraseña actual del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña correcta"),

            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "Errores de validación de campos", summary = "Errores de validación de campos", value = """
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
                             """),
                    @ExampleObject(name = "Contraseña incorrecta", summary = "Contraseña incorrecta", value = """
                            {
                                "detail": "That is not your password",
                                "fields": null,
                                "instance": null,
                                "message": "Contraseña incorrecta",
                                "status": 400,
                                "title": "Invalid request",
                                "type": "/errors/validation"
                            }
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
    @PostMapping("/checkPassword")
    public ResponseEntity<Void> checkPassword(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckPasswordRequest input)
            throws UserNotFoundException, MismatchCheckPasswordException {
        UUID userId = JwtUtils.getUserId(jwt);

        profileService.checkPassword(userId, input);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Actualiza la contraseña del usuario", description = "Actualiza la contraseña del usuario en la base de datos si el usuario recuerda su contraseña anterior")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualización correcta de la contraseña del usuario", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", implementation = SuccessfulUpdatePasswordResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Su contraseña ha sido actualizada"
                    }
                    """))),

            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "Errores de validación de campos", summary = "Errores de validación de campos", value = """
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
                            """),
                    @ExampleObject(name = "Contraseña antigua incorrecta", summary = "Contraseña antigua incorrecta", value = """
                            {
                                "detail": "That is not your password",
                                "fields": null,
                                "instance": null,
                                "message": "Contraseña incorrecta",
                                "status": 400,
                                "title": "Invalid request",
                                "type": "/errors/validation"
                            }
                            """),
                    @ExampleObject(name = "Las contraseñas no coinciden", summary = "Las contraseñas no coinciden", value = """
                            {
                                "detail": "The password confirmation does not match the password",
                                "fields": null,
                                "instance": null,
                                "message": "Los campos de su nueva contraseña no coinciden",
                                "status": 400,
                                "title": "Invalid request",
                                "type": "/errors/validation"
                            }
                            """),
                    @ExampleObject(name = "No puede utilizar su contraseña anterior como nueva contraseña", summary = "No puede utilizar su contraseña anterior como nueva contraseña", value = """
                            {
                                "detail": "The new password must not be the same as the old password",
                                "fields": null,
                                "instance": null,
                                "message": "No puede utilizar esta contraseña",
                                "status": 400,
                                "title": "Invalid request",
                                "type": "/errors/validation"
                            }
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
    @PutMapping("updatePassword")
    public ResponseEntity<SuccessfulResponse<?>> updatePassword(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdatePasswordRequest input)
            throws UserNotFoundException, MismatchCheckPasswordException, MismatchUpdatePasswordException,
            MismatchSameOldPasswordException {
        UUID userId = JwtUtils.getUserId(jwt);

        SuccessfulResponse<?> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Su contraseña ha sido actualizada");
        successfulResponse.setBody(null);

        profileService.updatePassword(userId, input);
        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

}