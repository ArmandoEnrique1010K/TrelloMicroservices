package com.trello.project.operation.service.handler;

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
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

@Service
public class OperationAcceptInvitationServiceImpl implements OperationAcceptInvitationService {

    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;

    public OperationAcceptInvitationServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyAcceptInvitation(UUID operationId, boolean automatic) {
        // 1. Recuperar la operación persistida.
        Operation persistedOperation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // 2. Validar si puede ejecutarse la aceptación.
        OperationStatus currentStatus = persistedOperation.getStatus();

        if (currentStatus != OperationStatus.CREATED
                && currentStatus != OperationStatus.COMPENSATION_COMPLETED) {

            throw new OperationNotPendingException();
        }

        // 3. Incrementar el contador correspondiente.
        if (automatic) {
            persistedOperation.setAutomaticAttempts(
                    persistedOperation.getAutomaticAttempts() + 1);
        } else {
            persistedOperation.setManualAttempts(
                    persistedOperation.getManualAttempts() + 1);
        }

        // 4. Preparar la nueva ejecución.
        persistedOperation.setStatus(OperationStatus.PENDING);
        persistedOperation.setCompletedAt(null);
        persistedOperation.setNextAutomaticAttemptAt(
                LocalDateTime.now().plusSeconds(30));

        // 5. Tomar los datos de la operación
        UUID boardId = persistedOperation.getBoardId();
        UUID recipientUserId = persistedOperation.getUserId();
        UUID invitationId = persistedOperation.getInvitationId();

        // Boolean memberActive = operation.getMemberActive();
        // Role role = operation.getRole();

        Invitation invitation = invitationProjectService.findInvitationByIdAndRecipientUserId(
                invitationId,
                recipientUserId);

        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // 6. Actualizar la invitación
        invitation.setStatus(Status.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationProjectService.saveInvitation(invitation);

        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, recipientUserId);

        Board board = invitation.getBoard();

        // 7. Operacion relacionada a miembro
        // Si el miembro esta inactivo, debe activarlo
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setActive(true);
            member.setRole(invitation.getRole());
            memberProjectService.saveMember(member);
        } else {
            // Si no existe el miembro, agregar el nuevo miembro
            Member member = new Member();
            member.setUserId(recipientUserId);
            member.setRole(invitation.getRole());
            member.setBoard(board);
            member.setActive(true);
            memberProjectService.saveMember(member);
        }

        // Guardar la operación
        operationProjectService.saveOperation(persistedOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertAcceptInvitation(UUID operationId) {
        // 1. Validar que la operación exista y cambiar de estado
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        operation.setStatus(OperationStatus.COMPENSATION_PROCESSING);
        operationProjectService.saveOperation(operation);

        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        UUID invitationId = operation.getInvitationId();
        Boolean activeMember = operation.getMemberActive();
        Role role = operation.getRole();

        // 2. Buscar la invitación y modificar los campos
        Invitation invitation = invitationProjectService.findInvitationByIdAndRecipientUserId(invitationId, userId);
        invitation.setStatus(Status.UNCONFIRMED);
        invitation.setAcceptedAt(null);
        invitationProjectService.saveInvitation(invitation);

        // 3. Buscar el miembro (podria no existir)
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, userId);

        // Si el estado del miembro es null quiere decir que se trataba de un nuevo
        // miembro
        if (activeMember == null) {

            // Si el miembro existe, eliminarlo permanentemente
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();

                memberProjectService.deletePermanentlyMember(
                        member.getId(),
                        userId);
            } else {
                // En el caso de que el miembro no exista, pero indica que se trato de una
                // operación en la que se trata de eliminar un miembro existente

                // Lanzar excepción de miembro no encontrado

            }
        } else if (Boolean.FALSE.equals(activeMember)) {
            // En el caso que el campo activeMember sea false, indica que se trata de un
            // miembro inactivo

            // Si el miembro no existe, establecer estado de MANUAL_REVIEW
            if (optionalMember.isEmpty()) {
                operation.setStatus(OperationStatus.MANUAL_REVIEW);
                operationProjectService.saveOperation(operation);

                // Se colocar un return para terminar la ejecución del código y no se lanza una
                // excepción porque ahí si no haria los cambios en la base de datos por
                // @Transactional
                return;
            }

            // Revertir los cambios realizados en miembro
            Member member = optionalMember.get();
            member.setActive(false);
            member.setRole(role);
            memberProjectService.saveMember(member);
        } else {
            // En el caso de que activeMember sea true, se tendria una inconsistencia,
            // porque indica que se trata de un miembro activo
            operation.setStatus(OperationStatus.MANUAL_REVIEW);
            operationProjectService.saveOperation(operation);
            return;
        }

        // 4. Dejar operación con el estado de COMPENSATED
        operation.setStatus(OperationStatus.COMPENSATED);
        operation.setCompletedAt(null);
        operationProjectService.saveOperation(operation);
    }

}
