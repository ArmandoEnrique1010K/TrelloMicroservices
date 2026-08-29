package com.trello.identity.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.identity.auth.dto.response.AuthenticationResponse;
import com.trello.identity.auth.exception.InvalidRefreshTokenException;
import com.trello.identity.entities.RefreshToken;
import com.trello.identity.entities.User;
import com.trello.identity.exception.BusinessRuleException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.repositories.RefreshTokenRepository;
import com.trello.identity.service.UserIdentityService;

@Service
public class RefreshTokenService {

    private final UserIdentityService userIdentityService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    // Expirara en 30 dias el refreshToken
    private final long expirationTime = 60L * 60 * 24 * 30;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserIdentityService userIdentityService, JwtService jwtService) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.userIdentityService = userIdentityService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthenticationResponse refreshAccessToken(
            String refreshTokenValue)
            throws BusinessRuleException, UserNotFoundException {

        // 1. Obtener el hash del refresh token recibido
        String tokenHash = hashRefreshToken(refreshTokenValue);

        // 2. Buscar el refresh token en la base de datos
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        // 3. Verificar si el token ya fue revocado
        if (refreshToken.getRevokedAt() != null) {
            throw new InvalidRefreshTokenException();
        }

        // 4. Verificar si el token expiró
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException();
        }

        // 5. Obtener el usuario asociado
        User user = userIdentityService.findUserById(refreshToken.getUser().getId());

        // 6. Verificar que la cuenta siga estando confirmada
        if (!user.isConfirmed()) {
            throw new InvalidRefreshTokenException();
        }

        // 7. Generar un nuevo Access Token
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                expirationTime);

        // 8. Revocar el Refresh Token utilizado
        refreshToken.setRevokedAt(LocalDateTime.now());

        refreshTokenRepository.save(refreshToken);

        // 9. Generar un nuevo Refresh Token
        String newRefreshToken = generateRefreshToken();

        // 10. Guardar el nuevo Refresh Token
        saveRefreshToken(
                newRefreshToken,
                user.getId());

        // 11. Construir respuesta
        AuthenticationResponse response = new AuthenticationResponse();

        response.setConfirmed(user.isConfirmed());
        response.setAccessToken(accessToken);
        response.setRefreshToken(newRefreshToken);

        response.setExpiresIn(expirationTime);

        return response;
    }

    public String generateRefreshToken() {

        byte[] bytes = new byte[64];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public void saveRefreshToken(
            String refreshToken,
            UUID userId) {

        RefreshToken entity = new RefreshToken();

        User user = userIdentityService.findUserById(userId);

        entity.setTokenHash(
                hashRefreshToken(refreshToken));

        entity.setUser(user);

        entity.setExpiresAt(
                LocalDateTime.now().plusSeconds(expirationTime));

        refreshTokenRepository.save(entity);
    }

    private String hashRefreshToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 no está disponible",
                    exception);
        }
    }

    @Transactional
    public String createRefreshToken(User user) {

        String refreshTokenValue = generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setTokenHash(
                hashRefreshToken(refreshTokenValue));

        refreshToken.setUser(user);

        refreshToken.setExpiresAt(
                LocalDateTime.now().plusSeconds(expirationTime));

        refreshTokenRepository.save(refreshToken);

        return refreshTokenValue;
    }
}
