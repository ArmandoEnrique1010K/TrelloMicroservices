package com.trello.workflow.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.trello.workflow.client.config.FeignClientConfig;
import com.trello.workflow.client.dto.response.UserResponse;
import com.trello.workflow.client.fallback.IdentityUserQueryClientFallbackFactory;

@FeignClient(name = "businessdomain-identity-user-query", url = "${services.identity.url}", configuration = FeignClientConfig.class, fallbackFactory = IdentityUserQueryClientFallbackFactory.class)
public interface IdentityUserQueryClient {

    @GetMapping("/users/batch")
    List<UserResponse> findUsersByIds(@RequestParam(required = false) List<UUID> usersIds);

    @GetMapping("/users/{userId}")
    UserResponse findUserById(@PathVariable("userId") UUID userId);

}
