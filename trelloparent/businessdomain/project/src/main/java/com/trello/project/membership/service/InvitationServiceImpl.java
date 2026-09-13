package com.trello.project.membership.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.enums.Status;
import com.trello.project.membership.dto.request.InvitationRequest;
import com.trello.project.membership.dto.response.InvitationResponse;
import com.trello.project.membership.exception.InvitationAlreadyExistsException;
import com.trello.project.membership.mapper.InvitationRequestMapper;
import com.trello.project.membership.mapper.InvitationResponseMapper;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.WorkspaceProjectService;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final WorkspaceProjectService workspaceProjectService;
    private final BoardProjectService boardProjectService;
    private final InvitationRequestMapper invitationRequestMapper;
    private final InvitationProjectService invitationProjectService;
    private final InvitationResponseMapper invitationResponseMapper;

    public InvitationServiceImpl(WorkspaceProjectService workspaceProjectService,
            BoardProjectService boardProjectService,
            InvitationRequestMapper invitationRequestMapper, InvitationProjectService invitationProjectService,
            InvitationResponseMapper invitationResponseMapper) {
        this.workspaceProjectService = workspaceProjectService;
        this.boardProjectService = boardProjectService;
        this.invitationRequestMapper = invitationRequestMapper;
        this.invitationProjectService = invitationProjectService;
        this.invitationResponseMapper = invitationResponseMapper;
    }

    @Override
    public InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException {

        Board board = boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        System.out.println(board.getId());

        if (invitationProjectService.existsInvitationByBoardIdAndRecipientUserId(boardId, recipientUserId)) {
            throw new InvitationAlreadyExistsException();
        }

        Invitation invitationToInvitationRequest = invitationRequestMapper
                .invitationRequestToInvitation(invitationRequest);

        // Emisor
        invitationToInvitationRequest.setSenderUserId(ownerUserId);
        // Receptor
        invitationToInvitationRequest.setRecipientUserId(recipientUserId);

        invitationToInvitationRequest.setBoard(board);
        invitationToInvitationRequest.setStatus(Status.UNCONFIRMED);

        Invitation savedInvitation = invitationProjectService.saveInvitation(invitationToInvitationRequest);
        InvitationResponse invitationResponse = invitationResponseMapper
                .invitationToInvitationResponse(savedInvitation);
        return invitationResponse;
    }

}
