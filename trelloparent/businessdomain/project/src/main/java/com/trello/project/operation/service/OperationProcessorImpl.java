package com.trello.project.operation.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.exception.RemoteBoardAccessAlreadyExistsException;
import com.trello.project.client.exception.RemoteBoardAccessNotFoundException;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.OperationCompensatedException;
import com.trello.project.exception.OperationManualReviewException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
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
                .orElseThrow(OperationNotFoundException::new);

        // 2. Evitar reprocesar operaciones ya completadas.
        // Solo se procesan operaciones pendientes de ejecución original.
        if (operation.getStatus() != OperationStatus.PENDING
                && operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        // 3. Marcar que se está procesando la operación.
        operation.setStatus(operation.getStatus() == OperationStatus.PENDING ? OperationStatus.PROCESSING
                : OperationStatus.COMPENSATION_PROCESSING);
        operation.setManualAttempts(operation.getManualAttempts() + 1);

        // Aumentar el tiempo para la realización automatica de la tarea EN 30 SEGUNDOS
        // en caso de que falle la realización manual
        operation.setNextAutomaticAttemptAt(LocalDateTime.now().plusSeconds(30));

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
            operation.setStatus(operation.getStatus() == OperationStatus.PROCESSING ? OperationStatus.COMPLETED
                    : OperationStatus.COMPENSATED);
            operation.setCompletedAt(LocalDateTime.now());

            // Borrar intento automatico
            operation.setNextAutomaticAttemptAt(null);
            operationProjectService.saveOperation(operation);

            // En este caso todo paso bien y no se mostrara ninguna excepción
            return;

        } catch (ServiceUnavailableException e) {
            // La operación original falló; la compensación está pendiente.
            operation.setStatus(OperationStatus.COMPENSATION_PENDING);
            operationProjectService.saveOperation(operation);

            try {
                operationService.revertInvitationAcceptance(operationId);

                // Metodo para revertir permiso de acceso cuando se añade un usuario en el
                // microservicio Workflow
                try {
                    if (operation.getMemberActive() == null) {
                        workflowClientService.revertAddBoardAccess(operationId);
                    } else if (operation.getMemberActive() == false) {
                        workflowClientService.revertActivateBoardAccess(operationId);
                    } else {
                        // Aunque es imposible que llegue a pasar
                        throw new BusinessRuleException();
                    }
                    // Cualquier excepcion caera en un ServiceUnavailableException
                    // Como si el llamado es cuando se ha acabado el tiempo de respuesta de
                    // resilence
                    // Obvio que no pasara porque el tiempo limite va a ser de 10 segundos en
                    // lugar de los 4 segundos
                } catch (ServiceUnavailableException exception) {
                    operation.setStatus(OperationStatus.MANUAL_REVIEW);
                    operation.setCompletedAt(null);
                    operationProjectService.saveOperation(operation);
                    throw new BusinessRuleException();
                }

            } catch (Exception compensationException) {

                // No cambiar a MANUAL_REVIEW automáticamente.
                // Permitir que el scheduler vuelva a intentar la compensación.
                log.error(
                        "Compensation failed for operation {}. "
                                + "It remains pending for recovery.",
                        operationId,
                        compensationException);

                // No ocultar el estado real: la compensación sigue pendiente.
                // No se puede recuperar porque si llego aqui es porque cayo en un exception
                // throw new CompensationPendingException();

                throw new BusinessRuleException();
            }

            // Solo si revertInvitationAcceptance terminó correctamente.
            throw new OperationCompensatedException();

        } catch (RemoteBoardAccessNotFoundException | RemoteBoardAccessAlreadyExistsException
                | BusinessRuleException e) {
            // En resumen, si la respuesta del endpoint que se llama desde este
            // microservicio, cae en una excepción que sea de tipo
            // BoardAccessNotFountException o BoardAccessAlreadyExistsException, se tendria
            // que recurrir a la intervención humana para la revision directa en la base de
            // datos

            // BoardAccessNotFountException y BoardAccessAlreadyExistsException se obtienen
            // del microservicio Workflow, en la carpeta client, dentro del paquete
            // exception se utiliza un ErrorDecoder para capturar la excepción y mapearla a
            // una excepción personalizada segun la propiedad "type" de la excepción
            // obtenida desde el microservicio Workflow
            log.error(
                    "Unexpected error while processing operation {}",
                    operationId,
                    e);

            // En este caso no se va a revertir la operación, requiere de intervención
            // manual
            operation.setStatus(OperationStatus.MANUAL_REVIEW);
            operationProjectService.saveOperation(operation);

            // 500
            throw new OperationManualReviewException();

        } catch (Exception ex) {
            log.error(
                    "Unexpected error while processing operation {}",
                    operationId,
                    ex);

            // operation.setStatus(OperationStatus.MANUAL_REVIEW);
            // operationProjectService.saveOperation(operation);

            // // 409
            throw new BusinessRuleException();
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
            // throw new IllegalStateException(
            // "The member was already active before the operation: "
            // + operation.getId());
            log.error("The member was already active before the operation: " + operation.getId());
        }
    }
}