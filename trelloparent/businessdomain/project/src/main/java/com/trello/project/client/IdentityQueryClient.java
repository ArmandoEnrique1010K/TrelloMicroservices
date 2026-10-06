package com.trello.project.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.fallback.IdentityQueryClientFallbackFactory;

// Si dos interfaces contienen un @FeignClient, no pueden utilizar
// el mismo name.
//
// Convención utilizada en este proyecto:
//
// - "-query"
//      Clientes que realizan operaciones de consulta/lectura,
//      principalmente peticiones GET.
//
// - "-command"
//      Clientes que realizan operaciones que modifican estado,
//      como POST, PUT, PATCH y DELETE.
//
// Esta separación permite identificar rápidamente si un cliente
// realiza operaciones de lectura o de modificación.
@FeignClient(name = "businessdomain-identity-query", url = "${services.identity.url}", configuration = FeignClientConfig.class,
        // FallbackFactory permite definir un comportamiento alternativo
        // cuando la comunicación con el microservicio Identity falla.
        //
        // Además, permite obtener la excepción original mediante
        // el parámetro "cause" del método create().
        fallbackFactory = IdentityQueryClientFallbackFactory.class)
public interface IdentityQueryClient {

    @GetMapping("/users/batch")
    List<UserResponse> findUsersByIds(@RequestParam(required = false) List<UUID> usersIds);

    // No se pasa el @AuthenticationPrincipal Jwt jwt porque el token
    // es agregado automáticamente mediante el interceptor definido
    // en FeignClientConfig.
    @GetMapping("/users/search")
    List<UserResponse> listAllUsersByEmailAndExcludingIds(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) List<UUID> ids);
}
