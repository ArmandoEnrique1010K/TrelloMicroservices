package com.trello.identity.user.service;

import java.util.List;
import java.util.UUID;

import com.trello.identity.user.dto.response.UserResponse;

public interface UserService {

    List<UserResponse> listAllUsersByEmailAndExcludingIds(String email, List<UUID> ids);

    UserResponse findUserById(UUID id);

    List<UserResponse> listAllUsersByIds(List<UUID> ids);
}
