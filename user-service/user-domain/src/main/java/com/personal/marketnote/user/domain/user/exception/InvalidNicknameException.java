package com.personal.marketnote.user.domain.user.exception;

public class InvalidNicknameException extends IllegalArgumentException {

    public InvalidNicknameException(String message) {
        super(message);
    }
}
