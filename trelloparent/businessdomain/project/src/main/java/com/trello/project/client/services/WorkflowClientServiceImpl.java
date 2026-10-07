package com.trello.project.client.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.WorkflowCommandClient;
import com.trello.project.client.enums.WorkflowRole;

@Service
public class WorkflowClientServiceImpl implements WorkflowClientService {

    private final WorkflowCommandClient workflowClient;

    public WorkflowClientServiceImpl(WorkflowCommandClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public void addBoardAccess(UUID boardId, WorkflowRole roleName) {
        workflowClient.addBoardAccess(boardId, roleName);
    }

    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, WorkflowRole roleName) {
        workflowClient.activateBoardAccess(boardId, memberUserId, roleName);
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
    public void addBoardAndBoardAccessUserOwner(UUID boardId) {
        workflowClient.addBoardAndBoardAccessUserOwner(boardId);
    }

    @Override
    public void deleteBoardByIdAndBoardAccessAndTasks(UUID boardId) {
        workflowClient.deleteBoardByIdAndBoardAccessAndTasks(boardId);
    }

    @Override
    public void deleteManyBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardIds) {
        workflowClient.deleteManyBoardsByIdsAndBoardAccessAndTasks(boardIds);
    }
}
