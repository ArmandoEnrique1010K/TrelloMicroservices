package com.trello.identity.auth.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserAlreadyExistsException extends Exception {

    private final String code;

    public UserAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
