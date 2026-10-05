package com.trello.project.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.client.IdentityQueryClient;
import com.trello.project.client.dto.response.UserResponse;

// Servicios que requieren de un llamado al microservicio Identity
@Service
public class IdentityClientServiceImpl implements IdentityClientService {

    private final IdentityQueryClient identityQueryClient;

    public IdentityClientServiceImpl(IdentityQueryClient identityQueryClient) {
        this.identityQueryClient = identityQueryClient;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponse> findUsersByIds(Set<UUID> usersIds) {

        // Si ocurre un error debe devolver una excepción
        // try {
        // List<UserResponse> users = identityQueryClient.findUsersByIds(
        // new ArrayList<>(usersIds));

        // return users;
        // } catch (RetryableException | FeignException.ServiceUnavailable e) {
        // throw new ServiceUnavailableException();
        // }

        // Se ha definido una excepción en IdentityQueryClientFallbackFactory
        return identityQueryClient.findUsersByIds(new ArrayList<>(usersIds));
    }

    @Override
    public List<UserResponse> listAllUsersByEmailAndExcludingIds(String email,
            List<UUID> excludedUsersIds) {
        return identityQueryClient.listAllUsersByEmailAndExcludingIds(email, excludedUsersIds);
    }
}
