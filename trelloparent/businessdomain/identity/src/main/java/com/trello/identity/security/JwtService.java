package com.trello.identity.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.trello.identity.auth.dto.response.ServiceTokenResponse;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateAccessToken(
            // Authentication authentication
            UUID userId,
            String email,
            long expiresIn) {

        // CustomUserDetails userDetails = (CustomUserDetails)
        // authentication.getPrincipal();

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("trello-identity")
                // .subject(userDetails.getUsername())
                // .claim("userId", userDetails.getUserId())

                // El email como subject
                // .subject(email)
                // .claim("userId", userId.toString())

                // ID del usuario como subject
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                // Expira en ... segundos

                // Si se va a probar en la interfaz de Swagger por ejemplo, el token puede
                // tardar más de la duración especificada en caducar, esperar al menos 1 minuto
                // más para ver que caduque
                .expiresAt(now.plusSeconds(
                        expiresIn))
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    // Generar el JWT de servicio interno
    public ServiceTokenResponse generateServiceToken() {

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(300);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("identity-service")
                .subject("project-service")
                .audience(List.of("workflow-service"))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("scope", "workflow:operations")
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)).getTokenValue();

        ServiceTokenResponse response = new ServiceTokenResponse();
        response.setAccessToken(token);
        response.setExpiresAt(expiresAt);

        return response;
    }
}