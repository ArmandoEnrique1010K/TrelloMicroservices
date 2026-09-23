package com.trello.project.client.services;

import java.util.UUID;

import com.trello.project.client.enums.WorkflowRole;

public interface WorkflowClientService {

    void saveBoardAccess(
            UUID boardId,
            WorkflowRole roleName);
}
