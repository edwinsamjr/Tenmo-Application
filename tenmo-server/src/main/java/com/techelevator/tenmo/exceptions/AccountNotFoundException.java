package com.techelevator.tenmo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public AccountNotFoundException() {
        super("Account not found");
    }

}
