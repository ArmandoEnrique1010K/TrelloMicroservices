package com.trello.identity.security;

import java.time.Instant;
import java.util.UUID;

// import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

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
                .subject(email)
                .claim("userId", userId.toString())
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

}