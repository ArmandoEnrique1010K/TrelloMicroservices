package com.trello.workflow.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.trello.workflow.client.config.FeignClientConfig;
import com.trello.workflow.client.dto.response.UserResponse;

@FeignClient(name = "businessdomain-identity", url = "${services.identity.url}", configuration = FeignClientConfig.class)
public interface IdentityClient {
    @GetMapping("/user/batch")
    List<UserResponse> findUsersByIds(@RequestParam(required = false) List<UUID> usersIds);

    @GetMapping("/user/{userId}")
    UserResponse findUserById(@PathVariable("userId") UUID userId);
}
