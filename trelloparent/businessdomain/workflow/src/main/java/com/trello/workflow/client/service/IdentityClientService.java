package com.trello.workflow.client.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.trello.workflow.client.dto.response.UserResponse;

public interface IdentityClientService {
    List<UserResponse> findUsersByIds(Set<UUID> usersIds);
}
