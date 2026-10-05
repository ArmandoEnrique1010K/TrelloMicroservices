package com.trello.project.client.fallback;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.trello.project.client.IdentityQueryClient;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IdentityQueryClientFallbackFactory
        implements FallbackFactory<IdentityQueryClient> {

    // Cualquier excepción que ocurra cuando se haga un llamado al endpoint, caera
    // en un ServiceUnavailableException
    @Override
    public IdentityQueryClient create(Throwable cause) {
        log.error(
                "Fallback ejecutado para IdentityQueryClient. Causa: {}",
                cause.toString(),
                cause);

        return new IdentityQueryClient() {

            @Override
            public List<UserResponse> findUsersByIds(List<UUID> usersIds) {
                log.error("Error relacionado con findUsersByIds");
                throw new ServiceUnavailableException();
            }

            @Override
            public List<UserResponse> listAllUsersByEmailAndExcludingIds(
                    String email,
                    List<UUID> ids) {
                log.error("Error relacionado con listAllUsersByEmailAndExcludingIds");
                throw new ServiceUnavailableException();
            }
        };
    }
}