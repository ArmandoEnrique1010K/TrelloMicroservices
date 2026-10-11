package com.trello.project.operation.service;

import com.trello.project.invitation.exception.InvitationConfirmedException;

import java.util.UUID;

public interface OperationService {
    UUID registerInvitationAcceptance(UUID invitationId, UUID recipientUserId);

    void applyInvitationAcceptance(UUID operationId) throws InvitationConfirmedException;

    void revertInvitationAcceptance(UUID operationId);
}
