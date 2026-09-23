package com.trello.project.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.enums.WorkflowRole;

@FeignClient(name = "businessdomain-workflow", url = "${services.workflow.url}", configuration = FeignClientConfig.class)
public interface WorkflowClient {

    @PostMapping("/boardAccess/board/{boardId}/role/{roleName}")
    ResponseEntity<Void> saveBoardAccess(
            @PathVariable("boardId") UUID boardId,
            @PathVariable("roleName") WorkflowRole roleName);
}
