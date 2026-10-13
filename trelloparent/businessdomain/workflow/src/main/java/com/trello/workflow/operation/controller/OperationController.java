package com.trello.workflow.operation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.operation.service.OperationActivateBoardAccessService;
import com.trello.workflow.operation.service.OperationAddBoardAccessService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/operations")
public class OperationController {

    private final OperationAddBoardAccessService operationAddBoardAccessService;
    private final OperationActivateBoardAccessService operationActivateBoardAccessService;

    public OperationController(OperationAddBoardAccessService operationAddBoardAccessService,
            OperationActivateBoardAccessService operationActivateBoardAccessService) {
        this.operationAddBoardAccessService = operationAddBoardAccessService;
        this.operationActivateBoardAccessService = operationActivateBoardAccessService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/revert-add-board-access")
    public ResponseEntity<Void> revertAddBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationAddBoardAccessService.revertAddBoardAccess(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/revert-activate-board-access")
    public ResponseEntity<Void> revertActivateBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationActivateBoardAccessService.revertActivateBoardAccess(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

}
