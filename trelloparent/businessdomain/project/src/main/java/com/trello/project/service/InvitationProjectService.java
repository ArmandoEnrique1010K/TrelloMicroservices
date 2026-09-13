package com.trello.project.service;

import java.util.UUID;

import com.trello.project.entities.Invitation;

public interface InvitationProjectService {
    Invitation saveInvitation(Invitation invitation);

    boolean existsInvitationByBoardIdAndRecipientUserId(UUID boardId, UUID recipientUserId);
}
