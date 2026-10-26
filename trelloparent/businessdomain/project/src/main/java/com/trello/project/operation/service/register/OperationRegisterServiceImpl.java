package com.trello.project.operation.service.register;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationRegisterServiceImpl implements OperationRegisterService {

    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;

    public OperationRegisterServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
    }

    /**
     * Registra una operación para aceptar una invitación.
     *
     * Valida las condiciones de negocio, comprueba si existe una operación
     * previa que pueda reutilizarse y registra el estado inicial junto con
     * la información necesaria para ejecutar o compensar la operación.
     *
     * La operación se registra antes de que otro servicio aplique los cambios
     * de negocio sobre Invitation y Member.
     *
     * Si se produce una excepción que provoque el rollback, los cambios
     * realizados dentro de esta transacción se revierten.
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAcceptInvitation(UUID invitationId, UUID recipientUserId)
            throws InvitationConfirmedException, OperationNotPendingException {

        // 1. Validar las condiciones de negocio antes de comprobar
        // si existe una operación previa que pueda reutilizarse.
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        invitationId,
                        recipientUserId);

        // La invitación solo puede aceptarse si todavía no ha sido confirmada.
        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // Capturar el rol actual de la invitación para registrar el rol
        // que se asignará al usuario al aceptar la invitación.
        Role invitationRole = invitation.getRole();

        // 2. Buscar una operación previa de aceptación de la invitación que
        // pueda reutilizarse para continuar un intento anterior.
        Optional<Operation> existingOperation = operationProjectService
                .findLatestOperationByInvitationIdAndType(
                        invitationId,
                        OperationType.ACCEPT_INVITATION);

        // Si existe una operación previa, evaluar su estado para determinar
        // si se puede reutilizar, si el proceso debe rechazarse o si corresponde
        // registrar una nueva operación.
        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            switch (existing.getStatus()) {
                case REGISTERED, LOCAL_COMPENSATION_COMPLETED, COMPENSATION_COMPLETED -> {
                    // Reutilizar la operación existente para reintentar el proceso
                    // conservando su identificador.
                    //
                    // Actualizar el nuevo rol con el valor actual de la invitación,
                    // ya que este puede haber cambiado desde el intento anterior.
                    existing.setNewRole(invitation.getRole());

                    // Devolver la operación existente para continuar el flujo
                    // utilizando el mismo identificador.
                    return existing;
                }

                // La operación se encuentra en ejecución, pendiente de ejecución
                // o compensación, o requiere intervención manual. No se permite
                // iniciar otro intento mientras esta operación no se resuelva.
                case EXECUTION_PENDING,
                        EXECUTING,
                        COMPENSATION_PENDING,
                        LOCAL_COMPENSATION_IN_PROGRESS,
                        MANUAL_INTERVENTION_REQUIRED ->
                    throw new OperationNotPendingException();

                case COMPLETED -> {
                    // La operación anterior finalizó correctamente. No se reutiliza;
                    // se continúa con el flujo para registrar una nueva operación
                    // de aceptación de la invitación.
                }
            }
        }

        // 3. Obtener el tablero asociado a la invitación.
        Board findedBoard = invitation.getBoard();
        UUID boardId = findedBoard.getId();

        // 4. Consultar si ya existe un registro de miembro para el usuario
        // en este tablero.
        //
        // La existencia de un miembro puede indicar que el usuario fue
        // retirado previamente del tablero. En ese caso, su registro puede
        // necesitar reactivación en lugar de una creación desde cero.
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, recipientUserId);

        // Capturar el estado de actividad anterior del miembro.
        // null indica que no existe un registro previo.
        Boolean memberActive = optionalMember
                .map(m -> m.isActive())
                .orElse(null);

        // Capturar el rol anterior del miembro para disponer de la
        // información necesaria durante una eventual compensación.
        // null indica que no existe un registro previo.
        Role role = optionalMember
                .map(m -> m.getRole())
                .orElse(null);

        // 5. Construir la operación antes de aplicar modificaciones sobre
        // Invitation y Member.
        //
        // Se registran los valores previos y el nuevo rol previsto para
        // disponer de información suficiente para gestionar la ejecución
        // y, si fuera necesario, la compensación de los cambios locales.

        // Generar el identificador que permitirá relacionar los pasos
        // posteriores de la operación.
        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACCEPT_INVITATION);
        operation.setStatus(OperationStatus.REGISTERED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        // Guardar las referencias del tablero, el usuario y la invitación
        // involucrados en la operación.
        operation.setBoardId(boardId);
        operation.setUserId(recipientUserId);
        operation.setInvitationId(invitationId);

        // Guardar el estado previo del miembro y los roles anterior y
        // previsto. Estos datos permiten conocer la situación inicial
        // y respaldar la lógica de compensación.
        operation.setMemberActive(memberActive);
        operation.setOldRole(role);
        operation.setNewRole(invitationRole);

        // Registrar la fecha de creación y dejar sin programar el siguiente
        // intento automático hasta que corresponda según el flujo.
        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        // 6. Persistir la operación en estado REGISTERED y devolverla
        // para que el flujo de aplicación de negocio pueda continuar.
        operationProjectService.saveOperation(operation);
        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerChangeRoleMember(Role role, UUID memberId, UUID ownerUserId) {

        // Validar logica de negocio
        Member findedMember = memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);

        // Buscar operación existente
        Optional<Operation> existingOperation = operationProjectService.findLatestOperationByMemberIdAndType(memberId,
                OperationType.CHANGE_ROLE_MEMBER);

        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            switch (existing.getStatus()) {
                case REGISTERED, LOCAL_COMPENSATION_COMPLETED, COMPENSATION_COMPLETED -> {
                    // Cambia el rol
                    existing.setNewRole(role);
                    return existing;
                }

                case EXECUTION_PENDING,
                        EXECUTING,
                        COMPENSATION_PENDING,
                        LOCAL_COMPENSATION_IN_PROGRESS,
                        MANUAL_INTERVENTION_REQUIRED ->
                    throw new OperationNotPendingException();

                case COMPLETED -> {
                }
            }
        }

        Board findedBoard = findedMember.getBoard();
        UUID boardId = findedBoard.getId();
        UUID userId = findedMember.getUserId();

        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.CHANGE_ROLE_MEMBER);
        operation.setStatus(OperationStatus.REGISTERED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        // Solamente se rellenan los campos necesarios
        operation.setBoardId(boardId);

        // No confundir los usuarios:
        // ID del miembro
        operation.setMemberId(memberId);
        // ID del usuario miembro (toma el ID real del usuario)
        operation.setUserId(userId);
        // ID del usuario administrador del espacio de trabajo, que a su vez es el
        // administrador del tablero
        operation.setOwnerUserId(ownerUserId);

        // Se establece el antiguo rol
        operation.setOldRole(findedMember.getRole());
        // Y el nuevo rol aqui
        operation.setNewRole(role);

        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        operationProjectService.saveOperation(operation);
        return operation;
    }

    // TODO: SI DE FORMA AUTOMATICA SE DESACTIVA EL MIEMBRO, ENTONCES SE DEBEN
    // ELIMINAR LAS OPERACIONES PENDIENTES RELACIONADAS AL MIEMBRO POR ID Y
    // OWNERUSERID
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerDeactivateMember(UUID memberId, UUID ownerUserId) {
        Member findedMember = memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);

        Optional<Operation> existingOperation = operationProjectService.findLatestOperationByMemberIdAndType(memberId,
                OperationType.DEACTIVATE_MEMBER);

        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            switch (existing.getStatus()) {
                case REGISTERED, LOCAL_COMPENSATION_COMPLETED, COMPENSATION_COMPLETED -> {
                    return existing;
                }

                case EXECUTION_PENDING,
                        EXECUTING,
                        COMPENSATION_PENDING,
                        LOCAL_COMPENSATION_IN_PROGRESS,
                        MANUAL_INTERVENTION_REQUIRED ->
                    throw new OperationNotPendingException();

                case COMPLETED -> {
                }
            }
        }

        Board findedBoard = findedMember.getBoard();
        UUID boardId = findedBoard.getId();
        UUID userId = findedMember.getUserId();

        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.DEACTIVATE_MEMBER);
        operation.setStatus(OperationStatus.REGISTERED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        operation.setBoardId(boardId);
        operation.setMemberId(memberId);
        operation.setUserId(userId);
        operation.setOwnerUserId(ownerUserId);
        operation.setMemberActive(true);

        operation.setOldRole(findedMember.getRole());

        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        operationProjectService.saveOperation(operation);
        return operation;
    }
}
