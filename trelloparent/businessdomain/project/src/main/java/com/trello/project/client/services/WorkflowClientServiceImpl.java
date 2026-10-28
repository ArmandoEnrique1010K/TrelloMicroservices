package com.trello.project.client.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.WorkflowBoardAccessCommandClient;
import com.trello.project.client.WorkflowBoardCommandClient;
import com.trello.project.client.WorkflowOperationCommandClient;
import com.trello.project.client.enums.WorkflowRole;

@Service
public class WorkflowClientServiceImpl implements WorkflowClientService {

    private final WorkflowBoardAccessCommandClient workflowBoardAccessCommandClient;
    private final WorkflowBoardCommandClient workflowBoardCommandClient;
    private final WorkflowOperationCommandClient workflowOperationCommandClient;

    public WorkflowClientServiceImpl(WorkflowBoardAccessCommandClient workflowBoardAccessCommandClient,
            WorkflowBoardCommandClient workflowBoardCommandClient,
            WorkflowOperationCommandClient workflowOperationCommandClient) {
        this.workflowBoardAccessCommandClient = workflowBoardAccessCommandClient;
        this.workflowBoardCommandClient = workflowBoardCommandClient;
        this.workflowOperationCommandClient = workflowOperationCommandClient;
    }

    @Override
    public void addBoardAccess(UUID boardId, WorkflowRole roleName, UUID operationId) {
        workflowBoardAccessCommandClient.addBoardAccess(boardId, roleName, operationId);
    }

    @Override
    public void activateBoardAccess(UUID boardId, UUID userId, WorkflowRole roleName,
            WorkflowRole oldRoleName,
            UUID operationId) {
        workflowBoardAccessCommandClient.activateBoardAccess(boardId, userId,
                roleName, oldRoleName, operationId);
    }

    @Override
    public void changeRoleBoardAccess(UUID boardId, UUID memberUserId, WorkflowRole roleName, WorkflowRole oldRoleName,
            UUID operationId) {
        workflowBoardAccessCommandClient.changeRoleBoardAccess(boardId, memberUserId, roleName, oldRoleName,
                operationId);
    }

    @Override
    public void deactivateBoardAccess(UUID boardId, UUID memberUserId,
            WorkflowRole oldRoleName, UUID operationId) {
        workflowBoardAccessCommandClient.deactivateBoardAccess(boardId, memberUserId, oldRoleName, operationId);
    }

    @Override
    public void addBoardAndBoardAccessUserOwner(UUID boardId, UUID operationId) {
        workflowBoardCommandClient.addBoardAndBoardAccessUserOwner(boardId, operationId);
    }

    @Override
    public void deleteBoardByIdAndBoardAccessAndTasks(UUID boardId) {
        workflowBoardCommandClient.deleteBoardByIdAndBoardAccessAndTasks(boardId);
    }

    @Override
    public void deleteManyBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardIds) {
        workflowBoardCommandClient.deleteManyBoardsByIdsAndBoardAccessAndTasks(boardIds);
    }

    // Operacion
    @Override
    public void revertAddBoardAccess(UUID operationId) {
        workflowOperationCommandClient.revertAddBoardAccess(operationId);
    }

    @Override
    public void revertActivateBoardAccess(UUID operationId) {
        workflowOperationCommandClient.revertActivateBoardAccess(operationId);
    }

    @Override
    public void revertChangeRoleBoardAccess(UUID operationId) {
        workflowOperationCommandClient.revertChangeRoleBoardAccess(operationId);
    }

    @Override
    public void revertDeactivateBoardAccess(UUID operationId) {
        workflowOperationCommandClient.revertDeactivateBoardAccess(operationId);
    }

    @Override
    public void revertAddBoardAndBoardAccessUserOwner(UUID operationId) {
        workflowOperationCommandClient.revertAddBoardAndBoardAccessUserOwner(operationId);
    }
}
