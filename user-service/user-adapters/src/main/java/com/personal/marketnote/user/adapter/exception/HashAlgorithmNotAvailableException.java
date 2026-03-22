package com.personal.marketnote.user.adapter.exception;

public class HashAlgorithmNotAvailableException extends IllegalStateException {
    private static final String MESSAGE = "SHA-256 algorithm not available";

    public HashAlgorithmNotAvailableException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
