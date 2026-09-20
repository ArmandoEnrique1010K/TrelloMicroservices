package com.trello.project.member.mapper;

import org.mapstruct.Mapper;

import com.trello.project.entities.Member;
import com.trello.project.member.dto.response.MemberResponse;

@Mapper(componentModel = "spring")
public interface MemberResponseMapper {
    MemberResponse memberToMemberResponse(Member source);

    // List<MemberResponse> memberListToMemberResponseList(List<Member> source);
}
