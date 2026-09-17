package com.trello.project.service;

import com.trello.project.repositories.MemberRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Member;
import com.trello.project.enums.Role;
import com.trello.project.membership.exception.MemberNotFoundException;

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
    public List<Member> findAllMembersByBoardId(UUID boardId) {
        return memberRepository.findByBoardId(boardId);
    }

    @Override
    public Member findMemberByBoardIdAndOwnerUserIdAndRole(UUID boardId, UUID ownerUserId, Role role)
            throws MemberNotFoundException {
        return memberRepository.findByBoardIdAndBoardWorkspaceOwnerUserIdAndRole(boardId,
                ownerUserId, role).orElseThrow(
                        MemberNotFoundException::new);
    }

    @Override
    public void deleteMemberById(UUID memberId, UUID ownerUserId) throws MemberNotFoundException {
        Member member = memberRepository.findByIdAndBoardWorkspaceOwnerUserId(memberId, ownerUserId)
                .orElseThrow(MemberNotFoundException::new);
        memberRepository.delete(member);
    }
}
