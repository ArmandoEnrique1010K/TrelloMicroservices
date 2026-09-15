package com.trello.project.client.services;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.trello.project.client.dto.response.UserResponse;

public interface IdentityClientService {
    List<UserResponse> findUsersByIds(Set<UUID> usersIds);

    UserResponse findUserById(UUID userId);
}
