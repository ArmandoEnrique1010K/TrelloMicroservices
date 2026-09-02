package com.trello.project.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.trello.project.client.config.FeignClientConfig;

@FeignClient(name = "test-service", configuration = FeignClientConfig.class)
public interface TestClient {

    @GetMapping("/test/jwt")
    Map<String, Object> getJwt();

}
