package com.trello.project.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.IdentityClient;
import com.trello.project.client.dto.response.UserResponse;

// Servicios que requieren de un llamado al microservicio Identity
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
    public List<UserResponse> listAllUsersByEmailAndExcludingIds(String email,
            List<UUID> excludedUsersIds) {
        return identityClient.listAllUsersByEmailAndExcludingIds(email, excludedUsersIds);
    }
}
