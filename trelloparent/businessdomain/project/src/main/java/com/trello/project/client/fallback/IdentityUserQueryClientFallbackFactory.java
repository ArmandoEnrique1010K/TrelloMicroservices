package com.trello.project.client.fallback;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.trello.project.client.IdentityUserQueryClient;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IdentityUserQueryClientFallbackFactory
        implements FallbackFactory<IdentityUserQueryClient> {

    // FallbackFactory recibe la excepción que provocó el fallo
    // de la llamada realizada mediante OpenFeign.
    //
    // Esto permite conocer la causa original del fallo, por ejemplo:
    //
    // feign.RetryableException
    // -> Connection refused
    //
    // o cualquier otra excepción producida durante la comunicación
    // con el microservicio Identity.
    @Override
    public IdentityUserQueryClient create(Throwable cause) {
        log.error(
                "Fallback ejecutado para IdentityUserQueryClient. Causa: {}",
                cause.toString(),
                cause);

        // Se devuelve una implementación alternativa del cliente.
        //
        // En lugar de devolver datos falsos o vacíos, cada operación
        // transforma el fallo de comunicación en una excepción de
        // dominio de la aplicación:
        //
        // ServiceUnavailableException
        //
        // Esta excepción es importante porque Resilience4j Retry está
        // configurado para reconocerla como una excepción reintentable.
        return new IdentityUserQueryClient() {

            @Override
            public List<UserResponse> findUsersByIds(List<UUID> usersIds) {
                log.error("Error relacionado con findUsersByIds");

                // El Fallback transforma el error original en una
                // excepción que puede ser procesada por la capa
                // superior.
                //
                // En IdentityClientServiceImpl, Resilience4j Retry
                // reconoce esta excepción y realiza otro intento
                // mientras no se alcance max-attempts.
                throw new ServiceUnavailableException();
            }

            @Override
            public List<UserResponse> listAllUsersByEmailAndExcludingIds(
                    String email,
                    List<UUID> ids) {
                log.error("Error relacionado con listAllUsersByEmailAndExcludingIds");

                // Mismo comportamiento para esta operación.
                throw new ServiceUnavailableException();
            }
        };
    }
}