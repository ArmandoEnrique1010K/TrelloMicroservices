package com.trello.project.membership.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Member;
import com.trello.project.membership.dto.response.MemberResponse;

@Mapper(componentModel = "spring")
public interface MemberResponseMapper {
    @Mappings({
            @Mapping(target = "memberUser", ignore = true)
    })
    MemberResponse memberToMemberResponse(Member source);

    List<MemberResponse> memberListToMemberResponseList(List<Member> source);
}
