package com.techelevator.tenmo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class TransferNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public TransferNotFoundException() {
        super("Transfer Id does not exist or you are unauthorized to view transfer");
    }

}
