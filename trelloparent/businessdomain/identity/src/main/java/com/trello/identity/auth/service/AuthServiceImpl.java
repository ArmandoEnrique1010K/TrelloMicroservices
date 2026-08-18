package com.trello.identity.auth.service;

import com.trello.identity.repositories.UserRepository;
import com.trello.identity.security.AuthenticationProviderConfig;
import com.trello.identity.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.identity.auth.dto.AccountRequest;
import com.trello.identity.auth.dto.AccountResponse;
import com.trello.identity.auth.dto.AuthenticationRequest;
import com.trello.identity.auth.dto.AuthenticationResponse;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.auth.mapper.AccountRequestMapper;
import com.trello.identity.auth.mapper.AccountResponseMapper;
import com.trello.identity.entities.User;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRequestMapper accountRequestMapper;
    private final AccountResponseMapper accountResponseMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthenticationProviderConfig authenticationProviderConfig;

    public AuthServiceImpl(
            UserRepository userRepository, AccountRequestMapper accountRequestMapper,
            AccountResponseMapper accountResponseMapper, AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuthenticationProviderConfig authenticationProviderConfig) {
        this.userRepository = userRepository;
        this.accountRequestMapper = accountRequestMapper;
        this.accountResponseMapper = accountResponseMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authenticationProviderConfig = authenticationProviderConfig;
    }

    @Transactional
    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) throws MismatchPasswordException,
            UserAlreadyExistsException {
        User userToAccountRequest = accountRequestMapper.accountRequestToUser(accountRequest);

        if (!accountRequest.getPassword().equals(accountRequest.getPasswordConfirmation())) {
            throw new MismatchPasswordException();
        }

        User existingUser = userRepository.findByEmail(userToAccountRequest.getEmail());

        if (existingUser != null) {
            throw new UserAlreadyExistsException();
        }

        userToAccountRequest
                .setPassword(authenticationProviderConfig.passwordEncoder().encode(accountRequest.getPassword()));

        // TODO: ESTE CAMPO DEBE SER GUARDADO COMO FALSE, PORQUE AUN NO SE HA CONFIRMADO
        // LA CUENTA DEL USUARIO SI TIENE AQUEL EMAIL REAL
        userToAccountRequest.setConfirmed(true);
        User savedUser = userRepository.save(userToAccountRequest);

        AccountResponse accountResponse = accountResponseMapper.userToAccountResponse(savedUser);

        return accountResponse;
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        User existingUser = userRepository.findByEmail(authenticationRequest.getEmail());
        if (existingUser == null) {
            // Ignora el siguiente mensaje porque se trata de una excepcion de tipo
            // BadCredentialsException de Spring
            throw new BadCredentialsException("Invalid credentials");
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));

        AuthenticationResponse response = new AuthenticationResponse();

        response.setConfirmed(existingUser.isConfirmed());

        if (!existingUser.isConfirmed()) {
            response.setAccessToken(null);
            response.setExpiresIn(0);

            return response;
        }

        String accessToken = jwtService.generateAccessToken(authentication);

        response.setAccessToken(accessToken);
        response.setExpiresIn(900);

        return response;
    }

    @Override
    public void logout() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

}
