package com.personal.marketnote.user.exception;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;

@Getter
public class UserAuthProviderNotFoundException extends EntityNotFoundException {
    private static final String USER_AUTH_PROVIDER_NOT_FOUND_EXCEPTION_MESSAGE = "%s:: 존재하지 않는 회원 인증 제공자입니다. 전송된 회원 인증 제공자: %s";

    public UserAuthProviderNotFoundException(String code, AuthVendor authVendor) {
        super(String.format(USER_AUTH_PROVIDER_NOT_FOUND_EXCEPTION_MESSAGE, code, authVendor));
    }
}
