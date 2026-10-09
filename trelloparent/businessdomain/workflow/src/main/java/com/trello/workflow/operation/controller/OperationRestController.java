package com.trello.workflow.operation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.operation.service.OperationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Operation API", description = "API para la gestión de operaciones atomicas")
@RestController
@RequestMapping("/operations")
public class OperationRestController {

    private final OperationService operationService;

    public OperationRestController(OperationService operationService) {
        this.operationService = operationService;
    }

    @Operation(summary = "Compensa una operación pendiente", description = "Compensa una operación que ha quedado pendiente durante la caida del microservicio")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{operationId}/compensate")
    public ResponseEntity<Void> compensateOperation(
            @Parameter(description = "ID de la operación", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID operationId,
            @AuthenticationPrincipal Jwt jwt) {
        // UUID userId = JwtUtils.getUserId(jwt);

        operationService.compensateOperation(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
