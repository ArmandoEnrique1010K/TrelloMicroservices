package com.trello.project.client.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.WorkflowClient;
import com.trello.project.client.enums.WorkflowRole;

@Service
public class WorkflowClientServiceImpl implements WorkflowClientService {

    private final WorkflowClient workflowClient;

    public WorkflowClientServiceImpl(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public void saveBoardAccess(UUID boardId, WorkflowRole roleName) {
        workflowClient.saveBoardAccess(boardId, roleName);
    }
}
