package com.trello.workflow.label.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.common.SuccessfulVoidResponse;
import com.trello.workflow.label.dto.request.LabelRequest;
import com.trello.workflow.label.dto.response.LabelResponse;
import com.trello.workflow.label.service.LabelService;
import com.trello.workflow.security.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// TODO: PROBAR LOS ENDPOINTS
@Tag(name = "Label API", description = "API para la gestión de etiquetas")
@RestController
@RequestMapping("/label")
public class LabelRestController {

    private final LabelService labelService;

    public LabelRestController(LabelService labelService) {
        this.labelService = labelService;
    }

    // TODO: AÑADIR APIRESPONSES
    @Operation(summary = "Agrega una etiqueta", description = "Agrega una etiqueta al tablero por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/board/{boardId}")
    public ResponseEntity<SuccessfulResponse<LabelResponse>> createLabel(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Valid @RequestBody LabelRequest input) {

        UUID userId = JwtUtils.getUserId(jwt);

        LabelResponse response = labelService.createLabel(input, boardId, userId);
        SuccessfulResponse<LabelResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Etiqueta creada");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    @Operation(summary = "Lista las etiquetas", description = "Obtiene una lista de etiquetas por ID del tablero en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<LabelResponse>> listAllLabels(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId) {
        UUID userId = JwtUtils.getUserId(jwt);

        List<LabelResponse> response = labelService.listAllLabels(boardId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Edita una etiqueta", description = "Edita los datos de una etiqueta por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{labelId}")
    public ResponseEntity<SuccessfulResponse<LabelResponse>> editLabel(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de etiqueta", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID labelId,
            @Valid @RequestBody LabelRequest input) {
        UUID userId = JwtUtils.getUserId(jwt);
        LabelResponse response = labelService.editLabel(labelId, input, userId);

        SuccessfulResponse<LabelResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha modificado la etiqueta");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Elimina una etiqueta", description = "Elimina una etiqueta por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{labelId}")
    public ResponseEntity<SuccessfulResponse<SuccessfulVoidResponse>> deleteLabel(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de etiqueta", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID labelId) {
        UUID userId = JwtUtils.getUserId(jwt);
        labelService.deleteLabel(labelId, userId);

        SuccessfulResponse<SuccessfulVoidResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado la etiqueta");
        successfulResponse.setBody(null);
        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

}
