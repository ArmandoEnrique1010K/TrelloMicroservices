package com.trello.project.operation.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationProcessorImpl implements OperationProcessor {
    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final InvitationProjectService invitationProjectService;
    private final OperationService operationService;

    public OperationProcessorImpl(OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            InvitationProjectService invitationProjectService, OperationService operationService) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.invitationProjectService = invitationProjectService;
        this.operationService = operationService;
    }

    // No se coloca un @Transactional porque este método se encarga de hacer un
    // llamado al cliente
    @Override
    public void processOperation(UUID operationId) {
        // 1. Recuperar la operación persistida en Project.
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Operation not found: " + operationId));

        // 2. Evitar reprocesar operaciones ya completadas.
        // Solo se procesan operaciones pendientes de ejecución original.
        if (operation.getStatus() != OperationStatus.PENDING) {
            return;
        }

        // 3. Marcar que se está procesando la operación.
        operation.setStatus(OperationStatus.PROCESSING);
        operation.setAttempts(operation.getAttempts() + 1);
        operationProjectService.saveOperation(operation);

        try {
            // 4. Ejecutar según el tipo de operación.
            switch (operation.getType()) {

                case ACCEPT_INVITATION ->
                    processAcceptInvitation(operation);

                // Aunque no va a ser necesario esta excepción porque nunca caera ahí
                default -> throw new IllegalArgumentException(
                        "Unsupported operation type: "
                                + operation.getType());
            }

            // 5. Workflow respondió correctamente.
            // La operación original finalizó correctamente.
            operation.setStatus(OperationStatus.COMPLETED);
            operation.setCompletedAt(LocalDateTime.now());
            operationProjectService.saveOperation(operation);

        } catch (ServiceUnavailableException e) {
            // La operación original falló; la compensación está pendiente.
            operation.setStatus(OperationStatus.COMPENSATION_PENDING);
            operationProjectService.saveOperation(operation);

            try {
                operationService.revertInvitationAcceptance(operationId);
            } catch (Exception compensationException) {

                // No cambiar a MANUAL_REVIEW automáticamente.
                // Permitir que el scheduler vuelva a intentar la compensación.
                log.error(
                        "Compensation failed for operation {}. "
                                + "It remains pending for recovery.",
                        operationId,
                        compensationException);
            }
        } catch (Exception e) {
            log.error(
                    "Unexpected error while processing operation {}",
                    operationId,
                    e);

            // En este caso no se va a revertir la operación
            operation.setStatus(OperationStatus.MANUAL_REVIEW);
            operationProjectService.saveOperation(operation);
        }
    }

    private void processAcceptInvitation(Operation operation) {

        // Recuperar la invitación para obtener el tablero y el rol.
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        operation.getInvitationId(),
                        operation.getUserId());

        UUID boardId = invitation.getBoard().getId();

        WorkflowRole workflowRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                invitation.getRole());

        // memberActive == null: no existía un Member antes de aceptar.
        // memberActive == false: existía un Member inactivo.
        // memberActive == true: existía un Member activo
        Boolean memberWasActive = operation.getMemberActive();

        if (memberWasActive == null) {

            // Crear el acceso en Workflow.
            workflowClientService.addBoardAccess(
                    boardId,
                    workflowRole,
                    operation.getId());

        } else if (!memberWasActive) {

            // Reactivar el acceso en Workflow.
            workflowClientService.activateBoardAccess(
                    boardId,
                    operation.getUserId(),
                    workflowRole,
                    operation.getId());

        } else {
            // Solamente si el miembro es verdadero se lanzara otra excepción
            throw new IllegalStateException(
                    "The member was already active before the operation: "
                            + operation.getId());
        }
    }
}