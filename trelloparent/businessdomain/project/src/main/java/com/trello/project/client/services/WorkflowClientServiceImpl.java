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
    public void addBoardAccess(UUID boardId, WorkflowRole roleName) {
        workflowClient.addBoardAccess(boardId, roleName);
    }

    @Override
    public void changeRoleBoardAccess(UUID boardId, UUID memberUserId, WorkflowRole roleName) {
        workflowClient.changeRoleBoardAccess(boardId, memberUserId, roleName);
    }

    @Override
    public void deactivateBoardAccess(UUID boardId, UUID memberUserId) {
        workflowClient.deactivateBoardAccess(boardId, memberUserId);
    }

    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, WorkflowRole roleName) {
        workflowClient.activateBoardAccess(boardId, memberUserId, roleName);
    }
}
