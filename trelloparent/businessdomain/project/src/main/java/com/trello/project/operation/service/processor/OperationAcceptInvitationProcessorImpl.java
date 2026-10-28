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
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationAcceptInvitationService;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationAcceptInvitationProcessorImpl implements OperationAcceptInvitationProcessor {
    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final InvitationProjectService invitationProjectService;
    private final OperationAcceptInvitationService operationAcceptInvitationService;
    private final OperationUtils operationUtils;

    public OperationAcceptInvitationProcessorImpl(
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            InvitationProjectService invitationProjectService,
            OperationAcceptInvitationService operationAcceptInvitationService,
            OperationUtils operationUtils) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.invitationProjectService = invitationProjectService;
        this.operationAcceptInvitationService = operationAcceptInvitationService;
        this.operationUtils = operationUtils;
    }

    // Procesa una operación de aceptación de invitación previamente registrada.
    @Override
    public void processAcceptInvitationOperation(UUID operationId) throws OperationNotFoundException,
            OperationConflictException {
        // 1. Recuperar la operación persistida mediante su identificador.
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // Verificar que la operación corresponda al proceso de aceptación
        // de invitaciones antes de iniciar su procesamiento.
        if (operation.getType() != OperationType.ACCEPT_INVITATION) {
            throw new OperationConflictException();
        }

        // 2. Delegar la ejecución de la operación y la gestión de sus posibles
        // fallos al método encargado de coordinar el flujo de la saga.
        processOriginalOperation(operation);
    }

    // Coordina la ejecución de la operación original y, ante determinados
    // fallos, inicia el proceso de compensación local y remota.
    private void processOriginalOperation(Operation operation)
            throws BusinessRuleException, OperationNotFoundException,
            OperationCompensatedException, OperationManualReviewException {

        // 1. Marcar la operación como EXECUTING antes de ejecutar la lógica
        // de negocio y comunicarse con el microservicio Workflow.
        operationUtils.markExecuting(operation);

        try {
            // 2. Ejecutar la operación original en el microservicio Workflow.
            processAcceptInvitation(operation);

            // 3. Registrar que la operación finalizó correctamente.
            operationUtils.markCompleted(operation);

        } catch (ServiceUnavailableException e) {
            // La comunicación con Workflow no está disponible y el resultado
            // de la operación remota puede ser desconocido.

            // Iniciar la compensación para revertir los cambios locales y,
            // posteriormente, intentar compensar los cambios remotos.
            operationUtils.markCompensationPending(operation);

            try {
                // 4. Intentar compensar inmediatamente los cambios locales.
                // Si esta compensación termina, su estado permitirá continuar
                // desde la compensación remota en futuros reintentos.
                processLocalCompensation(operation);

                // 5. Intentar revertir los cambios realizados en Workflow.
                processRemoteCompensation(operation);
            } catch (ServiceUnavailableException compensationException) {
                // Si falla la comunicación durante la compensación remota,
                // consultar el estado persistido para determinar si la
                // compensación distribuida llegó a completarse.
                Operation updatedOperation = operationProjectService
                        .findOperationById(operation.getId())
                        .orElseThrow(OperationNotFoundException::new);

                // Si la compensación ya terminó, informar que la operación
                // original fue compensada, aunque se haya producido una
                // excepción durante el proceso de compensación.
                if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                    throw new OperationCompensatedException();
                }

                // Si la compensación no llegó a completarse, propagar el error
                // de comunicación para que se gestione según el flujo previsto.
                throw compensationException;
            }

            // 6. Comprobar el estado persistido después de intentar ambas
            // compensaciones, ya que la operación original no se completó.
            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                throw new OperationCompensatedException();
            }

        } catch (RemoteBoardAccessNotFoundException
                | RemoteBoardAccessAlreadyExistsException e) {
            // La respuesta de Workflow indica una inconsistencia funcional
            // relacionada con los permisos de acceso al tablero.
            // Registrar la operación para revisión manual, ya que no se debe
            // asumir que la inconsistencia puede resolverse automáticamente.
            operationUtils.markManualReview(operation, e);

            throw new OperationManualReviewException();

        } catch (Exception e) {
            // Cualquier otro error se registra para revisión manual, dado que
            // no se ha definido aquí una estrategia segura de recuperación
            // automática para esta excepción.
            operationUtils.markManualReview(operation, e);

            throw new BusinessRuleException();
        }
    }

    // Ejecuta la operación de aceptación de invitación mediante el cliente
    // del microservicio Workflow, según el estado previo del miembro.
    private void processAcceptInvitation(Operation operation) throws BusinessRuleException {
        // 1. Recuperar la invitación para obtener el tablero asociado
        // y el rol que debe asignarse al usuario.
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        operation.getInvitationId(),
                        operation.getUserId());

        UUID boardId = invitation.getBoard().getId();

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
                    workflowRole,
                    operation.getId());
        } else if (!memberWasActive) {
            // Existía un miembro, pero estaba inactivo.
            // Reactivar su permiso de acceso en Workflow.
            workflowClientService.activateBoardAccess(
                    boardId,
                    operation.getUserId(),
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
    private void processLocalCompensation(Operation operation) throws OperationNotPendingException,
            OperationNotFoundException, OperationManualReviewException {

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
        } catch (OperationManualReviewException e) {
            throw e;
        } catch (Exception e) {
            // Si se produce un error durante la compensación local, marcar
            // la operación para revisión manual, ya que no se debe asumir
            // que los datos locales quedaron en un estado consistente.
            operationUtils.markManualReview(operation, e);

            throw new OperationManualReviewException();
        }
    }

    // Revierte en Workflow los cambios remotos después de confirmar
    // que la compensación local ya se completó.
    private void processRemoteCompensation(Operation operation) throws OperationNotPendingException,
            OperationManualReviewException, OperationCompensatedException {

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

        } catch (ServiceUnavailableException e) {

            // Workflow no está disponible para completar la compensación remota.
            // Los cambios locales ya se revirtieron, por lo que debe conservarse
            // LOCAL_COMPENSATION_COMPLETED para reintentar desde la etapa remota.
            operationUtils.markLocalCompensationCompleted(operation);

            // Informar que la operación original ha sido compensada localmente,
            // pero que la compensación remota todavía requiere ser completada.
            throw new OperationCompensatedException();
        }
    }
}