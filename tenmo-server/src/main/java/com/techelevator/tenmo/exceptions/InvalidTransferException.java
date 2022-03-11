package com.techelevator.tenmo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidTransferException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidTransferException() {
        super("Invalid transfer");
    }

}
