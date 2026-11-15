package com.trello.project.operation.service.handler;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Board;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.BoardStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.Role;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.utils.OperationHandlerUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

@Service
public class OperationDeactivateMemberServiceImpl implements OperationDeactivateMemberService {
    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;
    private final OperationHandlerUtils operationHandlerUtils;
    private final BoardProjectService boardProjectService;

    public OperationDeactivateMemberServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService,
            OperationHandlerUtils operationHandlerUtils,
            BoardProjectService boardProjectService) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
        this.operationHandlerUtils = operationHandlerUtils;
        this.boardProjectService = boardProjectService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeactivateMember(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, MemberNotFoundException,
            MemberInactiveException {

        Operation operation = operationHandlerUtils
                .prepareExecution(operationId, automatic);

        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        UUID memberId = operation.getMemberId();
        UUID ownerUserId = operation.getOwnerUserId();

        // Bloqueo pesimista
        Board board = boardProjectService.findBoardByIdWithLock(boardId);

        if (board.getStatus() != BoardStatus.ACTIVE) {
            throw new BoardNotFoundException();
        }

        // Buscar miembro
        Member findedMember = memberProjectService.findMemberActiveByIdAndOwnerUserId(memberId, ownerUserId);

        // Cambiar estado activo del miembro
        findedMember.setActive(false);

        // Borrar invitación si existe
        invitationProjectService.deleteInvitationIfExistsByBoardIdAndRecipientUserId(
                boardId,
                userId,
                ownerUserId);

        memberProjectService.saveMember(findedMember);

        operationProjectService.saveOperation(operation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeactivateMember(UUID operationId)
            throws OperationNotFoundException, MemberNotFoundException {
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        operationProjectService.saveOperation(operation);

        UUID memberId = operation.getMemberId();
        UUID ownerUserId = operation.getOwnerUserId();
        Role oldRole = operation.getOldRole();

        Member findedMember = memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);
        findedMember.setRole(oldRole);
        findedMember.setActive(true);
        memberProjectService.saveMember(findedMember);
    }

}
