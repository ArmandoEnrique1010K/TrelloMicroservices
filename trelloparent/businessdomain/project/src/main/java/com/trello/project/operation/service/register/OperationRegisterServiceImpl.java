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

    // Recordar que si cae en una excepción, revertira los cambios realizados en la
    // base de datos
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAcceptInvitation(UUID invitationId, UUID recipientUserId) {

        // 1. Idempotencia
        // Buscar una operación por tipo e ID de invitación
        Optional<Operation> existingOperation = operationProjectService.findOperationByInvitationIdAndType(
                invitationId,
                OperationType.ACCEPT_INVITATION);

        // 2. No registrar nuevamente la operación si existe
        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            switch (existing.getStatus()) {
                case CREATED, COMPENSATION_COMPLETED -> {
                    // Terminar aqui la ejecución del metodo y devolver la entidad
                    return existing;
                }

                // Lanzar una excepción si la operación no esta en un estado pendiente
                case PENDING, PROCESSING, COMPENSATION_PENDING, COMPENSATION_PROCESSING,
                        COMPENSATED, COMPLETED, MANUAL_REVIEW ->
                    throw new OperationNotPendingException();
            }
        }

        // 3. Buscar y validar la invitación
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        invitationId,
                        recipientUserId);

        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // Busca el tablero correspondiente a la invitación
        Board findedBoard = invitation.getBoard();
        UUID boardId = findedBoard.getId();

        // 4. Buscar si existe un miembro
        // Si el miembro existe quiere decir que se trata de un miembro que ha sido
        // eliminado del equipo de un tablero
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, recipientUserId);

        // Puede ser un null, false o true
        Boolean memberActive = optionalMember
                .map(m -> m.isActive())
                .orElse(null);

        // Puede ser un null o un Role (que es el rol antiguo)
        Role role = optionalMember
                .map(m -> m.getRole())
                .orElse(null);

        // 5. Registrar la operación antes de alterar los datos relacionados a
        // Invitation y Member

        // Genera un ID unico para la operación
        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACCEPT_INVITATION);
        operation.setStatus(OperationStatus.CREATED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        // Solamente se rellenan los campos necesarios
        operation.setBoardId(boardId);
        operation.setUserId(recipientUserId);
        operation.setInvitationId(invitationId);

        operation.setMemberActive(memberActive);
        operation.setRole(role);

        // Fecha actual en la fecha de creación
        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        // 6. Guardar operación y devolver la entidad
        operationProjectService.saveOperation(operation);
        return operation;
    }
}
