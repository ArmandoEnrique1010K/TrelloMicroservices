package com.trello.project.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.fallback.WorkflowOperationCommandClientFallbackFactory;

@FeignClient(name = "businessdomain-workflow-operation-command", url = "${services.workflow.url}", configuration = FeignClientConfig.class, fallbackFactory = WorkflowOperationCommandClientFallbackFactory.class)
public interface WorkflowOperationCommandClient {
    @PostMapping("/operations/revert-add-board-access")
    ResponseEntity<Void> revertAddBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId);

    @PostMapping("/operations/revert-activate-board-access")
    ResponseEntity<Void> revertActivateBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId);
}
