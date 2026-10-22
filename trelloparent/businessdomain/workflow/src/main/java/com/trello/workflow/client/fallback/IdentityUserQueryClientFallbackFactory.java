package com.trello.workflow.client.fallback;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.trello.workflow.client.IdentityUserQueryClient;
import com.trello.workflow.client.dto.response.UserResponse;
import com.trello.workflow.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IdentityUserQueryClientFallbackFactory implements FallbackFactory<IdentityUserQueryClient> {

    @Override
    public IdentityUserQueryClient create(Throwable cause) {
        log.error(
                "Fallback ejecutado para IdentityUserQueryClient. Causa: {}",
                cause.toString(),
                cause);

        return new IdentityUserQueryClient() {

            @Override
            public List<UserResponse> findUsersByIds(List<UUID> usersIds) {
                log.error("Error relacionado con findUsersByIds");
                throw new ServiceUnavailableException();
            }

            @Override
            public UserResponse findUserById(UUID userId) {
                log.error("Error relacionado con listAllUsersByEmailAndExcludingIds");
                throw new ServiceUnavailableException();
            }
        };
    }
}
