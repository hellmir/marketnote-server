package com.personal.marketnote.user.utility.jwt.exception;

public class InvalidJwtGenerationParametersException extends IllegalArgumentException {

    public InvalidJwtGenerationParametersException(String message) {
        super(message);
    }
}
