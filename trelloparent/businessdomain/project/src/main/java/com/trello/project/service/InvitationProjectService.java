package com.trello.project.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.entities.Invitation;
import com.trello.project.exception.BoardNotFoundException;

public interface InvitationProjectService {
    Invitation saveInvitation(Invitation invitation);

    boolean existsInvitationByBoardIdAndRecipientUserId(UUID boardId, UUID recipientUserId)
            throws BoardNotFoundException;

    List<Invitation> findAllInvitationsByRecipientUserId(UUID recipientUserId);

    List<Invitation> findAllInvitationsByBoardId(UUID boardId);

}
