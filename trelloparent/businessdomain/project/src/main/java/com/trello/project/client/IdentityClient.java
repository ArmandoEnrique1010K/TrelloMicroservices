package com.trello.project.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.trello.project.client.config.FeignClientConfig;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.common.SuccessfulResponse;

@FeignClient(name = "businessdomain-identity", url = "${services.identity.url}", configuration = FeignClientConfig.class)
public interface IdentityClient {

    @GetMapping("/user/{userId}")
    SuccessfulResponse<UserResponse> findUserById(@PathVariable("userId") UUID userId);

    @GetMapping("/user/batch")
    List<UserResponse> findUsersByIds(@RequestParam(required = false) List<UUID> usersIds);
}
