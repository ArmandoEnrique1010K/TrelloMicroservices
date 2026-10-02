package com.trello.workflow.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.client.IdentityClient;
import com.trello.workflow.client.dto.response.UserResponse;

@Service
public class IdentityClientServiceImpl implements IdentityClientService {

    private final IdentityClient identityClient;

    public IdentityClientServiceImpl(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    @Override
    public List<UserResponse> findUsersByIds(Set<UUID> usersIds) {
        return identityClient.findUsersByIds(new ArrayList<>(usersIds));
    }

    @Override
    public UserResponse findUserById(UUID userId) {
        return identityClient.findUserById(userId);
    }
}
