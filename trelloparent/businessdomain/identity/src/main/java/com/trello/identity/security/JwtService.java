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

    // Método privado encargado de construir y firmar JWT de usuario.
    // Recibe la información del usuario y el tiempo de expiración
    // para generar un token con los claims correspondientes.
    private String generateToken(
            UUID userId,
            String email,
            long expirationTime) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                // Identifica al servicio emisor del JWT.
                .issuer("identity-service")

                // Identificador del usuario autenticado.
                .subject(userId.toString())

                // Servicios destinatarios que pueden aceptar este JWT.
                // El servicio receptor también debe validar el claim aud.
                .audience(List.of(
                        // Se sobreentiende que este JWT se utilizara tanto para este microservicio como
                        // para los demás microservicios
                        // "identity-service",
                        "project-service", "workflow-service"))

                // Fecha y hora en la que el JWT dejará de ser válido.
                .expiresAt(now.plusSeconds(expirationTime))

                // Fecha y hora en la que se generó el JWT.
                .issuedAt(now)

                // Scope que identifica al token como un JWT de usuario.
                // Spring Security lo transforma en la autoridad SCOPE_user.
                .claim("scope", "user")

                // Correo electrónico asociado al usuario.
                .claim("email", email)
                .build();

        // log.info(claims.getClaimAsString("scope"));

        // Firma y serializa el JWT utilizando el JwtEncoder configurado.
        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)).getTokenValue();
    }

    // Genera un JWT de acceso para un usuario autenticado.
    // Obtiene los datos del usuario desde el Authentication recibido
    // y delega la construcción del token al método generateToken().
    public String generateAccessToken(
            Authentication authentication,
            long expirationTime) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Si posteriormente se implementan roles, se pueden obtener
        // las autoridades del usuario y agregarlas como claims al JWT.

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

    // Genera un JWT de recarga (Refresh Token).
    // Actualmente utiliza la misma estructura y los mismos claims
    // que el JWT de acceso, pero permite establecer una expiración diferente.
    //
    // IMPORTANTE:
    // Si se utiliza esta implementación, el refresh token también tendrá
    // el scope "user". Para una separación estricta, conviene diferenciar
    // ambos tipos de tokens mediante un claim específico y validar su uso.
    public String generateRefreshToken(
            UUID userId,
            String email,
            long expirationTime) {

        return generateToken(
                userId,
                email,
                expirationTime);
    }

    // Genera un JWT exclusivo para la comunicación interna entre servicios.
    // Este token no representa a un usuario, sino a un servicio autorizado.
    public ServiceTokenResponse generateServiceToken() {

        Instant now = Instant.now();

        // Los JWT internos tienen una duración corta para reducir
        // el tiempo de exposición ante una posible filtración.
        Instant expiresAt = now.plusSeconds(300);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                // Servicio responsable de emitir el token.
                .issuer("identity-service")

                // Identidad del servicio que representa el JWT.
                .subject("project-service")

                // Únicamente workflow-service está autorizado como destinatario.
                .audience(List.of("workflow-service"))
                // Fecha de expiración del JWT interno.
                .expiresAt(expiresAt)

                // Fecha de emisión del JWT.
                .issuedAt(now)

                // Scope exclusivo para operaciones internas de Workflow.
                // Spring Security lo transforma en la autoridad:
                // SCOPE_workflow:operations
                .claim("scope", "workflow:operations")

                .build();

        // log.info(claims.getClaimAsString("scope"));

        // Firma y serializa el JWT interno.
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)).getTokenValue();

        // Devuelve el token y su fecha de expiración.
        ServiceTokenResponse response = new ServiceTokenResponse();
        response.setAccessToken(token);
        response.setExpiresAt(expiresAt);

        return response;
    }
}