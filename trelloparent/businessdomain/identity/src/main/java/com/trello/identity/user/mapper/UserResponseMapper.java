package com.trello.identity.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.identity.entities.User;
import com.trello.identity.user.dto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {

    UserResponse userToUserResponse(User source);

    List<UserResponse> userListToUserResponseList(List<User> source);
}
