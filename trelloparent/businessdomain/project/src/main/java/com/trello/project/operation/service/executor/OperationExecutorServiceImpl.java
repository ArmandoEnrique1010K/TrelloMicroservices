package com.trello.project.operation.service.executor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
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
            WorkspaceProjectService workspaceProjectService) {
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

    @Override
    public void executeAllCompensatedOperations() {

        // Estados de las operaciones que pueden volver a ejecutarse automáticamente
        List<OperationStatus> permitedStatues = new ArrayList<>();
        permitedStatues.add(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
        permitedStatues.add(OperationStatus.COMPENSATION_COMPLETED);

        // Obtener operaciones pendientes ordenadas por fecha del próximo intento
        List<Operation> pendingOperations = operationProjectService
                .findOperationsByStatusesListOrderByNextAutomaticAttemptAtAscWorkspaceIdAsc(permitedStatues);

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
                    // No se llama al metodo para registrar la operacion, dado que ya se encuentra
                    // en la base de datos

                    // El parametro automatic en "true", indica que se va a sumar 1 en el contador
                    // de "Intentos automaticos" de la operación en la BD, nada más eso implica

                    // Ejecuta logica de negocio para aceptar la invitacion en este microservicio
                    operationAcceptInvitationService.applyAcceptInvitation(operation.getId(), true);

                    // Ejecuta la logica para llamar a los endpoints del microservicio Workflow para
                    // ejecutar la operacion
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
                    operationCreateBoardService.applyCreateBoard(operation.getId(), true);
                    operationCreateBoardProcessor.processCreateBoardOperation(operation.getId());
                    break;

                case DEACTIVATE_MEMBER:
                    operationDeactivateMemberService.applyDeactivateMember(operation.getId(), true);
                    operationDeactivateMemberProcessor.processDeactivateMemberOperation(operation.getId());
                    break;

                case DELETE_BOARD:
                    operationDeleteBoardService.applyDeleteBoard(operation.getId(), true);
                    operationDeleteBoardProcessor.processDeleteBoardOperation(operation.getId());
                    break;

                case DELETE_WORKSPACE:
                    operationDeleteWorkspaceService.applyDeleteWorkspace(operation.getId(), true);
                    operationDeleteWorkspaceProcessor.processDeleteWorkspaceOperation(operation.getId());
                    break;

                default:
                    break;
            }
        }
    }

    // Borrar todas los tableros y espacios de trabajo que estan inactivos y estado
    // de la operacion en COMPLETED
    @Override
    public void deleteAllBoardsByStatusFalseAndWorkspacesByStatusFalse() {

        // Obtener todas las operaciones cuyo estado sea COMPLETED y de tipo
        // DELETE_BOARD y DELETE_WORKSPACE

        // Obviamente de todas las operaciones obtenidas, cuando se obtiene el tablero o
        // espacio de trabajo, tienen en el campo "status" el valor false,
        // lo que significa que los usuarios ya no podran acceder a ese tablero o
        // espacio de trabajo

        // Tambien cuando se guarda una operacion de "DELETE_WORKSPACE" y ha tenido
        // exito, el estado del workspace esta en false, y cada uno de sus boards
        // tambien estan en false

        // Primero se eliminan físicamente los Boards que fueron desactivados
        // y cuya operación de eliminación terminó correctamente.
        List<Operation> completedBoardsOperations = operationProjectService
                .findOperationsByOperationTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
                        OperationType.DELETE_BOARD, OperationStatus.COMPLETED);

        // Realizar el borrado fisico de datos en la base de datos
        for (Operation operation : completedBoardsOperations) {

            boardProjectService.deleteBoardByIdAndOwnerUserId(
                    operation.getBoardId(),
                    operation.getOwnerUserId());

            // Registrar que la eliminación física ya fue realizada.
            // operation.setPhysicallyDeletedAt(LocalDateTime.now());
            // operationProjectService.updateOperation(operation);

            // CASO ESPECIAL: EL ESTADO DEBE CAMBIAR A COMPLETED
            operation.setStatus(OperationStatus.COMPLETED);
            operationProjectService.saveOperation(operation);
        }

        List<Operation> completedWorkspaceOperations = operationProjectService
                .findOperationsByOperationTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
                        OperationType.DELETE_WORKSPACE, OperationStatus.COMPLETED);

        for (Operation operation : completedWorkspaceOperations) {

            workspaceProjectService.deleteWorkspaceByIdAndOwnerUserId(
                    operation.getWorkspaceId(),
                    operation.getOwnerUserId());

            // Registrar que la eliminación física ya fue realizada.
            operation.setStatus(OperationStatus.COMPLETED);
            operationProjectService.saveOperation(operation);
        }
    }

    @Override
    public void deleteAllCompletedOperations() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllCompletedOperations'");
    }

}
