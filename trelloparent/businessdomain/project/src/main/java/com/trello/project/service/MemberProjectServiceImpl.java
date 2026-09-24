package com.trello.project.service;

import com.trello.project.repositories.MemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Member;
import com.trello.project.member.exception.MemberInactiveException;
import com.trello.project.member.exception.MemberNotFoundException;

@Service
public class MemberProjectServiceImpl implements MemberProjectService {

    private final MemberRepository memberRepository;

    MemberProjectServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    // No se devuelve una excepción throws BoardNotFoundException, porque este
    // metodo se utiliza en MemberServiceImpl y antes de llamarlo se lanza una
    // excepción en un metodo para validar que el tablero (Board) exista
    @Override
    public List<Member> findAllMembersByBoardIdAndIsOwnerUser(UUID boardId, boolean isOwnerUser) {
        // Si es el usuario administrador del espacio de trabajo
        if (isOwnerUser) {
            return memberRepository.findByBoardId(boardId);
        }

        // De lo contrario solamente listara los activos
        // Obviamente si el miembro tiene el rol de MEMBER o ADMIN
        return memberRepository.findByActiveTrueAndBoardId(boardId);
    }

    @Override
    public Member findMemberByIdAndOwnerUserId(UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException {

        Member member = memberRepository.findByIdAndBoardWorkspaceOwnerUserId(memberId,
                ownerUserId).orElseThrow(
                        MemberNotFoundException::new);

        // Si el miembro esta inactivo
        if (!member.isActive()) {
            throw new MemberInactiveException();
        }

        return member;
    }

    // TODO: REVISAR ESTE MÉTODO SI DEVUELVE NULL
    @Override
    public Optional<Member> findOptionalMemberByBoardIdAndUserId(UUID memberId, UUID userId) {
        Optional<Member> member = memberRepository.findByBoardIdAndUserIdAndActiveFalse(memberId, userId);
        return member;
    }
}
