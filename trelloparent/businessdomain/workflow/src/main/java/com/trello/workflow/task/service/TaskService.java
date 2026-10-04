package com.trello.workflow.task.service;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.enums.Color;
import com.trello.workflow.enums.Status;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.exception.MismatchedAuthorException;
import com.trello.workflow.label.dto.request.LabelRequest;
import com.trello.workflow.label.exception.LabelAlreadyExistsException;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.AuthorTaskResponse;
import com.trello.workflow.task.dto.response.DetailsTaskResponse;
import com.trello.workflow.task.dto.response.LabelTaskResponse;
import com.trello.workflow.task.dto.response.TaskResponse;
import com.trello.workflow.task.exception.LabelAlreadyAssignedException;
import com.trello.workflow.task.exception.LabelNotAssignedToTaskException;

public interface TaskService {

    TaskResponse createTask(TaskRequest taskRequest, UUID boardId, UUID userId)
            throws ForbiddenOperationException;

    List<AuthorTaskResponse> listAllTasksByBoardId(UUID boardId, UUID userId);

    TaskResponse editTask(UUID taskId, TaskRequest taskRequest, UUID userId)
            throws MismatchedAuthorException, ForbiddenOperationException;

    TaskResponse changeStatusTask(UUID taskId, Status status, UUID userId)
            throws ForbiddenOperationException;

    void deleteTask(UUID taskId, UUID userId)
            throws MismatchedAuthorException, ForbiddenOperationException;

    DetailsTaskResponse getTaskDetailsById(UUID taskId, UUID userId);

    // Asocia una etiqueta a una tarea
    LabelTaskResponse addLabelInTask(UUID labelId, UUID taskId, UUID userId)
            throws MismatchedAuthorException, ForbiddenOperationException, LabelAlreadyAssignedException;

    // Quita una etiqueta de una tarea
    LabelTaskResponse deleteLabelInTask(UUID labelId, UUID taskId, UUID userId)
            throws MismatchedAuthorException, ForbiddenOperationException, LabelNotAssignedToTaskException;

    // Crea una nueva etiqueta y lo agrega a la tarea
    LabelTaskResponse createLabelAndAddInTask(LabelRequest labelRequest, UUID taskId, Color colorName, UUID userId)
            throws LabelAlreadyExistsException, MismatchedAuthorException, ForbiddenOperationException;

}
