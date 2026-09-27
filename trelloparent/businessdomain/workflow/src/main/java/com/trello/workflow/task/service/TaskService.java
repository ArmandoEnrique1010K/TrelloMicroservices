package com.trello.workflow.task.service;

import java.util.UUID;

import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.TaskResponse;

public interface TaskService {

    TaskResponse createTask(TaskRequest taskRequest, UUID boardId, UUID userId)
            throws ForbiddenOperationException;
}
