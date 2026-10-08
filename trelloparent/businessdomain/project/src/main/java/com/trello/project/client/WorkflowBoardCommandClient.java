package com.trello.project.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.trello.project.client.config.FeignClientConfig;

@FeignClient(name = "businessdomain-workflow-board-command", url = "${services.workflow.url}", configuration = FeignClientConfig.class)
public interface WorkflowBoardCommandClient {
    // Si hay parametros dinamicos se utiliza @PathVariable
    @PostMapping("/boards/{boardId}")
    ResponseEntity<Void> addBoardAndBoardAccessUserOwner(
            @PathVariable("boardId") UUID boardId);

    @DeleteMapping("/boards/{boardId}")
    ResponseEntity<Void> deleteBoardByIdAndBoardAccessAndTasks(
            @PathVariable("boardId") UUID boardId);

    @DeleteMapping("/boards/batch")
    ResponseEntity<Void> deleteManyBoardsByIdsAndBoardAccessAndTasks(
            @RequestParam(required = false) List<UUID> boardIds);
}
