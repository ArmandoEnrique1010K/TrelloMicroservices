package com.trello.workflow.task.service;

import com.trello.workflow.services.TaskWorkflowService;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.boardaccess.utils.BoardAccessRoleUtils;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.History;
import com.trello.workflow.entities.Task;
import com.trello.workflow.enums.Role;
import com.trello.workflow.enums.Status;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.HistoryWorkflowService;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.TaskResponse;
import com.trello.workflow.task.mapper.TaskRequestMapper;
import com.trello.workflow.task.mapper.TaskResponseMapper;

@Service
public class TaskServiceImpl implements TaskService {

    private final HistoryWorkflowService historyWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final TaskWorkflowService taskWorkflowService;
    private final TaskRequestMapper taskRequestMapper;
    private final TaskResponseMapper taskResponseMapper;

    public TaskServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService,
            HistoryWorkflowService historyWorkflowService, TaskWorkflowService taskWorkflowService,
            TaskRequestMapper taskRequestMapper,
            TaskResponseMapper taskResponseMapper) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.historyWorkflowService = historyWorkflowService;
        this.taskWorkflowService = taskWorkflowService;
        this.taskRequestMapper = taskRequestMapper;
        this.taskResponseMapper = taskResponseMapper;
    }

    @Override
    public TaskResponse createTask(TaskRequest taskRequest, UUID boardId, UUID userId)
            throws ForbiddenOperationException {

        // Buscar permiso de acceso al tablero por boardId y userId
        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserId(boardId, userId);

        // Rol del usuario actual
        Role role = boardAccess.getRole();

        // Si el usuario no tiene el rol de propietario o admin, no podra realizar la
        // operación
        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            throw new ForbiddenOperationException();
        }

        // Fecha y hora actual
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Guardado de tarea
        Task taskToTaskRequest = taskRequestMapper.taskRequestToTask(taskRequest);
        taskToTaskRequest.setCreatedAt(currentDateTime);
        taskToTaskRequest.setUpdatedAt(currentDateTime);
        taskToTaskRequest.setBoard(boardAccess.getBoard());
        taskToTaskRequest.setStatus(Status.PENDING);

        Task savedTask = taskWorkflowService.saveTask(taskToTaskRequest);

        // Agrega una historia al historial
        History history = new History();
        history.setUserId(userId);
        history.setCreatedAt(currentDateTime);
        history.setStatus(Status.PENDING);
        history.setTask(savedTask);
        historyWorkflowService.saveHistory(history);

        // Respuesta
        TaskResponse taskResponse = taskResponseMapper.taskToTaskResponse(savedTask);
        return taskResponse;
    }

}
