package com.trello.workflow.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.client.IdentityUserQueryClient;
import com.trello.workflow.client.dto.response.UserResponse;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class IdentityClientServiceImpl implements IdentityClientService {

    private final IdentityUserQueryClient identityClient;

    public IdentityClientServiceImpl(IdentityUserQueryClient identityClient) {
        this.identityClient = identityClient;
    }

    @Retry(name = "businessdomain-identity-user-query")
    @Override
    public List<UserResponse> findUsersByIds(Set<UUID> usersIds) {
        return identityClient.findUsersByIds(new ArrayList<>(usersIds));
    }

    @Retry(name = "businessdomain-identity-user-query")
    @Override
    public UserResponse findUserById(UUID userId) {
        return identityClient.findUserById(userId);
    }
}
