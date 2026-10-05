package com.trello.project.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.client.IdentityQueryClient;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.exception.ServiceUnavailableException;

import feign.FeignException;
import feign.RetryableException;

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
        try {
            List<UserResponse> users = identityQueryClient.findUsersByIds(
                    new ArrayList<>(usersIds));

            return users;
        } catch (RetryableException | FeignException.ServiceUnavailable e) {
            throw new ServiceUnavailableException();
        }
    }

    @Override
    public List<UserResponse> listAllUsersByEmailAndExcludingIds(String email,
            List<UUID> excludedUsersIds) {
        return identityQueryClient.listAllUsersByEmailAndExcludingIds(email, excludedUsersIds);
    }
}
