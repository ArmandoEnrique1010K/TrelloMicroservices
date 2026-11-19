package com.trello.project.operation.service.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.CleanupStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.service.handler.OperationAcceptInvitationService;
import com.trello.project.operation.service.handler.OperationChangeRoleMemberService;
import com.trello.project.operation.service.handler.OperationCreateBoardService;
import com.trello.project.operation.service.handler.OperationDeactivateMemberService;
import com.trello.project.operation.service.handler.OperationDeleteBoardService;
import com.trello.project.operation.service.handler.OperationDeleteWorkspaceService;
import com.trello.project.operation.service.processor.OperationAcceptInvitationProcessor;
import com.trello.project.operation.service.processor.OperationChangeRoleMemberProcessor;
import com.trello.project.operation.service.processor.OperationCreateBoardProcessor;
import com.trello.project.operation.service.processor.OperationDeactivateMemberProcessor;
import com.trello.project.operation.service.processor.OperationDeleteBoardProcessor;
import com.trello.project.operation.service.processor.OperationDeleteWorkspaceProcessor;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.OperationProjectService;
import com.trello.project.service.WorkspaceProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationExecutorServiceImpl implements OperationExecutorService {
    private final OperationProjectService operationProjectService;
    private final OperationAcceptInvitationService operationAcceptInvitationService;
    private final OperationAcceptInvitationProcessor operationAcceptInvitationProcessor;
    private final OperationChangeRoleMemberService operationChangeRoleMemberService;
    private final OperationChangeRoleMemberProcessor operationChangeRoleMemberProcessor;
    private final OperationCreateBoardService operationCreateBoardService;
    private final OperationCreateBoardProcessor operationCreateBoardProcessor;
    private final OperationDeactivateMemberService operationDeactivateMemberService;
    private final OperationDeactivateMemberProcessor operationDeactivateMemberProcessor;
    private final OperationDeleteBoardService operationDeleteBoardService;
    private final OperationDeleteBoardProcessor operationDeleteBoardProcessor;
    private final OperationDeleteWorkspaceService operationDeleteWorkspaceService;
    private final OperationDeleteWorkspaceProcessor operationDeleteWorkspaceProcessor;
    private final BoardProjectService boardProjectService;
    private final WorkspaceProjectService workspaceProjectService;
    private final InvitationProjectService invitationProjectService;

    public OperationExecutorServiceImpl(
            OperationProjectService operationProjectService,
            OperationAcceptInvitationService operationAcceptInvitationService,
            OperationAcceptInvitationProcessor operationAcceptInvitationProcessor,
            OperationChangeRoleMemberService operationChangeRoleMemberService,
            OperationChangeRoleMemberProcessor operationChangeRoleMemberProcessor,
            OperationCreateBoardService operationCreateBoardService,
            OperationCreateBoardProcessor operationCreateBoardProcessor,
            OperationDeactivateMemberService operationDeactivateMemberService,
            OperationDeactivateMemberProcessor operationDeactivateMemberProcessor,
            OperationDeleteBoardService operationDeleteBoardService,
            OperationDeleteBoardProcessor operationDeleteBoardProcessor,
            OperationDeleteWorkspaceService operationDeleteWorkspaceService,
            OperationDeleteWorkspaceProcessor operationDeleteWorkspaceProcessor,
            BoardProjectService boardProjectService,
            WorkspaceProjectService workspaceProjectService,
            InvitationProjectService invitationProjectService) {
        this.operationProjectService = operationProjectService;
        this.operationAcceptInvitationService = operationAcceptInvitationService;
        this.operationAcceptInvitationProcessor = operationAcceptInvitationProcessor;
        this.operationChangeRoleMemberService = operationChangeRoleMemberService;
        this.operationChangeRoleMemberProcessor = operationChangeRoleMemberProcessor;
        this.operationCreateBoardService = operationCreateBoardService;
        this.operationCreateBoardProcessor = operationCreateBoardProcessor;
        this.operationDeactivateMemberService = operationDeactivateMemberService;
        this.operationDeactivateMemberProcessor = operationDeactivateMemberProcessor;
        this.operationDeleteBoardService = operationDeleteBoardService;
        this.operationDeleteBoardProcessor = operationDeleteBoardProcessor;
        this.operationDeleteWorkspaceService = operationDeleteWorkspaceService;
        this.operationDeleteWorkspaceProcessor = operationDeleteWorkspaceProcessor;
        this.boardProjectService = boardProjectService;
        this.workspaceProjectService = workspaceProjectService;
        this.invitationProjectService = invitationProjectService;
    }

    // Comprueba si existen operaciones anteriores del mismo Workspace que todavía
    // no hayan alcanzado un estado que permita continuar con las operaciones
    // posteriores.
    //
    // Esta validación es necesaria porque, aunque la SAGA se ejecuta de forma
    // síncrona, una operación puede fallar y quedar pendiente de reejecución
    // automática.
    //
    // En ese escenario, el Scheduler podría recuperar una operación posterior antes
    // de que la operación anterior haya terminado su proceso de compensación y
    // reejecución.
    private boolean hasPreviousUncompletedOperations(Operation operation) {

        return operationProjectService
                .existsPreviousOperationsByWorkspaceIdAndCreatedAt(
                        operation.getWorkspaceId(),
                        operation.getCreatedAt());
    }

    // Ejecutar todas las operaciones que fueron compensadas
    @Override
    public void executeAllCompensatedOperations() {

        // Estados de las operaciones que pueden volver a ejecutarse automáticamente
        List<OperationStatus> permitedStatues = new ArrayList<>();
        permitedStatues.add(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
        permitedStatues.add(OperationStatus.COMPENSATION_COMPLETED);

        // Obtener operaciones pendientes ordenadas por fecha de creación
        List<Operation> pendingOperations = operationProjectService
                .findOperationsByStatusesListOrderByWorkspaceIdAscAndCreatedAtAsc(permitedStatues);

        // La ejecución síncrona permite que cada operación complete su flujo de
        // procesamiento antes de continuar con la siguiente operación del bucle.
        //
        // Sin embargo, este comportamiento no garantiza el orden de ejecución entre
        // operaciones dependientes que fueron recuperadas en diferentes ejecuciones
        // del Scheduler. Por ello, se comprueba si existen operaciones anteriores
        // del mismo Workspace que todavía no hayan finalizado.
        for (Operation operation : pendingOperations) {

            // Evita ejecutar una operación mientras existan operaciones anteriores
            // del mismo Workspace que todavía puedan afectar su consistencia.
            if (hasPreviousUncompletedOperations(operation)) {
                log.info(
                        "Operation {} is waiting for previous operations",
                        operation.getId());

                // Omite esta operación y permite continuar con otras operaciones
                // pertenecientes a Workspaces independientes.
                continue;
            }

            switch (operation.getType()) {
                case ACCEPT_INVITATION:
                    // El parametro automatic en "true", indica que se va a sumar 1 en el contador
                    // de "Intentos automaticos" de la operación en la BD, nada más eso implica

                    // Ejecuta logica de negocio para aceptar la invitacion en este microservicio
                    operationAcceptInvitationService.applyAcceptInvitation(operation.getId(), true);

                    // Ejecuta la logica para llamar a los endpoints del microservicio Workflow para
                    // ejecutar la operacion

                    // En el caso de que haya un retraso, el metodo processAcceptInvitationOperation
                    // llama al endpoint para revertir la operacion desde el microservicio Workflow
                    operationAcceptInvitationProcessor.processAcceptInvitationOperation(operation.getId());

                    // Recordar que si la operación queda en un estado LOCAL_COMPENSATION_COMPLETED
                    // o COMPENSATION_COMPLETED, va a tener una nueva fecha de reintento definida
                    // dentro del metodo, en este caso, applyAcceptInvitation
                    break;

                // Con los demás metodos seria similar
                case CHANGE_ROLE_MEMBER:
                    operationChangeRoleMemberService.applyChangeRoleMember(operation.getId(), true);
                    operationChangeRoleMemberProcessor.processChangeRoleMemberOperation(operation.getId());
                    break;

                case CREATE_BOARD:
                    // PROBLEMA
                    // Cuando se crea un tablero, el problema comienza cuando el usuario
                    // utiliza el mismo nombre del tablero cuando el microservicio Workflow esta
                    // apagado e introduce los mismos datos para crear un tablero, el nombre de los
                    // tableros es unico (no puede existir 2 tableros con el mismo nombre) por ID de
                    // workspace

                    // Aplicar nuevamente la lógica de negocio local.
                    operationCreateBoardService.applyCreateBoard(operation.getId(), true);

                    // Ejecutar la operación en Workflow.
                    operationCreateBoardProcessor.processCreateBoardOperation(operation.getId());

                    break;
                case DEACTIVATE_MEMBER:
                    operationDeactivateMemberService.applyDeactivateMember(operation.getId(), true);
                    operationDeactivateMemberProcessor.processDeactivateMemberOperation(operation.getId());

                    // Cancelar las demás operaciones relacionadas con el miembro

                    UUID memberId = operation.getMemberId();

                    operationProjectService.cancelAllOperationsByMemberId(memberId, operation.getId());

                    operationDeactivateMemberProcessor.processCancelOperationsByMemberId(
                            memberId,
                            operation.getId());
                    break;

                case DELETE_BOARD:
                    operationDeleteBoardService.applyDeleteBoard(operation.getId(), true);

                    operationDeleteBoardProcessor.processDeleteBoardOperation(operation.getId());

                    // Cancelar las demás operaciones relacionadas con el tablero.
                    UUID boardId = operation.getBoardId();

                    // La operación que sera excluida sera la misma operacion por ID

                    // Recordar que cambia el estado a CANCELED y CleanupStatus a EXECUTION_PENDING,
                    // indicando que las operaciones fueron limpiadas
                    operationProjectService.cancelAllOperationsByBoardId(
                            boardId,
                            operation.getId());

                    // Realiza una llamada al microservicio Workflow para marcar las operaciones
                    // como CANCELED
                    operationDeleteBoardProcessor.processCancelOperationsByBoardId(
                            boardId,
                            operation.getId());

                    break;

                case DELETE_WORKSPACE:
                    operationDeleteWorkspaceService.applyDeleteWorkspace(operation.getId(), true);
                    operationDeleteWorkspaceProcessor.processDeleteWorkspaceOperation(operation.getId());

                    // Cancelar las demás operaciones relacionadas con el espacio de trabajo.
                    UUID workspaceId = operation.getWorkspaceId();

                    operationProjectService.cancelAllOperationsByWorkspaceId(workspaceId, operation.getId());
                    operationDeleteWorkspaceProcessor.processCancelOperationsByWorkspaceId(workspaceId,
                            operation.getId());

                    break;

                default:
                    break;
            }
        }
    }

    // Borrar todas los tableros y espacios de trabajo que estan inactivos y estado
    // de la operacion en LOGICAL_DELETE_COMPLETED
    @Override
    public void deleteAllBoardsByStatusFalseAndWorkspacesByStatusFalse() {
        // De todas las operaciones obtenidas el tablero (board) asociado a la operación
        // por ID de tablero, tiene en su campo "status" el valor false, indicando que
        // se volvio inaccesible para los usuarios, debido a que aún no fue eliminado
        // fisicamente de la base de datos

        // Obtener todas las operaciones cuyo estado sea LOGICAL_DELETE_COMPLETED y de
        // tipo DELETE_BOARD
        List<Operation> completedBoardsOperations = operationProjectService
                .findOperationsByOperationTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
                        OperationType.DELETE_BOARD, OperationStatus.LOGICAL_DELETE_COMPLETED);

        // Realizar el borrado fisico de datos en la base de datos
        for (Operation operation : completedBoardsOperations) {

            // Utiliza un bloque try-catch porque cuando se elimina un tablero, el metodo
            // deleteBoardByIdAndOwnerUserId puede caer en una excepcion que hereda de
            // Runtime exception; es por ello que para no detener el proceso de ejecución
            // por cada uno de los tableros se utiliza el bloque try-catch
            try {
                // Se eliminan físicamente los Boards que fueron desactivados
                boardProjectService.deleteBoardByIdAndOwnerUserId(
                        operation.getBoardId(),
                        operation.getOwnerUserId());

                // Registrar que la eliminación física ya fue realizada.
                // El estado cambia a COMPLETED
                operation.setStatus(OperationStatus.COMPLETED);
                operationProjectService.saveOperation(operation);

                // Toma las mismas excepciones que se esperan ser lanzadas desde el metodo
                // deleteBoardByIdAndOwnerUserId
            } catch (WorkspaceNotFoundException | BoardNotFoundException exception) {
                log.info("Board {} was already physically deleted", operation.getBoardId());
            }
        }

        List<Operation> completedWorkspaceOperations = operationProjectService
                .findOperationsByOperationTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
                        OperationType.DELETE_WORKSPACE, OperationStatus.LOGICAL_DELETE_COMPLETED);

        for (Operation operation : completedWorkspaceOperations) {

            try {
                workspaceProjectService.deleteWorkspaceByIdAndOwnerUserId(
                        operation.getWorkspaceId(),
                        operation.getOwnerUserId());

                operation.setStatus(OperationStatus.COMPLETED);
                operationProjectService.saveOperation(operation);
            } catch (WorkspaceNotFoundException exception) {
                log.info("Workspace {} was already physically deleted", operation.getWorkspaceId());
            }
        }
    }

    // Como este metodo estara dentro de un scheduler, se coloca Transactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAllCompletedOperations() {
        operationProjectService.deleteAllOperationsByStatus(
                OperationStatus.COMPLETED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAllCanceledOperations() {

        // Recordar que en el campo cleanupStatus se establece COMPLETED cuando la
        // operación esta lista para ser eliminada fisicamente de la base de datos
        operationProjectService.deleteAllOperationsByStatusAndCleanupStatus(
                OperationStatus.CANCELED,
                CleanupStatus.COMPLETED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAllAcceptedInvitations() {
        invitationProjectService.deleteAllAcceptedInvitations();
    }
}
