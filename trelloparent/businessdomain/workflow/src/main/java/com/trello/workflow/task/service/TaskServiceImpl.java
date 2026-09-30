package com.trello.workflow.task.service;

import com.trello.workflow.services.TaskWorkflowService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.trello.workflow.boardaccess.utils.BoardAccessRoleUtils;
import com.trello.workflow.client.service.IdentityClientService;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.History;
import com.trello.workflow.entities.Task;
import com.trello.workflow.enums.Role;
import com.trello.workflow.enums.Status;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.exception.MismatchedAuthorException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.HistoryWorkflowService;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.AuthorTaskResponse;
import com.trello.workflow.task.dto.response.TaskResponse;
import com.trello.workflow.task.mapper.AuthorTaskResponseMapper;
import com.trello.workflow.task.mapper.TaskRequestMapper;
import com.trello.workflow.task.mapper.TaskResponseMapper;

@Service
public class TaskServiceImpl implements TaskService {

    private final HistoryWorkflowService historyWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final TaskWorkflowService taskWorkflowService;
    private final TaskRequestMapper taskRequestMapper;
    private final TaskResponseMapper taskResponseMapper;
    private final IdentityClientService identityClientService;
    private final AuthorTaskResponseMapper authorTaskResponseMapper;

    public TaskServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService,
            HistoryWorkflowService historyWorkflowService, TaskWorkflowService taskWorkflowService,
            TaskRequestMapper taskRequestMapper,
            TaskResponseMapper taskResponseMapper, IdentityClientService identityClientService,
            AuthorTaskResponseMapper authorTaskResponseMapper) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.historyWorkflowService = historyWorkflowService;
        this.taskWorkflowService = taskWorkflowService;
        this.taskRequestMapper = taskRequestMapper;
        this.taskResponseMapper = taskResponseMapper;
        this.identityClientService = identityClientService;
        this.authorTaskResponseMapper = authorTaskResponseMapper;
    }

    @Override
    public TaskResponse createTask(TaskRequest taskRequest, UUID boardId, UUID userId)
            throws ForbiddenOperationException {

        // Buscar permiso de acceso al tablero por boardId y userId
        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);

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
        taskToTaskRequest.setCreatedByUserId(userId);
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

    // Sin importar el rol, cualquier usuario tiene acceso a este endpoint
    // Pero se verifica que la tarea corresponda al autor
    @Override
    public List<AuthorTaskResponse> listAllTasksByBoardId(UUID boardId, UUID userId) {
        // Siempre y cuando sea el usuario administrador del tablero o cualquier miembro
        boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId, userId);

        List<Task> listTasksByBoardId = taskWorkflowService.findAllTasksByBoardId(boardId);
        List<AuthorTaskResponse> responses = authorTaskResponseMapper.taskListToAuthorTaskResponse(listTasksByBoardId);

        // Por cada respuesta debe verificar que el valor del campo "createdByUserId" de
        // la entidad Task, sea igual a "userId" que se pasa como parametro en esta
        // función
        for (int i = 0; i < listTasksByBoardId.size() && i < responses.size(); i++) {
            Task task = listTasksByBoardId.get(i);
            AuthorTaskResponse response = responses.get(i);
            boolean isCreatedByCurrentUser = task.getCreatedByUserId().equals(userId);

            // Objects.equals(task.getCreatedByUserId(), userId);
            response.setAuthor(isCreatedByCurrentUser);
        }

        return responses;
    }

    @Override
    public TaskResponse editTask(UUID taskId, TaskRequest taskRequest, UUID userId)
            throws MismatchedAuthorException, ForbiddenOperationException {

        String name = taskRequest.getName();
        String description = taskRequest.getDescription();

        Task findedTask = taskWorkflowService.findTaskById(taskId);

        UUID boardId = findedTask.getBoard().getId();

        // Buscar permiso de acceso al tablero por boardId y userId
        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);

        boolean isAuthor = findedTask.getCreatedByUserId().equals(userId);

        if (!isAuthor) {
            throw new MismatchedAuthorException();
        }

        // Rol del usuario actual
        Role role = boardAccess.getRole();

        // Si el usuario no tiene el rol de propietario o admin, no podra realizar la
        // operación
        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            throw new ForbiddenOperationException();
        }

        // Fecha y hora actual
        LocalDateTime localDateTime = LocalDateTime.now();

        findedTask.setName(name);
        findedTask.setDescription(description);
        findedTask.setUpdatedAt(localDateTime);

        Task savedTask = taskWorkflowService.saveTask(findedTask);

        TaskResponse taskResponse = taskResponseMapper.taskToTaskResponse(savedTask);
        return taskResponse;
    }

    @Override
    public TaskResponse changeStatusTask(UUID taskId, Status status, UUID userId) throws ForbiddenOperationException {

        Task findedTask = taskWorkflowService.findTaskById(taskId);
        UUID boardId = findedTask.getBoard().getId();
        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);
        Role role = boardAccess.getRole();

        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.MEMBER)) {
            throw new ForbiddenOperationException();
        }

        LocalDateTime localDateTime = LocalDateTime.now();

        findedTask.setStatus(status);
        findedTask.setUpdatedAt(localDateTime);

        Task savedTask = taskWorkflowService.saveTask(findedTask);

        // Guardar en el historial el nuevo estado
        History history = new History();
        history.setUserId(userId);
        history.setCreatedAt(localDateTime);
        history.setStatus(status);
        history.setTask(savedTask);
        historyWorkflowService.saveHistory(history);

        TaskResponse taskResponse = taskResponseMapper.taskToTaskResponse(savedTask);
        return taskResponse;
    }

    @Override
    public void deleteTask(UUID taskId, UUID userId) throws MismatchedAuthorException, ForbiddenOperationException {
        Task findedTask = taskWorkflowService.findTaskById(taskId);
        UUID boardId = findedTask.getBoard().getId();
        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);

        boolean isAuthor = findedTask.getCreatedByUserId().equals(userId);

        if (!isAuthor) {
            throw new MismatchedAuthorException();
        }

        Role role = boardAccess.getRole();

        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            throw new ForbiddenOperationException();
        }

        // Cuando se borra una tarea tambien se van a borrar las notas y su historial
        // asociado
        taskWorkflowService.deleteTaskById(taskId);
    }
}
