package com.trello.identity.user.service;

import java.util.List;
import java.util.UUID;

import com.trello.identity.user.dto.UserResponse;

public interface UserService {

    List<UserResponse> listAllUsersByEmailExcludingIds(String email, List<UUID> ids);
}
