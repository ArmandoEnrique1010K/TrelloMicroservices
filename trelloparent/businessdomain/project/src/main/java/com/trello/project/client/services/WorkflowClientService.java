package com.trello.project.client.services;

import java.util.List;
import java.util.UUID;

import com.trello.project.client.enums.WorkflowRole;

public interface WorkflowClientService {

    void addBoardAccess(
            UUID boardId,
            WorkflowRole roleName,
            UUID operationId);

    void activateBoardAccess(
            UUID boardId,
            UUID userId,
            WorkflowRole roleName,
            WorkflowRole oldRoleName,
            UUID operationId);

    void changeRoleBoardAccess(
            UUID boardId,
            UUID memberUserId,
            WorkflowRole roleName,
            WorkflowRole oldRoleName,
            UUID operationId);

    void deactivateBoardAccess(
            UUID boardId,
            UUID memberUserId,
            WorkflowRole oldRoleName,
            UUID operationId);

    void addBoardAndBoardAccessUserOwner(UUID boardId, UUID operationId);

    void deleteBoardByIdAndBoardAccessAndTasks(UUID boardId);

    void deleteManyBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardIds);

    void revertAddBoardAccess(UUID operationId);

    void revertActivateBoardAccess(UUID operationId);

    void revertChangeRoleBoardAccess(UUID operationId);

    void revertDeactivateBoardAccess(UUID operationId);

    void revertAddBoardAndBoardAccessUserOwner(UUID operationId);
}
