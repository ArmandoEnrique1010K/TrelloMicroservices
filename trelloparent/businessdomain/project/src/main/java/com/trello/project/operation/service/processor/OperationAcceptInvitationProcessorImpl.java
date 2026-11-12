package com.trello.project.operation.service.processor;

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
import com.trello.project.enums.OperationType;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.InvitationNotFoundException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationAcceptInvitationService;
import com.trello.project.operation.service.utils.AbstractOperationProcessor;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
// Extiende la clase abstracta para reutilizar el algoritmo general
// de ejecución y compensación definido mediante Template Method.
public class OperationAcceptInvitationProcessorImpl
        extends AbstractOperationProcessor
        implements OperationAcceptInvitationProcessor {

    // private final OperationUtils operationUtils;
    // private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final InvitationProjectService invitationProjectService;
    private final OperationAcceptInvitationService operationAcceptInvitationService;

    public OperationAcceptInvitationProcessorImpl(
            OperationUtils operationUtils,
            OperationProjectService operationProjectService,
            OperationAcceptInvitationService operationAcceptInvitationService,
            WorkflowClientService workflowClientService,
            InvitationProjectService invitationProjectService) {

        // Instancia desde AbstractOperationProcessor
        super(operationUtils, operationProjectService);

        // this.operationUtils = operationUtils;
        // this.operationProjectService = operationProjectService;

        this.workflowClientService = workflowClientService;
        this.invitationProjectService = invitationProjectService;
        this.operationAcceptInvitationService = operationAcceptInvitationService;
    }

    // Valida y delega el procesamiento de la operación a la clase abstracta
    // Procesa una operación de aceptación de invitación previamente registrada
    @Override
    public void processAcceptInvitationOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, InvitationNotFoundException, OperationNotPendingException,
            OperationManualReviewException {

        // 1. Recuperar la operación persistida mediante su identificador.
        // Se utiliza el metodo definido en AbstractOperationProcessor
        Operation operation = findOperation(operationId);

        // Verificar que la operación corresponda al proceso de aceptación
        // de invitaciones antes de iniciar su procesamiento.
        if (operation.getType() != OperationType.ACCEPT_INVITATION) {
            throw new OperationConflictException();
        }

        // 2. Delegar la ejecución de la operación y la gestión de sus posibles
        // fallos al método encargado de coordinar el flujo de la saga.
        processOriginalOperation(operation);
    }

    //

    // Este método es generado cuando la clase extiende de
    // AbstractOperationProcessor

    // Ejecuta la operación de aceptación de invitación mediante el cliente
    // del microservicio Workflow, según el estado previo del miembro.
    @Override
    protected void executeOriginalOperation(Operation operation) {
        // 1. Recuperar la invitación para obtener el tablero asociado
        // y el rol que debe asignarse al usuario.
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        operation.getInvitationId(),
                        operation.getUserId());

        UUID boardId = invitation.getBoard().getId();
        UUID userId = operation.getUserId();

        // 2. Convertir el rol del microservicio Project al enum que utiliza
        // Workflow para gestionar los permisos de acceso al tablero.
        WorkflowRole workflowRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                invitation.getRole());

        // Cambiar el rol antiguo
        WorkflowRole oldRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                operation.getOldRole());

        // Recuperar el estado de actividad del miembro antes de la operación.
        // Este valor determina qué acción debe ejecutarse en Workflow.
        Boolean memberWasActive = operation.getMemberActive();

        if (memberWasActive == null) {
            // No existía un miembro previo en Project.
            // Crear el permiso de acceso correspondiente en Workflow.
            workflowClientService.addBoardAccess(
                    boardId,
                    userId,
                    workflowRole,
                    operation.getId());
        } else if (!memberWasActive) {
            // Existía un miembro, pero estaba inactivo.
            // Reactivar su permiso de acceso en Workflow.
            workflowClientService.activateBoardAccess(
                    boardId,
                    userId,
                    workflowRole,
                    // Debe enviar el rol antiguo para guardarlo
                    oldRole,
                    operation.getId());
        } else {
            // El miembro ya estaba activo antes de aceptar la invitación.
            // La operación no puede continuar bajo esta condición de negocio.
            throw new BusinessRuleException();
        }
    }

    // Revierte los cambios locales realizados en Project como primera
    // etapa de la compensación de la operación.
    @Override
    protected void processLocalCompensation(Operation operation) {

        // La compensación local solo puede iniciarse cuando la operación
        // se encuentra en COMPENSATION_PENDING.
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {
            // 1. Registrar el inicio de la compensación local.
            operationUtils.markLocalCompensationInProgress(operation);

            // 2. Revertir los cambios de negocio locales asociados a la
            // aceptación de la invitación.
            operationAcceptInvitationService
                    .revertAcceptInvitation(operation.getId());

            // 3. Comprobar el estado persistido después de la compensación.
            // La operación puede haber sido marcada para revisión manual
            // durante la reversión de los cambios locales.
            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.MANUAL_INTERVENTION_REQUIRED) {
                throw new OperationManualReviewException();
            }

            // 4. Registrar que la compensación local terminó correctamente.
            // El siguiente paso del proceso será compensar los cambios
            // remotos en Workflow, sin repetir esta reversión local.
            operationUtils.markLocalCompensationCompleted(updatedOperation);
            // } catch (OperationManualReviewException e) {
            // log.info("EXCEPTION: " + e);
        } catch (Exception exception) {
            // Si se produce un error durante la compensación local, marcar
            // la operación para revisión manual, ya que no se debe asumir
            // que los datos locales quedaron en un estado consistente.
            operationUtils.markManualReview(operation, exception);

            log.info("EXCEPTION: " + exception);
            throw new OperationManualReviewException();
        }
    }

    // Revierte en Workflow los cambios remotos después de confirmar
    // que la compensación local ya se completó.
    @Override
    protected void processRemoteCompensation(Operation operation) {
        // La compensación remota solo puede ejecutarse cuando los cambios
        // locales ya se han revertido correctamente.
        if (operation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        try {
            if (operation.getMemberActive() == null) {
                // El miembro no existía antes de la aceptación.
                // Revertir la creación del permiso de acceso en Workflow.
                workflowClientService.revertAddBoardAccess(
                        operation.getId());
            } else if (Boolean.FALSE.equals(
                    operation.getMemberActive())) {
                // El miembro existía, pero estaba inactivo.
                // Revertir la reactivación del permiso de acceso en Workflow.
                workflowClientService.revertActivateBoardAccess(
                        operation.getId());
            } else {
                // El estado previo del miembro no es compatible con las
                // compensaciones definidas para esta operación.
                // Solicitar revisión manual en lugar de ejecutar una reversión
                // que podría alterar incorrectamente los permisos de acceso.
                operationUtils.markManualReview(
                        operation,
                        new IllegalStateException(
                                "Estado previo incompatible con la compensación"));

                throw new OperationManualReviewException();
            }

            // La compensación local y la remota han finalizado.
            // Registrar el estado final de la operación compensada.
            operationUtils.markCompensationCompleted(operation);

        } catch (ServiceUnavailableException exception) {

            // Workflow no está disponible para completar la compensación remota.
            // Los cambios locales ya se revirtieron, por lo que debe conservarse
            // LOCAL_COMPENSATION_COMPLETED para reintentar desde la etapa remota.
            operationUtils.markLocalCompensationCompleted(operation);

            // Informar que la operación original ha sido compensada localmente,
            // pero que la compensación remota todavía requiere ser completada.
            log.info("EXCEPTION: " + exception);
            throw new OperationCompensatedException();
        } catch (RemoteBoardAccessAlreadyExistsException | RemoteBoardAccessNotFoundException exception) {
            // // La respuesta de Workflow indica una inconsistencia funcional
            // // relacionada con los permisos de acceso al tablero.
            // // Registrar la operación para revisión manual, ya que no se debe
            // // asumir que la inconsistencia puede resolverse automáticamente.

            operationUtils.markManualReview(operation, exception);
            log.info("EXCEPTION: " + exception);
            throw new OperationManualReviewException();
        }
    }
}