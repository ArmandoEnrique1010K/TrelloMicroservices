package com.trello.project.client.services;

import java.util.List;
import java.util.UUID;

import com.trello.project.client.enums.WorkflowRole;

public interface WorkflowClientService {

    void addBoardAccess(
            UUID boardId,
            WorkflowRole roleName);

    void activateBoardAccess(
            UUID boardId,
            UUID memberUserId,
            WorkflowRole roleName);

    void changeRoleBoardAccess(
            UUID boardId,
            UUID memberUserId,
            WorkflowRole roleName);

    void deactivateBoardAccess(
            UUID boardId,
            UUID memberUserId);

    void addBoardAndBoardAccessUserOwner(UUID boardId);

    void deleteBoardByIdAndBoardAccessAndTasks(UUID boardId);

    void deleteManyBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardIds);
}
