package com.trello.project.service;

import com.trello.project.repositories.BoardRepository;
import com.trello.project.repositories.InvitationRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Invitation;
import com.trello.project.exception.BoardNotFoundException;

@Service
public class InvitationProjectServiceImpl implements InvitationProjectService {

    private final BoardRepository boardRepository;
    private final InvitationRepository invitationRepository;

    InvitationProjectServiceImpl(InvitationRepository invitationRepository, BoardRepository boardRepository) {
        this.invitationRepository = invitationRepository;
        this.boardRepository = boardRepository;
    }

    @Override
    public Invitation saveInvitation(Invitation invitation) {
        return invitationRepository.save(invitation);
    }

    @Override
    public boolean existsInvitationByBoardIdAndRecipientUserId(UUID boardId, UUID recipientUserId)
            throws BoardNotFoundException {
        // TODO: LLAMAR A UN SERVICIO PARA VERIFICAR SI EXISTE EL USUARIO EN LA BASE DE
        // DATOS
        boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        return invitationRepository.existsByBoardIdAndRecipientUserId(boardId, recipientUserId);
    }

    @Override
    public List<Invitation> findAllInvitationsByRecipientUserId(UUID recipientUserId) {
        return invitationRepository.findByRecipientUserId(recipientUserId);
    }

    @Override
    public List<Invitation> findAllInvitationsByBoardId(UUID boardId) {
        boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        return invitationRepository.findByBoardId(boardId);
    }
}
