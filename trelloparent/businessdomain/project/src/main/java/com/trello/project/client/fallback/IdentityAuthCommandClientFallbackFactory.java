package com.trello.project.client.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.trello.project.client.IdentityAuthCommandClient;
import com.trello.project.client.dto.response.ServiceTokenResponse;
import com.trello.project.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IdentityAuthCommandClientFallbackFactory implements FallbackFactory<IdentityAuthCommandClient> {

    @Override
    public IdentityAuthCommandClient create(Throwable cause) {
        log.error(
                "Fallback ejecutado para IdentityAuthCommandClientFallbackFactory. Causa: {}",
                cause.toString(),
                cause);

        return new IdentityAuthCommandClient() {

            @Override
            public ServiceTokenResponse generateInternalToken() {
                log.error("Error relacionado con generateInternalToken");

                throw new ServiceUnavailableException();
            }
        };
    }

}
