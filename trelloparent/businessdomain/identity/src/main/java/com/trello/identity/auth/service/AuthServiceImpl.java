package com.trello.identity.auth.service;

import com.trello.identity.repositories.UserRepository;
import com.trello.identity.security.JwtService;
import com.trello.identity.service.IdentityService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trello.identity.auth.dto.response.AccountResponse;
import com.trello.identity.auth.dto.response.AuthenticationResponse;
import com.trello.identity.auth.dto.request.AccountRequest;
import com.trello.identity.auth.dto.request.AuthenticationRequest;
import com.trello.identity.auth.exception.CustomBadCredentialsException;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.auth.mapper.AccountRequestMapper;
import com.trello.identity.auth.mapper.AccountResponseMapper;
import com.trello.identity.entities.User;
import com.trello.identity.exception.UserNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final IdentityService identityService;
    private final AccountRequestMapper accountRequestMapper;
    private final AccountResponseMapper accountResponseMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository, IdentityService identityService, AccountRequestMapper accountRequestMapper,
            AccountResponseMapper accountResponseMapper, AuthenticationManager authenticationManager,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.identityService = identityService;
        this.accountRequestMapper = accountRequestMapper;
        this.accountResponseMapper = accountResponseMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest)
            throws MismatchPasswordException,
            UserAlreadyExistsException {

        if (!accountRequest.getPassword().equals(accountRequest.getPasswordConfirmation())) {
            throw new MismatchPasswordException();
        }

        User userToAccountRequest = accountRequestMapper.accountRequestToUser(accountRequest);

        if (identityService.existsUserByEmail(userToAccountRequest.getEmail())) {
            throw new UserAlreadyExistsException();
        }

        userToAccountRequest
                .setPassword(passwordEncoder.encode(accountRequest.getPassword()));

        // TODO: ESTE CAMPO DEBE SER GUARDADO COMO FALSE, PORQUE AUN NO SE HA CONFIRMADO
        // LA CUENTA DEL USUARIO SI TIENE AQUEL EMAIL REAL
        userToAccountRequest.setConfirmed(true);
        User savedUser = userRepository.save(userToAccountRequest);

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
            existingUser = identityService.findUserByEmail(authenticationRequest.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()));

            AuthenticationResponse response = new AuthenticationResponse();

            String accessToken = jwtService.generateAccessToken(authentication);

            response.setAccessToken(accessToken);
            response.setExpiresIn(900);
            response.setConfirmed(existingUser.isConfirmed());

            return response;

        } catch (UserNotFoundException | BadCredentialsException exception) {
            throw new CustomBadCredentialsException();
        }
    }

    @Override
    public void logout() {
        // TODO: IMPLEMENTAR CIERRE DE SESION POR MEDIO DE COOKIE

    }
}
