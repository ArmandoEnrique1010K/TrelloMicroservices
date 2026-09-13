package com.trello.project.membership.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.membership.dto.request.InvitationRequest;
import com.trello.project.membership.dto.response.InvitationResponse;
import com.trello.project.membership.exception.InvitationAlreadyExistsException;

public interface InvitationService {

    InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException;

    // Listar invitaciones por ID de usuario receptor (invitaciones recibidas por
    // usuario autenticado)
    List<InvitationResponse> listAllInvitationsByRecipientUserId(UUID recipientUserId);

    // Listar invitaciones por ID de tablero (solamente para administrador de
    // tablero)
    List<InvitationResponse> listAllInvitationsByBoardId(UUID boardId, UUID ownerUserId);

    // Editar una invitación existente

    // Eliminar invitación

    // Aceptar invitación

    // Rechazar invitación

}
