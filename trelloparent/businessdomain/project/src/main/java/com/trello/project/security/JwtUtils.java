package com.trello.project.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

public class JwtUtils {
    private JwtUtils() {
    }

    public static UUID getUserId(Jwt jwt) {
        return UUID.fromString(
                jwt.getSubject());
    }

}
