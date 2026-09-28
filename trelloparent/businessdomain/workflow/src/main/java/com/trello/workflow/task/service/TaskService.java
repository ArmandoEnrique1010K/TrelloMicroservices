package com.trello.workflow.task.service;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.enums.Status;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.TaskResponse;

public interface TaskService {

    TaskResponse createTask(TaskRequest taskRequest, UUID boardId, UUID userId)
            throws ForbiddenOperationException;

    List<TaskResponse> listAllTasksByBoardId(UUID boardId, UUID userId);

    TaskResponse editTask(UUID taskId, TaskRequest taskRequest, UUID userId) throws ForbiddenOperationException;

    TaskResponse changeStatusTask(UUID taskId, Status status, UUID userId) throws ForbiddenOperationException;

    void deleteTask(UUID taskId, UUID userId) throws ForbiddenOperationException;
}
