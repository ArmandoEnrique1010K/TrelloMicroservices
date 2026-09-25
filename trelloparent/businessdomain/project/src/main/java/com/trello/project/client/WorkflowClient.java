package com.trello.project.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.enums.WorkflowRole;

@FeignClient(name = "businessdomain-workflow", url = "${services.workflow.url}", configuration = FeignClientConfig.class)
public interface WorkflowClient {

    @PostMapping("/boardAccess/board/{boardId}/role/{roleName}")
    ResponseEntity<Void> addBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("roleName") WorkflowRole roleName);

    @PutMapping("/boardAccess/board/{boardId}/user/{memberUserId}/role/{roleName}")
    ResponseEntity<Void> changeRoleBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("memberUserId") UUID memberUserId,
            @PathVariable("roleName") WorkflowRole roleName);

    @PatchMapping("/boardAccess/board/{boardId}/user/{memberUserId}")
    ResponseEntity<Void> deactivateBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("memberUserId") UUID memberUserId);

    @PatchMapping("/boardAccess/board/{boardId}/user/{memberUserId}/role/{roleName}")
    ResponseEntity<Void> activateBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("memberUserId") UUID memberUserId,
            @PathVariable("roleName") WorkflowRole roleName);

}
