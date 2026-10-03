package com.trello.workflow.task.service;

import com.trello.workflow.services.TaskWorkflowService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trello.workflow.boardaccess.utils.BoardAccessRoleUtils;
import com.trello.workflow.client.dto.response.UserResponse;
import com.trello.workflow.client.service.IdentityClientService;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.History;
import com.trello.workflow.entities.Note;
import com.trello.workflow.entities.Task;
import com.trello.workflow.enums.Role;
import com.trello.workflow.enums.Status;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.exception.MismatchedAuthorException;
import com.trello.workflow.history.dto.response.HistoryResponse;
import com.trello.workflow.note.dto.response.UserNoteResponse;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.HistoryWorkflowService;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.AuthorTaskResponse;
import com.trello.workflow.task.dto.response.DetailsTaskResponse;
import com.trello.workflow.task.dto.response.TaskResponse;
import com.trello.workflow.task.mapper.AuthorTaskResponseMapper;
import com.trello.workflow.task.mapper.DetailsTaskResponseMapper;
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
    private final DetailsTaskResponseMapper detailsTaskResponseMapper;

    public TaskServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService,
            HistoryWorkflowService historyWorkflowService, TaskWorkflowService taskWorkflowService,
            TaskRequestMapper taskRequestMapper,
            TaskResponseMapper taskResponseMapper, IdentityClientService identityClientService,
            AuthorTaskResponseMapper authorTaskResponseMapper, DetailsTaskResponseMapper detailsTaskResponseMapper) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.historyWorkflowService = historyWorkflowService;
        this.taskWorkflowService = taskWorkflowService;
        this.taskRequestMapper = taskRequestMapper;
        this.taskResponseMapper = taskResponseMapper;
        this.identityClientService = identityClientService;
        this.authorTaskResponseMapper = authorTaskResponseMapper;
        this.detailsTaskResponseMapper = detailsTaskResponseMapper;
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

    @Override
    public DetailsTaskResponse getTaskDetailsById(UUID taskId, UUID userId) {

        // 1. Obtener la tarea y validar el acceso del usuario al tablero
        Task findedTask = taskWorkflowService.findTaskById(taskId);
        UUID boardId = findedTask.getBoard().getId();
        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);
        Role role = boardAccess.getRole();

        // 2. Mapear la información propia de Task

        // MapStruct se encarga del mapeo de los campos que pueden ser transformados
        // directamente desde Task hacia DetailsTaskResponse. Los campos "createdByUser"
        // e "histories" están ignorados deliberadamente en el mapper
        // porque necesitan lógica adicional:
        // - createdByUser: requiere consultar el microservicio Identity
        // - histories: su contenido depende del rol del usuario
        DetailsTaskResponse response = detailsTaskResponseMapper.taskToDetailsTaskResponse(findedTask);

        // 3. Obtener los datos necesarios para enriquecer la respuesta

        // ID del usuario que creó la tarea. Este UUID pertenece al microservicio
        // Identity, por lo que necesitamos realizar una llamada externa para obtener
        // sus datos completos.
        UUID createdByUserId = findedTask.getCreatedByUserId();

        // Las notas se utilizan posteriormente para obtener los usuarios que crearon
        // cada una de ellas
        List<Note> notes = findedTask.getNotes();

        // El historial solamente se procesará si el usuario tiene autorización
        // suficiente para visualizarlo.
        List<History> histories = findedTask.getHistories();

        // 4. Obtener el usuario creador de la tarea

        // Se realiza una llamada al microservicio Identity para obtener los datos del
        // usuario a partir de su UUID. Esta operación no pertenece al mapper porque
        // MapStruct debe encargarse únicamente de transformaciones de objetos y no de
        // llamadas a otros microservicios.
        UserResponse createdByUser = identityClientService.findUserById(createdByUserId);
        response.setCreatedByUser(createdByUser);

        // 5. Enriquecer las notas con los datos de sus usuarios

        // En este momento, MapStruct ya transformo:
        // List<Note> -> List<UserNoteResponse>

        // UserNoteResponse también necesita los datos del usuario que creó cada nota

        // Para evitar realizar una petición al Identity Service por cada nota,
        // enrichNotesWithUserData() obtiene todos los usuarios necesarios mediante una
        // única petición.
        enrichNotesWithUserData(notes, response.getNotes());

        // 6. Procesar el historial dependiendo del rol

        // El historial de cambios de estado solamente puede ser visualizado por
        // usuarios que tengan, como mínimo, el rol ADMIN

        // Si el usuario no tiene autorización, se devuelve una lista vacía en lugar del
        // historial
        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            response.setHistories(List.of());
        } else {
            // Este mapeo se realiza aquí y no en DetailsTaskResponseMapper porque el
            // historial no siempre debe formar parte de la respuesta, sino que depende de
            // la autorización del usuario.
            List<HistoryResponse> historiesResponse = mapHistories(histories);

            // Una vez creados los HistoryResponse, se obtienen los datos de los usuarios
            // que realizaron cada cambio
            enrichHistoriesWithUserData(histories, historiesResponse);

            response.setHistories(historiesResponse);

        }

        return response;

    }

    // Enriquece cada nota con los datos del usuario que la creó
    // Se utiliza getUsersById() para obtener todos los usuarios necesarios en una
    // única llamada al microservicio Identity, evitando realizar una petición por
    // cada nota
    private void enrichNotesWithUserData(
            List<Note> notes,
            List<UserNoteResponse> responses) {
        // Obtiene un Map<UUID, UserResponse> para poder localizar rápidamente los datos
        // de cada usuario mediante su UUID.
        Map<UUID, UserResponse> usersById = getUsersById(notes, note -> note.getCreatedByUserId());

        // Las dos listas mantienen el mismo orden porque UserNoteResponse fue generado
        // previamente a partir de List<Note>.
        for (int i = 0; i < notes.size(); i++) {
            UUID userId = notes.get(i).getCreatedByUserId();

            // Agrega al response los datos completos del usuario correspondiente a la nota
            responses.get(i).setCreatedByUser(
                    usersById.get(userId));
        }
    }

    // Convierte una lista de History en una lista de HistoryResponse
    // Este mapeo se mantiene en el Service porque el historial es condicional,
    // porque solamente debe procesarse cuando el usuario tiene autorización para
    // visualizarlo.
    private List<HistoryResponse> mapHistories(
            List<History> histories) {

        if (histories == null || histories.isEmpty()) {
            return Collections.emptyList();
        }

        return histories.stream()
                .map(this::mapHistory)
                .toList();
    }

    // Convierte una entidad History individual en HistoryResponse
    private HistoryResponse mapHistory(History history) {

        HistoryResponse response = new HistoryResponse();

        response.setId(history.getId());
        response.setCreatedAt(history.getCreatedAt());
        response.setStatus(history.getStatus());

        return response;
    }

    // Enriquece cada HistoryResponse con los datos del usuario que realizó la
    // acción registrada en el historial

    // Al igual que ocurre con las notas, History solamente contiene el UUID del
    // usuario, por lo que es necesario consultar el microservicio Identity.
    private void enrichHistoriesWithUserData(
            List<History> histories,
            List<HistoryResponse> responses) {

        Map<UUID, UserResponse> usersById = getUsersById(
                histories,
                history -> history.getUserId());

        for (int i = 0; i < histories.size(); i++) {

            UUID userId = histories.get(i).getUserId();

            responses.get(i).setCreatedByUser(
                    usersById.get(userId));
        }
    }

    // Obtiene los usuarios asociados a una colección de objetos

    // Este método es genérico porque puede trabajar con:
    // - List<Note> -> note.getCreatedByUserId()
    // - List<History> -> history.getUserId()

    // De esta manera se evita duplicar la lógica para obtener usuarios desde el
    // microservicio Identity
    private <T> Map<UUID, UserResponse> getUsersById(
            List<T> items,
            Function<T, UUID> userIdExtractor) {

        // Set se utiliza para eliminar IDs duplicados. Si varias notas fueron creadas
        // por el mismo usuario, solamente se consulta ese usuario una vez
        Set<UUID> userIds = items.stream()
                .map(userIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Si ningún objeto tiene un usuario asociado, no es necesario realizar una
        // petición al microservicio Identity.
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Consulta todos los usuarios mediante una única llamada al microservicio
        // Identity
        return identityClientService
                .findUsersByIds(userIds)
                .stream()
                .collect(Collectors.toMap(user -> user.getId(),
                        Function.identity()));
    }

}
