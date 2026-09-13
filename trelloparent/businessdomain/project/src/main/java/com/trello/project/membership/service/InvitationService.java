package com.trello.project.membership.service;

import java.util.UUID;

import com.trello.project.membership.dto.request.InvitationRequest;
import com.trello.project.membership.dto.response.InvitationResponse;
import com.trello.project.membership.exception.InvitationAlreadyExistsException;

public interface InvitationService {

    InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException;

}
