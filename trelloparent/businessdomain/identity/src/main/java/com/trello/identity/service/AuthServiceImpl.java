package com.trello.identity.service;

import com.trello.identity.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.identity.dtos.auth.AccountRequest;
import com.trello.identity.dtos.auth.AccountResponse;
import com.trello.identity.entities.User;
import com.trello.identity.exception.BusinessRuleException;
import com.trello.identity.mapper.auth.AccountRequestMapper;
import com.trello.identity.mapper.auth.AccountResponseMapper;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRequestMapper accountRequestMapper;
    private final AccountResponseMapper accountResponseMapper;

    public AuthServiceImpl(UserRepository userRepository, AccountRequestMapper accountRequestMapper,
            AccountResponseMapper accountResponseMapper) {
        this.userRepository = userRepository;
        this.accountRequestMapper = accountRequestMapper;
        this.accountResponseMapper = accountResponseMapper;
    }

    @Transactional
    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) throws BusinessRuleException {
        User userToAccountRequest = accountRequestMapper.accountRequestToUser(accountRequest);

        if (!accountRequest.getPassword().equals(accountRequest.getPasswordConfirmation())) {
            throw new BusinessRuleException("1025", "Error, las contraseñas no coinciden", HttpStatus.CONFLICT);
        }

        User existingUser = userRepository.findByEmail(userToAccountRequest.getEmail());

        if (existingUser != null) {
            throw new BusinessRuleException("1025", "Error, el usuario existe", HttpStatus.CONFLICT);
        }

        // TODO: ESTE CAMPO DEBE SER GUARDADO COMO FALSE, PORQUE AUN NO SE HA CONFIRMADO
        // LA CUENTA DEL USUARIO SI TIENE AQUEL EMAIL REAL
        userToAccountRequest.setConfirmed(true);
        User savedUser = userRepository.save(userToAccountRequest);

        AccountResponse accountResponse = accountResponseMapper.userToAccountResponse(savedUser);

        return accountResponse;
    }

    @Override
    public String login() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public void logout() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }
}
