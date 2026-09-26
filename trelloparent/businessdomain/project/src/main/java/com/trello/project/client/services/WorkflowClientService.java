package com.trello.project.client.services;

import java.util.UUID;

import com.trello.project.client.enums.WorkflowRole;

public interface WorkflowClientService {

    void addBoardAccess(
            UUID boardId,
            WorkflowRole roleName);

    void changeRoleBoardAccess(
            UUID boardId,
            UUID memberUserId,
            WorkflowRole roleName);

    void deactivateBoardAccess(
            UUID boardId,
            UUID memberUserId);

    void activateBoardAccess(
            UUID boardId,
            UUID memberUserId,
            WorkflowRole roleName);

    void deleteAllBoardAccessByBoardId(
            UUID boardId);
}
