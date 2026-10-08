package com.trello.project.client.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.WorkflowBoardAccessCommandClient;
import com.trello.project.client.WorkflowBoardCommandClient;
import com.trello.project.client.enums.WorkflowRole;

@Service
public class WorkflowClientServiceImpl implements WorkflowClientService {

    private final WorkflowBoardAccessCommandClient workflowBoardAccessCommandClient;
    private final WorkflowBoardCommandClient workflowBoardCommandClient;

    public WorkflowClientServiceImpl(WorkflowBoardAccessCommandClient workflowBoardAccessCommandClient,
            WorkflowBoardCommandClient workflowBoardCommandClient) {
        this.workflowBoardAccessCommandClient = workflowBoardAccessCommandClient;
        this.workflowBoardCommandClient = workflowBoardCommandClient;
    }

    @Override
    public void addBoardAccess(UUID boardId, WorkflowRole roleName, UUID operationId) {
        workflowBoardAccessCommandClient.addBoardAccess(boardId, roleName, operationId);
    }

    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, WorkflowRole roleName, UUID operationId) {
        workflowBoardAccessCommandClient.activateBoardAccess(boardId, memberUserId, roleName, operationId);
    }

    @Override
    public void changeRoleBoardAccess(UUID boardId, UUID memberUserId, WorkflowRole roleName) {
        workflowBoardAccessCommandClient.changeRoleBoardAccess(boardId, memberUserId, roleName);
    }

    @Override
    public void deactivateBoardAccess(UUID boardId, UUID memberUserId) {
        workflowBoardAccessCommandClient.deactivateBoardAccess(boardId, memberUserId);
    }

    @Override
    public void addBoardAndBoardAccessUserOwner(UUID boardId) {
        workflowBoardCommandClient.addBoardAndBoardAccessUserOwner(boardId);
    }

    @Override
    public void deleteBoardByIdAndBoardAccessAndTasks(UUID boardId) {
        workflowBoardCommandClient.deleteBoardByIdAndBoardAccessAndTasks(boardId);
    }

    @Override
    public void deleteManyBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardIds) {
        workflowBoardCommandClient.deleteManyBoardsByIdsAndBoardAccessAndTasks(boardIds);
    }
}
