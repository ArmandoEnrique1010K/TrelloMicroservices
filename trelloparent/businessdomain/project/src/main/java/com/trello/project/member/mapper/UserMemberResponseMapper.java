package com.trello.project.member.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Member;
import com.trello.project.member.dto.response.UserMemberResponse;

@Mapper(componentModel = "spring")
public interface UserMemberResponseMapper {

    // Aunque no se va a utilizar este método, se requiere definirlo para
    // especificar los campos que se van a ignorar, ya que el segundo metodo que
    // sirve para mapear una lista de Member, herede los campos que se van a ignorar
    @Mappings({
            @Mapping(target = "memberUser", ignore = true)
    })
    UserMemberResponse memberToUserMemberResponse(Member source);

    List<UserMemberResponse> memberListToUserMemberResponseList(List<Member> source);
}
