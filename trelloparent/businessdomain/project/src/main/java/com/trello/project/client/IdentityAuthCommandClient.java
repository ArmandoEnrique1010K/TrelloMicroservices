package com.trello.project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.trello.project.client.config.IdentityFeignConfig;
import com.trello.project.client.dto.response.ServiceTokenResponse;
import com.trello.project.client.fallback.IdentityAuthCommandClientFallbackFactory;

// services.identity.url=http://localhost:8081/business/v1
@FeignClient(name = "businessdomain-identity-auth-command", url = "${services.identity.url}", configuration = IdentityFeignConfig.class, fallbackFactory = IdentityAuthCommandClientFallbackFactory.class)
public interface IdentityAuthCommandClient {
    @PostMapping("/auth/internal/service-token")
    ServiceTokenResponse generateInternalToken();
}
