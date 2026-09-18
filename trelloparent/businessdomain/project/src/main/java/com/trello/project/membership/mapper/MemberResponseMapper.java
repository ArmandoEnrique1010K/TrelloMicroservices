package com.trello.project.membership.mapper;

import org.mapstruct.Mapper;

import com.trello.project.entities.Member;
import com.trello.project.membership.dto.response.MemberResponse;

@Mapper(componentModel = "spring")
public interface MemberResponseMapper {
    MemberResponse memberToMemberResponse(Member source);

    // List<MemberResponse> memberListToMemberResponseList(List<Member> source);
}
