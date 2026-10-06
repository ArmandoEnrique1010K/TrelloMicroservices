package com.trello.project.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.client.IdentityQueryClient;
import com.trello.project.client.dto.response.UserResponse;

import io.github.resilience4j.retry.annotation.Retry;

// Servicios que requieren de un llamado al microservicio Identity
@Service
public class IdentityClientServiceImpl implements IdentityClientService {

    private final IdentityQueryClient identityQueryClient;

    public IdentityClientServiceImpl(IdentityQueryClient identityQueryClient) {
        this.identityQueryClient = identityQueryClient;
    }

    // Retry se encarga de volver a ejecutar este método cuando se
    // produce una excepción configurada como reintentable.
    //
    // En este caso:
    //
    // Identity no responde
    // ↓
    // Feign/OpenFeign detecta el error
    // ↓
    // IdentityQueryClientFallbackFactory se ejecuta
    // ↓
    // Fallback lanza ServiceUnavailableException
    // ↓
    // Retry detecta ServiceUnavailableException
    // ↓
    // vuelve a ejecutar este método
    //
    // El nombre utilizado aquí debe coincidir con la instancia
    // configurada en application.properties:
    //
    // resilience4j.retry.instances.businessdomain-identity-query...
    @Retry(name = "businessdomain-identity-query")

    // La operación solamente realiza una consulta.
    // Por eso la transacción se marca como readOnly.
    //
    // Importante:
    // La anotación @Transactional no controla los reintentos.
    // El encargado de los reintentos es Resilience4j Retry.
    @Transactional(readOnly = true)
    @Override
    public List<UserResponse> findUsersByIds(Set<UUID> usersIds) {

        // No es necesario utilizar try/catch aquí.
        //
        // IdentityQueryClientFallbackFactory ya transforma el error
        // producido por la comunicación con Identity en:
        //
        // ServiceUnavailableException
        //
        // Como esta excepción está configurada en Retry mediante:
        //
        // resilience4j.retry.instances.businessdomain-identity-query.retry-exceptions
        //
        // Resilience4j vuelve a ejecutar este método automáticamente.
        //
        // Ejemplo:
        //
        // Intento 1 -> Identity falla
        // Fallback -> ServiceUnavailableException
        // Retry
        //
        // Intento 2 -> Identity falla
        // Fallback -> ServiceUnavailableException
        // Retry
        //
        // Intento 3 -> Identity falla
        // Fallback -> ServiceUnavailableException
        //
        // Se alcanzó max-attempts=3
        // -> no se realizan más intentos
        return identityQueryClient.findUsersByIds(
                new ArrayList<>(usersIds));

    }

    @Retry(name = "businessdomain-identity-query")
    @Override
    public List<UserResponse> listAllUsersByEmailAndExcludingIds(String email,
            List<UUID> excludedUsersIds) {
        return identityQueryClient.listAllUsersByEmailAndExcludingIds(email, excludedUsersIds);
    }
}
