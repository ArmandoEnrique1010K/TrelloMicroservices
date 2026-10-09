package com.trello.project.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.trello.project.client.config.FeignClientConfig;

@FeignClient(name = "businessdomain-workflow-operation-command", url = "${services.workflow.url}", configuration = FeignClientConfig.class)
public interface WorkflowOperationCommandClient {
    @PostMapping("/operations/{operationId}/compensate")
    ResponseEntity<Void> compensateOperation(
            @PathVariable("operationId") UUID operationId);
}
