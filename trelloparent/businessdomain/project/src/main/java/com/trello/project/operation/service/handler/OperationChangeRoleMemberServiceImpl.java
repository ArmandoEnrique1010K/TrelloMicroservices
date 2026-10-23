package com.trello.project.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Board;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.Role;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

@Service
public class OperationChangeRoleMemberServiceImpl implements OperationChangeRoleMemberService {
    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;

    public OperationChangeRoleMemberServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
    }

    @Override
    public void applyChangeRoleMember(UUID operationId, boolean automatic) {
        Operation persistedOperation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        OperationStatus currentStatus = persistedOperation.getStatus();

        if (currentStatus != OperationStatus.REGISTERED
                && currentStatus != OperationStatus.LOCAL_COMPENSATION_COMPLETED
                && currentStatus != OperationStatus.COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        if (automatic) {
            persistedOperation.setAutomaticAttempts(
                    persistedOperation.getAutomaticAttempts() + 1);
        } else {
            persistedOperation.setManualAttempts(
                    persistedOperation.getManualAttempts() + 1);
        }

        persistedOperation.setStatus(OperationStatus.EXECUTION_PENDING);
        persistedOperation.setCompletedAt(null);
        persistedOperation.setNextAutomaticAttemptAt(
                LocalDateTime.now().plusSeconds(30));

        UUID boardId = persistedOperation.getId();
        UUID userId = persistedOperation.getUserId();
        UUID memberId = persistedOperation.getMemberId();
        UUID ownerUserId = persistedOperation.getOwnerUserId();
        Role newRole = persistedOperation.getNewRole();

        // Buscar miembro
        Member findedMember = memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);

        // Actualiza el rol
        findedMember.setRole(newRole);

        Board board = findedMember.getBoard();

        // Borrar invitación si existe
        invitationProjectService.deleteInvitationIfExistsByBoardIdAndRecipientUserId(
                board.getId(),
                findedMember.getUserId(),
                ownerUserId);

        Member saveMember = memberProjectService.saveMember(findedMember);

        WorkflowRole workflowRole = WorkflowRoleUtils.parseRoleToWorkflowRole(newRole);

        // OBTENER EL ID DEL USUARIO DESDE EL MIEMBRO
        UUID memberUserId = saveMember.getUserId();

        // // Cambiar el rol en el otro microservicio
        // workflowClientService.changeRoleBoardAccess(board.getId(), memberUserId,
        // workflowRole);

        // MemberResponse memberResponse =
        // memberResponseMapper.memberToMemberResponse(saveMember);
        // return memberResponse;

        // Guardar la operación
        operationProjectService.saveOperation(persistedOperation);
    }

    @Override
    public void revertChangeRoleMember(UUID operationId) {
    }

}
