package com.trello.project.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Invitation;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    // No se toma en cuenta el usuario emisor porque un tablero tiene un solo
    // usuario encargado de enviar invitaciones que es el mismo que ha creado el
    // board
    boolean existsByBoardIdAndRecipientUserId(UUID boardId, UUID invitedUserId);

    List<Invitation> findByRecipientUserId(UUID recipientUserId);

    List<Invitation> findByBoardId(UUID boardId);

}
