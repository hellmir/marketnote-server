package com.personal.marketnote.user.exception;

import lombok.Getter;

@Getter
public class InvalidVendorJwtConfigException extends IllegalArgumentException {
    private static final String INVALID_VENDOR_JWT_CONFIG_MESSAGE
            = "ERR_USER_VENDOR_JWT_CONFIG::%s";

    public InvalidVendorJwtConfigException(String reason) {
        super(String.format(INVALID_VENDOR_JWT_CONFIG_MESSAGE, reason));
    }
}
