package com.trello.project.workspace.controller;

import java.util.HashMap;
import java.util.Map;

// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.project.client.TestClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

// Endpoint de prueba para probar el JWT
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private TestClient testClient;

    public TestController(TestClient testClient) {
        this.testClient = testClient;
    }

    @GetMapping("/feign-jwt")
    public ResponseEntity<Map<String, Object>> testFeignJwt() {
        return ResponseEntity.ok(testClient.getJwt());

    };

    @Operation(summary = "Prueba de autorización")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/authorization")
    public ResponseEntity<Map<String, Object>> authorization(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        System.out.println("Authorization: " + authorization);

        Map<String, Object> response = new HashMap<>();

        response.put("received", authorization != null);
        response.put("authorization", authorization);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Prueba para obtener ID del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/authorization2")
    public ResponseEntity<Map<String, Object>> authorization(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        Map<String, Object> response = new HashMap<>();

        response.put("userId", userId);
        response.put("email", email);

        return ResponseEntity.ok(response);
    }
}
