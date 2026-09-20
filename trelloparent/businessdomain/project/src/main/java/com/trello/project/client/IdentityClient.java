package com.trello.project.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.dto.response.UserResponse;

@FeignClient(name = "businessdomain-identity", url = "${services.identity.url}", configuration = FeignClientConfig.class)
public interface IdentityClient {

    // Si hay parametros dinamicos se utiliza @PathVariable
    // @GetMapping("/user/{userId}")
    // UserResponse findUserById(@PathVariable("userId") UUID userId);

    @GetMapping("/user/batch")
    List<UserResponse> findUsersByIds(@RequestParam(required = false) List<UUID> usersIds);

    // No se pasa el @AuthenticationPrincipal Jwt jwt, debido al interceptor en
    // FeignClientConfig
    @GetMapping("/user/search")
    List<UserResponse> listAllUsersByEmailAndExcludingIds(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) List<UUID> ids);
}
