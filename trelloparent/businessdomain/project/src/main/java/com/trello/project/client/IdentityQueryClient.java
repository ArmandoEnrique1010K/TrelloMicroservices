package com.trello.project.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.dto.response.UserResponse;

// Si 2 clases contienen un @FeignClient, no pueden tener el mismo name
// Se recomienda colocar al final del name lo siguiente:
// - "-query" si va a contener solamente peticiones de tipo GET (solamente lectura)
// - "-command" si va a contener peticiones de tipo POST, PUT, PATCH y DELETE; 
// (modifica estado), peticiones que no hacen un retry automatico por defecto.

@FeignClient(name = "businessdomain-identity-query", url = "${services.identity.url}", configuration = FeignClientConfig.class)
public interface IdentityQueryClient {

    @GetMapping("/users/batch")
    List<UserResponse> findUsersByIds(@RequestParam(required = false) List<UUID> usersIds);

    // No se pasa el @AuthenticationPrincipal Jwt jwt, debido al interceptor en
    // FeignClientConfig
    @GetMapping("/users/search")
    List<UserResponse> listAllUsersByEmailAndExcludingIds(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) List<UUID> ids);
}
