package com.trello.identity.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.trello.identity.auth.dto.response.ServiceTokenResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    // public String generateAccessToken(
    // Authentication authentication,
    // long expiresIn) {

    // CustomUserDetails userDetails = (CustomUserDetails)
    // authentication.getPrincipal();

    // Instant now = Instant.now();

    // List<String> authorities = authentication.getAuthorities()
    // .stream()
    // .map(GrantedAuthority::getAuthority)
    // .toList();

    // String userId = userDetails.getUserId().toString();
    // String email = userDetails.getUsername();

    // JwtClaimsSet claims = JwtClaimsSet.builder()
    // .issuer("identity-service")
    // // El email como subject
    // // .subject(email)

    // // ID del usuario como subject
    // // .subject(userId.toString())
    // .subject(userId)
    // .issuedAt(now)

    // // Si se va a probar en la interfaz de Swagger por ejemplo, el token puede
    // // tardar más de la duración especificada en caducar, esperar al menos 1
    // minuto
    // // más para ver que caduque

    // // Expira en ... segundos
    // .expiresAt(now.plusSeconds(expiresIn))
    // .claim("email", email)
    // .claim("authorities", authorities)
    // .claim("token_type", "USER")

    // .audience(List.of("workflow-service"))
    // .build();

    // return jwtEncoder
    // .encode(JwtEncoderParameters.from(claims))
    // .getTokenValue();
    // }

    private String generateToken(
            UUID userId,
            String email,
            long expirationTime) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("identity-service")
                .subject(userId.toString())
                .audience(List.of(
                        // Se sobreentiende que este JWT se utilizara tanto para este microservicio como
                        // para los demás microservicios
                        // "identity-service",
                        "project-service", "workflow-service"))
                .expiresAt(now.plusSeconds(expirationTime))
                .issuedAt(now)
                .claim("scope", "user")
                .claim("email", email)
                // .claim("authorities", authorities)
                // .claim("token_type", "USER")
                .build();

        log.info(claims.getClaimAsString("scope"));

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateAccessToken(
            Authentication authentication,
            long expirationTime) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // List<String> authorities = authentication.getAuthorities()
        // .stream()
        // .map(GrantedAuthority::getAuthority)
        // // Spring Security 7 incluye el rol por defecto "FACTOR_PASSWORD"
        // // Es por ello que solamente se filtran los roles
        // .filter(authority -> authority.startsWith("ROLE_"))
        // .toList();

        return generateToken(
                userDetails.getUserId(),
                userDetails.getUsername(),
                expirationTime);
    }

    // SOBRECARGA
    public String generateRefreshToken(
            UUID userId,
            String email,
            long expirationTime) {

        return generateToken(
                userId,
                email,
                // Aqui se define manualmente los roles
                // List.of("ROLE_USER"),
                expirationTime);
    }

    // public String generateAccessToken(
    // UUID userId,
    // String email,
    // long expirationTime) {

    // Instant now = Instant.now();

    // JwtClaimsSet claims = JwtClaimsSet.builder()
    // .issuer("identity-service")
    // .subject(userId.toString())
    // .issuedAt(now)
    // .expiresAt(now.plusSeconds(expirationTime))
    // .claim("email", email)

    // .build();

    // return jwtEncoder.encode(
    // JwtEncoderParameters.from(claims)).getTokenValue();
    // }

    // Generar el JWT de servicio interno

    public ServiceTokenResponse generateServiceToken() {

        Instant now = Instant.now();

        Instant expiresAt = now.plusSeconds(300);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("identity-service")
                .subject("project-service")
                .audience(List.of("workflow-service"))
                .expiresAt(expiresAt)
                .issuedAt(now)
                .claim("scope", "workflow:operations")
                .build();

        log.info(claims.getClaimAsString("scope"));

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)).getTokenValue();

        ServiceTokenResponse response = new ServiceTokenResponse();
        response.setAccessToken(token);
        response.setExpiresAt(expiresAt);

        return response;
    }
}