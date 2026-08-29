package com.trello.identity.auth.service;

import com.trello.identity.security.JwtService;
import com.trello.identity.security.RefreshTokenService;
import com.trello.identity.service.UserIdentityService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trello.identity.auth.dto.response.AccountResponse;
import com.trello.identity.auth.dto.response.AuthenticationResponse;
import com.trello.identity.auth.dto.request.AccountRequest;
import com.trello.identity.auth.dto.request.AuthenticationRequest;
import com.trello.identity.auth.dto.request.RefreshTokenRequest;
import com.trello.identity.auth.exception.CustomBadCredentialsException;
import com.trello.identity.auth.exception.InvalidRefreshTokenException;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.auth.mapper.AccountRequestMapper;
import com.trello.identity.auth.mapper.AccountResponseMapper;
import com.trello.identity.entities.User;
import com.trello.identity.exception.UserNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenService refreshTokenService;
    private final UserIdentityService userIdentityService;
    private final AccountRequestMapper accountRequestMapper;
    private final AccountResponseMapper accountResponseMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserIdentityService identityService, AccountRequestMapper accountRequestMapper,
            AccountResponseMapper accountResponseMapper, AuthenticationManager authenticationManager,
            JwtService jwtService,
            PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userIdentityService = identityService;
        this.accountRequestMapper = accountRequestMapper;
        this.accountResponseMapper = accountResponseMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest)
            throws MismatchPasswordException,
            UserAlreadyExistsException {

        if (!accountRequest.getPassword().equals(accountRequest.getPasswordConfirmation())) {
            throw new MismatchPasswordException();
        }

        User userToAccountRequest = accountRequestMapper.accountRequestToUser(accountRequest);

        if (userIdentityService.existsUserByEmail(userToAccountRequest.getEmail())) {
            throw new UserAlreadyExistsException();
        }

        userToAccountRequest
                .setPassword(passwordEncoder.encode(accountRequest.getPassword()));

        userToAccountRequest.setConfirmed(false);
        User savedUser = userIdentityService.saveUser(userToAccountRequest);

        AccountResponse accountResponse = accountResponseMapper.userToAccountResponse(savedUser);

        return accountResponse;
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest authenticationRequest)
            throws CustomBadCredentialsException {

        User existingUser;

        // Se utiliza un bloque try-catch porque normalmente se tiene que devolver un
        // error en común, si cae en un UserNotFoundException o en un
        // BadCredentialsException (propio de Spring Security)
        try {
            existingUser = userIdentityService.findUserByEmail(authenticationRequest.getEmail());

            // Normalmente authenticate devuelve un Authentication de
            // org.springframework.security.core, en este caso solo interesa el
            // procedimiento de inicio de sesión
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()));

            AuthenticationResponse response = new AuthenticationResponse();

            response.setConfirmed(existingUser.isConfirmed());

            // La cuenta todavía no fue confirmada
            if (!existingUser.isConfirmed()) {
                response.setAccessToken(null);
                response.setRefreshToken(null);
                response.setExpiresIn(0);

                return response;
            }

            // Tiempo de expiración: 15 minutos
            final long expirationTime = 60L * 15;

            // Tiempo de expiración: 30 segundos
            // final long expirationTime = 30L;

            String accessToken = jwtService.generateAccessToken(existingUser.getId(), existingUser.getEmail(),
                    expirationTime);
            String refreshToken = refreshTokenService.createRefreshToken(existingUser);

            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(expirationTime);

            return response;

        } catch (UserNotFoundException | BadCredentialsException exception) {
            throw new CustomBadCredentialsException();
        }
    }

    @Override
    public void logout() {
        // TODO: IMPLEMENTAR CIERRE DE SESION POR MEDIO DE COOKIE

    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws InvalidRefreshTokenException {
        return refreshTokenService.refreshAccessToken(
                request.getRefreshToken());
    }
}
