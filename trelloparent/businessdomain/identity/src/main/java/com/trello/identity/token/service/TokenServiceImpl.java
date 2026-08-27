package com.trello.identity.token.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.identity.entities.OtpToken;
import com.trello.identity.entities.User;
import com.trello.identity.enums.OtpPurpose;
import com.trello.identity.exception.MismatchSameOldPasswordException;
import com.trello.identity.exception.MismatchUpdatePasswordException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.service.OtpTokenIdentityService;
import com.trello.identity.service.UserIdentityService;
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

    private final UserIdentityService userIdentityService;
    private final OtpTokenIdentityService otpTokenIdentityService;
    private final PasswordEncoder passwordEncoder;

    public TokenServiceImpl(
            UserIdentityService userIdentityService,
            OtpTokenIdentityService otpTokenIdentityService, PasswordEncoder passwordEncoder) {
        this.userIdentityService = userIdentityService;
        this.otpTokenIdentityService = otpTokenIdentityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void sendConfirmAccountToken(UUID userId) throws UserNotFoundException, ConfirmedAccountException {
        User existingUser = userIdentityService.findUserById(userId);

        if (existingUser.isConfirmed()) {
            throw new ConfirmedAccountException();
        }

        // Genera el token de 6 digitos
        String token = TokenUtils.generateOtp();

        // Recordar que hay una relacion de uno a uno entre User y OtpToken
        // Si no hay un OtpToken, debe crear uno, si lo hay debe modificar sus campos
        OtpToken otpToken = otpTokenIdentityService
                .findOptionalOtpTokenByUserId(userId)
                .orElseGet(() -> {
                    OtpToken newOtpToken = new OtpToken();
                    newOtpToken.setUser(existingUser);
                    return newOtpToken;
                });

        otpToken.setOtpHash(passwordEncoder.encode(token));
        otpToken.setAttemps(0);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpToken.setResetToken(null);
        otpToken.setOtpPurpose(OtpPurpose.ACCOUNT_CONFIRMATION);
        otpTokenIdentityService.saveOtpToken(otpToken);
        // TODO: IMPLEMENTAR UN SERVICIO DE ENVIO DE CORREO REAL
        // En este caso se imprimira en consola el token de 6 digitos
        log.info("EL TOKEN DE 6 DIGITOS PARA ACTIVAR LA CUENTA ES: " + token);

    }

    // Es necesario el uso de transactional
    // Como hay varios save que hacen cambios en la base de datos,
    // la operación debe ejecutarse dentro de una sola transacción
    // para garantizar la consistencia de los datos y evitar estados parciales
    // en caso de que la validación del token falle o se complete exitosamente.

    // La propiedad noRollbackFor evita que se haga un roolback (restauración de
    // datos) cuando cae en la excepción InvalidTokenException

    // Tambien se sabe que si se llama a un metodo del repositorio que lleva la
    // anotacion @Modifying, el metodo del servicio debe llevar la anotación
    // @Transactional
    @Transactional(noRollbackFor = InvalidTokenException.class)
    @Override
    public void validateConfirmAccountToken(UUID userId,
            ValidateConfirmAccountTokenRequest validateConfirmAccountTokenRequest)
            throws InvalidTokenException, UserNotFoundException, ConfirmedAccountException {
        // Solamente va a tener 3 intentos de validacion del token
        final int maxAttempts = 3;

        // Token enviado por el usuario
        String tokenByUser = validateConfirmAccountTokenRequest.getToken();

        // Si no existe el token, debe lanzar una excepción
        OtpToken existingOtpToken = otpTokenIdentityService
                .findOptionalOtpTokenByUserId(userId).orElseThrow(InvalidTokenException::new);

        User user = userIdentityService.findUserById(userId);
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

        // Token correcto
        if (passwordEncoder.matches(
                tokenByUser,
                existingOtpToken.getOtpHash())) {
            otpTokenIdentityService.saveOtpToken(existingOtpToken);

            // Activar cuenta del usuario
            user.setConfirmed(true);
            userIdentityService.saveUser(user);

            // Borrar el token
            otpTokenIdentityService.deleteOtpTokenById(existingOtpToken.getId());

            return;
        }

        // Token incorrecto
        int attempts = existingOtpToken.getAttemps() + 1;
        existingOtpToken.setAttemps(attempts);

        if (attempts >= maxAttempts) {

            /*
             * Al alcanzar el máximo de intentos, se elimina el token mediante una
             * consulta DELETE directa definida en el repositorio con @Modifying.
             *
             * La eliminación se ejecuta dentro de la transacción actual. Como el método
             * está configurado con noRollbackFor = InvalidTokenException.class,
             * la excepción que se lanza después del DELETE no provoca un rollback.
             *
             * Por lo tanto, la transacción finaliza con COMMIT y el token eliminado
             * queda eliminado definitivamente de la base de datos.
             */
            otpTokenIdentityService.deleteOtpTokenById(existingOtpToken.getId());

            throw new InvalidTokenException();
        }

        otpTokenIdentityService.saveOtpToken(existingOtpToken);

        throw new InvalidTokenException();
    }

    @Override
    public void sendPasswordResetToken(SendPasswordResetTokenRequest sendPasswordResetTokenRequest)
            throws UserNotFoundException, UnconfirmedAccountException {

        String email = sendPasswordResetTokenRequest.getEmail();

        User existingUser = userIdentityService.findUserByEmail(email);

        if (!existingUser.isConfirmed()) {
            throw new UnconfirmedAccountException();
        }

        UUID userId = existingUser.getId();

        // Genera el token de 6 digitos
        String token = TokenUtils.generateOtp();

        OtpToken otpToken = otpTokenIdentityService
                .findOptionalOtpTokenByUserId(userId)
                .orElseGet(() -> {
                    OtpToken newOtpToken = new OtpToken();
                    newOtpToken.setUser(existingUser);
                    return newOtpToken;
                });

        // Si no hay un OtpToken, debe crear uno, si lo hay debe modificar sus campos
        otpToken.setOtpHash(passwordEncoder.encode(token));
        otpToken.setAttemps(0);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpToken.setResetToken(null);
        otpToken.setOtpPurpose(OtpPurpose.PASSWORD_RESET);

        otpTokenIdentityService.saveOtpToken(otpToken);
        // TODO: IMPLEMENTAR UN SERVICIO DE ENVIO DE CORREO REAL
        // En este caso se imprimira en consola el token de 6 digitos
        log.info("EL TOKEN DE 6 DIGITOS PARA ACTIVAR LA CUENTA ES: " + token);

    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    @Override
    public ValidatePasswordResetTokenResponse validatePasswordResetToken(
            ValidatePasswordResetTokenRequest validatePasswordResetTokenRequest)
            throws InvalidTokenException, UserNotFoundException, UnconfirmedAccountException {
        // Solamente va a tener 3 intentos de validacion del token
        final int maxAttempts = 3;

        // Token enviado por el usuario
        String email = validatePasswordResetTokenRequest.getEmail();
        String tokenByUser = validatePasswordResetTokenRequest.getToken();
        User user = userIdentityService.findUserByEmail(email);
        UUID userId = user.getId();

        // Si no existe el token, debe lanzar una excepción
        OtpToken existingOtpToken = otpTokenIdentityService
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

        // Token correcto
        if (passwordEncoder.matches(
                tokenByUser,
                existingOtpToken.getOtpHash())) {

            // Genera el UUID
            UUID resetToken = TokenUtils.generateResetToken();

            existingOtpToken.setResetToken(resetToken);
            otpTokenIdentityService.saveOtpToken(existingOtpToken);

            ValidatePasswordResetTokenResponse response = new ValidatePasswordResetTokenResponse();
            response.setResetToken(resetToken);

            return response;
        }

        // Token incorrecto
        int attempts = existingOtpToken.getAttemps() + 1;
        existingOtpToken.setAttemps(attempts);

        if (attempts >= maxAttempts) {
            otpTokenIdentityService.deleteOtpTokenById(existingOtpToken.getId());
            throw new InvalidTokenException();
        }

        otpTokenIdentityService.saveOtpToken(existingOtpToken);

        throw new InvalidTokenException();

    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) throws UserNotFoundException,
            UnconfirmedAccountException, MismatchUpdatePasswordException, MismatchSameOldPasswordException {

        UUID resetToken = resetPasswordRequest.getResetToken();

        // Buscar token por UUID
        OtpToken existingOtpToken = otpTokenIdentityService.findOtpTokenByResetToken(resetToken);

        User existingUser = userIdentityService.findUserByOtpTokenResetToken(resetToken);

        if (!existingUser.isConfirmed()) {
            throw new UnconfirmedAccountException();
        }

        if (!resetPasswordRequest.getNewPassword()
                .equals(resetPasswordRequest.getNewPasswordConfirmation())) {
            throw new MismatchUpdatePasswordException();
        }

        if (passwordEncoder.matches(
                resetPasswordRequest.getNewPassword(),
                existingUser.getPassword())) {
            throw new MismatchSameOldPasswordException();
        }

        existingUser.setPassword(
                passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userIdentityService.saveUser(existingUser);

        // Borrar el token
        otpTokenIdentityService.deleteOtpTokenById(existingOtpToken.getId());
    }

}
