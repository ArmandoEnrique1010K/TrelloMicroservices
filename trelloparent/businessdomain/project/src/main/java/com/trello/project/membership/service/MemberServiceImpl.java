package com.trello.project.membership.service;

import com.trello.project.membership.mapper.MemberResponseMapper;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Member;
import com.trello.project.enums.Role;
import com.trello.project.membership.dto.response.MemberResponse;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.MemberProjectService;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberResponseMapper memberResponseMapper;
    private final BoardProjectService boardProjectService;
    private final MemberProjectService memberProjectService;

    public MemberServiceImpl(BoardProjectService boardProjectService, MemberProjectService memberProjectService,
            MemberResponseMapper memberResponseMapper) {
        this.boardProjectService = boardProjectService;
        this.memberProjectService = memberProjectService;
        this.memberResponseMapper = memberResponseMapper;
    }

    @Override
    public List<MemberResponse> listAllMembersByBoardId(UUID boardId, UUID userId) {
        boardProjectService.findBoardAccessibleByUser(boardId, userId);

        List<Member> listMembersByBoardId = memberProjectService.findAllMembersByBoardId(boardId);

        return memberResponseMapper.memberListToMemberResponseList(listMembersByBoardId);

    }

    @Override
    public MemberResponse changeRoleMemberById(Role role, UUID memberId, UUID ownerUserId) {

        // Buscar miembro
        Member findedMember = memberProjectService.findMemberByBoardIdAndOwnerUserIdAndRole(memberId, ownerUserId,
                role);

        findedMember.setRole(role);

        Member saveMember = memberProjectService.saveMember(findedMember);
        MemberResponse memberResponse = memberResponseMapper.memberToMemberResponse(saveMember);
        return memberResponse;
    }

    @Override
    public void deleteMember(UUID memberId, UUID ownerUserId) {
        memberProjectService.deleteMemberById(memberId, ownerUserId);
    }

}
