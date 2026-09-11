package com.trello.identity.user.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trello.identity.security.JwtUtils;
import com.trello.identity.user.dto.UserResponse;
import com.trello.identity.user.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "User API", description = "API para la gestión de usuarios")
@RestController
@RequestMapping("/user")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<UserResponse>> listAllUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) List<UUID> ids) {

        // TODO: HABILITAR MENSAJES DE ERRORES
        UUID userId = JwtUtils.getUserId(jwt);

        List<UUID> usersIds = new ArrayList<>();
        usersIds.add(userId);

        if (ids != null) {
            for (UUID id : ids) {
                usersIds.add(id);
            }
        }

        List<UserResponse> response = userService.listAllUsersByEmailExcludingIds(email, usersIds);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
