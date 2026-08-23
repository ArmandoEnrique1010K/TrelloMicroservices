package com.trello.identity.token.service;

import com.trello.identity.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trello.identity.entities.OtpToken;
import com.trello.identity.entities.User;
import com.trello.identity.enums.OtpPurpose;
import com.trello.identity.exception.MismatchSameOldPasswordException;
import com.trello.identity.exception.MismatchUpdatePasswordException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.repositories.OtpTokenRepository;
import com.trello.identity.service.IdentityService;
import com.trello.identity.token.dto.request.SendPasswordResetTokenRequest;
import com.trello.identity.token.dto.request.ResetPasswordRequest;
import com.trello.identity.token.dto.request.ValidateConfirmAccountTokenRequest;
import com.trello.identity.token.dto.request.ValidatePasswordResetTokenRequest;
import com.trello.identity.token.dto.response.ValidatePasswordResetTokenResponse;
import com.trello.identity.token.exception.ConfirmedAccountException;
import com.trello.identity.token.exception.InvalidTokenException;
import com.trello.identity.token.exception.UnconfirmedAccountException;
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
    public void sendConfirmAccountToken(UUID userId) throws UserNotFoundException, ConfirmedAccountException {
        User existingUser = identityService.findUserById(userId);

        if (existingUser.isConfirmed()) {
            throw new ConfirmedAccountException();
        }

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
        log.info("EL TOKEN DE 6 DIGITOS PARA ACTIVAR LA CUENTA ES: " + token);

    }

    @Override
    public void validateConfirmAccountToken(UUID userId,
            ValidateConfirmAccountTokenRequest validateConfirmAccountTokenRequest)
            throws InvalidTokenException, UserNotFoundException, ConfirmedAccountException {
        // Solamente va a tener 3 intentos de validacion del token
        final int maxAttempts = 3;

        // Token enviado por el usuario
        String tokenByUser = validateConfirmAccountTokenRequest.getToken();

        // Si no existe el token, debe lanzar una excepción
        OtpToken existingOtpToken = identityService
                .findOptionalOtpTokenByUserId(userId).orElseThrow(InvalidTokenException::new);

        User user = identityService.findUserById(userId);
        if (user.isConfirmed()) {
            throw new ConfirmedAccountException();
        }

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
    public void sendPasswordResetToken(SendPasswordResetTokenRequest sendPasswordResetTokenRequest)
            throws UserNotFoundException, UnconfirmedAccountException {

        String email = sendPasswordResetTokenRequest.getEmail();

        User existingUser = identityService.findUserByEmail(email);

        if (!existingUser.isConfirmed()) {
            throw new UnconfirmedAccountException();
        }

        UUID userId = existingUser.getId();

        // Genera el token de 6 digitos
        String token = TokenUtils.generateOtp();

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
        otpToken.setOtpPurpose(OtpPurpose.PASSWORD_RESET);

        otpTokenRepository.save(otpToken);
        // TODO: IMPLEMENTAR UN SERVICIO DE ENVIO DE CORREO REAL
        // En este caso se imprimira en consola el token de 6 digitos
        log.info("EL TOKEN DE 6 DIGITOS PARA ACTIVAR LA CUENTA ES: " + token);

    }

    @Override
    public ValidatePasswordResetTokenResponse validatePasswordResetToken(
            ValidatePasswordResetTokenRequest validatePasswordResetTokenRequest)
            throws InvalidTokenException, UserNotFoundException, UnconfirmedAccountException {
        // Solamente va a tener 3 intentos de validacion del token
        final int maxAttempts = 3;

        // Token enviado por el usuario
        String email = validatePasswordResetTokenRequest.getEmail();
        String tokenByUser = validatePasswordResetTokenRequest.getToken();
        User user = identityService.findUserByEmail(email);
        UUID userId = user.getId();

        // Si no existe el token, debe lanzar una excepción
        OtpToken existingOtpToken = identityService
                .findOptionalOtpTokenByUserId(userId).orElseThrow(InvalidTokenException::new);

        if (!user.isConfirmed()) {
            throw new UnconfirmedAccountException();
        }

        // Si no es un token de confirmación de cuenta
        if (existingOtpToken.getOtpPurpose() != OtpPurpose.PASSWORD_RESET) {
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

            // Genera el UUID
            UUID resetToken = TokenUtils.generateResetToken();

            existingOtpToken.setUsedAt(LocalDateTime.now());
            existingOtpToken.setResetToken(resetToken);
            otpTokenRepository.save(existingOtpToken);

            ValidatePasswordResetTokenResponse response = new ValidatePasswordResetTokenResponse();
            response.setResetToken(resetToken);

            return response;
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
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) throws UserNotFoundException,
            UnconfirmedAccountException, MismatchUpdatePasswordException, MismatchSameOldPasswordException {

        UUID resetToken = resetPasswordRequest.getResetToken();

        User existingUser = identityService.findUserByOtpTokenResetToken(resetToken);

        if (!existingUser.isConfirmed()) {
            throw new UnconfirmedAccountException();
        }

        if (!resetPasswordRequest.getNewPassword()
                .equals(resetPasswordRequest.getNewPasswordConfirmation())) {
            throw new MismatchUpdatePasswordException();
        }

        if (passwordEncoder.matches(existingUser.getPassword(), resetPasswordRequest.getNewPassword())) {
            throw new MismatchSameOldPasswordException();
        }

        existingUser.setPassword(resetPasswordRequest.getNewPassword());
        userRepository.save(existingUser);
    }

}
