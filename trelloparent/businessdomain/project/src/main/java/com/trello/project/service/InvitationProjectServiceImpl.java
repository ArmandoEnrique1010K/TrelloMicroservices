package com.trello.project.service;

import com.trello.project.repositories.BoardRepository;
import com.trello.project.repositories.InvitationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Invitation;
import com.trello.project.enums.Status;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.invitation.exception.InvitationNotFoundException;

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
        boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        return invitationRepository.existsByBoardIdAndRecipientUserId(boardId, recipientUserId);
    }

    @Override
    public List<Invitation> findAllUnconfirmedInvitationsByRecipientUserId(UUID recipientUserId) {
        return invitationRepository.findByRecipientUserIdAndStatus(recipientUserId, Status.UNCONFIRMED);
    }

    @Override
    public List<Invitation> findAllInvitationsByBoardId(UUID boardId) {
        boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        return invitationRepository.findByBoardId(boardId);
    }

    @Override
    public Invitation findInvitationByIdAndSenderUserId(UUID invitationId, UUID senderUserId)
            throws InvitationNotFoundException {
        Invitation invitation = invitationRepository.findByIdAndSenderUserId(invitationId, senderUserId)
                .orElseThrow(InvitationNotFoundException::new);
        return invitation;
    }

    @Override
    public void deleteInvitationByIdAndSenderUserId(UUID invitationId, UUID senderUserId)
            throws InvitationNotFoundException {
        Invitation invitation = invitationRepository.findByIdAndSenderUserId(invitationId, senderUserId)
                .orElseThrow(InvitationNotFoundException::new);

        invitationRepository.delete(invitation);
    }

    @Override
    public Invitation findInvitationByIdAndRecipientUserId(UUID invitationId, UUID recipientUserId)
            throws InvitationNotFoundException {
        Invitation invitation = invitationRepository.findByIdAndRecipientUserId(invitationId,
                recipientUserId)
                .orElseThrow(InvitationNotFoundException::new);
        return invitation;

    }

    // Un delete que no encuentra coincidencias simplemente afecta 0 registros
    // Las operaciones que implican un borrado en la base de datos debe llevar un
    // transactional
    @Transactional
    @Override
    public void deleteInvitationIfExistsByBoardIdAndRecipientUserId(UUID boardId, UUID recipientUserId,
            UUID senderUserId) {
        invitationRepository.deleteByBoardIdAndRecipientUserIdAndSenderUserId(boardId, recipientUserId, senderUserId);
    }

    // Como este método se va a ejecutar automaticamente cada cierto tiempo debe
    // tener un @Transactional
    @Override
    @Transactional
    public void deleteAllAcceptedInvitations() {
        LocalDateTime now = LocalDateTime.now();
        invitationRepository.deleteAcceptedInvitations(now);
    }
}
