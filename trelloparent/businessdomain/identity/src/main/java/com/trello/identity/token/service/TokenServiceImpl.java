package com.trello.identity.token.service;

import com.trello.identity.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trello.identity.entities.OtpToken;
import com.trello.identity.entities.User;
import com.trello.identity.enums.OtpPurpose;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.repositories.OtpTokenRepository;
import com.trello.identity.service.IdentityService;
import com.trello.identity.token.dto.request.EmailRequest;
import com.trello.identity.token.dto.request.ResetPasswordRequest;
import com.trello.identity.token.dto.request.TokenRequest;
import com.trello.identity.token.dto.response.ValidatePasswordResetTokenResponse;
import com.trello.identity.token.exception.InvalidTokenException;
import com.trello.identity.token.service.utils.TokenUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final IdentityService identityService;
    private final PasswordEncoder passwordEncoder;

    public TokenServiceImpl(
            OtpTokenRepository otpTokenRepository,
            IdentityService identityService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.otpTokenRepository = otpTokenRepository;
        this.identityService = identityService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void sendConfirmAccountToken(UUID userId) throws UserNotFoundException {
        User existingUser = identityService.findUserById(userId);

        // Genera el token de 6 digitos
        String token = TokenUtils.generateOtp();

        // Recordar que hay una relacion de uno a uno entre User y OtpToken
        OtpToken otpToken = identityService.findOtpTokenByUserId(userId);

        // Si no hay un OtpToken, debe crear uno, si lo hay debe modificar sus campos
        if (otpToken == null) {
            otpToken = new OtpToken();
            otpToken.setUser(existingUser);
        }

        otpToken.setOtpHash(passwordEncoder.encode(token));
        otpToken.setAttemps(0);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpToken.setUsedAt(null);
        otpToken.setResetToken(null);
        otpToken.setOtpPurpose(OtpPurpose.ACCOUNT_CONFIRMATION);

        otpTokenRepository.save(otpToken);
        // TODO: IMPLEMENTAR UN SERVICIO DE ENVIO DE CORREO REAL
        // En este caso se imprimira en consola el token de 6 digitos
        log.info("EL TOKEN DE 6 DIGITOS ES: " + token);

    }

    @Override
    public void validateConfirmAccountToken(UUID userId, TokenRequest tokenRequest)
            throws InvalidTokenException, UserNotFoundException {
        // Solamente va a tener 3 intentos de validacion del token
        final int maxAttempts = 3;

        // Token enviado por el usuario
        String tokenByUser = tokenRequest.getToken();

        // Si no existe el token, debe lanzar una excepción
        OtpToken existingOtpToken = identityService
                .findOptionalOtpTokenByUserId(userId).orElseThrow(InvalidTokenException::new);

        // Si no es un token de confirmación de cuenta
        if (existingOtpToken.getOtpPurpose() != OtpPurpose.ACCOUNT_CONFIRMATION) {
            throw new InvalidTokenException();
        }

        // Token expirado
        if (LocalDateTime.now().isAfter(existingOtpToken.getExpiresAt())) {
            throw new InvalidTokenException();
        }

        // Token ya utilizado
        if (existingOtpToken.getUsedAt() != null) {
            throw new InvalidTokenException();
        }

        // Token correcto
        if (passwordEncoder.matches(
                tokenByUser,
                existingOtpToken.getOtpHash())) {
            existingOtpToken.setUsedAt(LocalDateTime.now());
            otpTokenRepository.save(existingOtpToken);

            // Activar cuenta del usuario
            User user = identityService.findUserById(userId);
            user.setConfirmed(true);
            userRepository.save(user);

            return;
        }

        // Token incorrecto
        int attempts = existingOtpToken.getAttemps() + 1;
        existingOtpToken.setAttemps(attempts);

        if (attempts >= maxAttempts) {
            otpTokenRepository.delete(existingOtpToken);
            throw new InvalidTokenException();
        }

        otpTokenRepository.save(existingOtpToken);

        throw new InvalidTokenException();
    }

    @Override
    public void sendPasswordResetToken(EmailRequest emailRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendPasswordResetToken'");
    }

    @Override
    public ValidatePasswordResetTokenResponse validatePasswordResetToken(TokenRequest tokenRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validatePasswordResetToken'");
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetPassword'");
    }

}
