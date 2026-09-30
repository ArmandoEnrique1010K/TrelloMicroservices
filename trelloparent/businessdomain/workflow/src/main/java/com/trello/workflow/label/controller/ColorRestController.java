package com.trello.workflow.label.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.enums.Color;
import com.trello.workflow.label.dto.response.ColorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Color API", description = "API para la gestión de colores")
@RestController
@RequestMapping("/color")
public class ColorRestController {

    @Operation(summary = "Lista los colores", description = "Lista todos los colores disponibles")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de colores", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ColorResponse.class)))),
            @ApiResponse(responseCode = "401", description = "El usuario no estaautenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
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
    public ResponseEntity<List<ColorResponse>> getColors() {
        List<ColorResponse> response = Arrays.stream(Color.values())
                .map(this::toColorResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Mapear campos de Color hacia ColorResponse
    private ColorResponse toColorResponse(Color color) {
        ColorResponse response = new ColorResponse();

        response.setId(color.name());
        response.setName(color.getName());
        response.setHex(color.getHex());

        return response;
    }
}
