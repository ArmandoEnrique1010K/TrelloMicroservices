package com.trello.workflow.services;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.entities.Task;
import com.trello.workflow.exception.TaskNotFoundException;

public interface TaskWorkflowService {
    Task saveTask(Task task);

    List<Task> findAllTasksByBoardId(UUID boardId);

    Task findTaskById(UUID taskId) throws TaskNotFoundException;

    void deleteTaskById(UUID taskId) throws TaskNotFoundException;
}
