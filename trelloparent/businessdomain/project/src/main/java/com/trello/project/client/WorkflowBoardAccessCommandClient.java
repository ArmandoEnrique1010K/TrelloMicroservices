package com.trello.project.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.enums.WorkflowRole;

// Como todas las peticiones tratan de hacer modificaciones en la base de datos, el name termina en "-command"
@FeignClient(name = "businessdomain-workflow-board-access-command", url = "${services.workflow.url}", configuration = FeignClientConfig.class)
public interface WorkflowBoardAccessCommandClient {
    @PostMapping("/board-access/board/{boardId}/role/{roleName}")
    ResponseEntity<Void> addBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("roleName") WorkflowRole roleName,
            // Se pasa el ID de operación por los encabezados
            // Identifica la operación que se esta ejecutando
            @RequestHeader("X-Operation-Id") UUID operationId);

    @PatchMapping("/board-access/board/{boardId}/user/{memberUserId}/role/{roleName}")
    ResponseEntity<Void> activateBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("memberUserId") UUID memberUserId,
            @PathVariable("roleName") WorkflowRole roleName,
            @RequestHeader("X-Operation-Id") UUID operationId);

    @PutMapping("/board-access/board/{boardId}/user/{memberUserId}/role/{roleName}")
    ResponseEntity<Void> changeRoleBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("memberUserId") UUID memberUserId,
            @PathVariable("roleName") WorkflowRole roleName);

    @PatchMapping("/board-access/board/{boardId}/user/{memberUserId}")
    ResponseEntity<Void> deactivateBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("memberUserId") UUID memberUserId);
}
