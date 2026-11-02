package com.personal.marketnote.user.service.exception;

import java.io.IOException;

public class UnlinkOauth2AccountFailedException extends IOException {
    public UnlinkOauth2AccountFailedException(String message) {
        super(message);
    }
}
