package com.scalablescripts.auth.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PasswordsdontMatchError extends ResponseStatusException {
    public PasswordsdontMatchError(){
        super(HttpStatus.BAD_REQUEST, "Passwords dont match");
    }
}
