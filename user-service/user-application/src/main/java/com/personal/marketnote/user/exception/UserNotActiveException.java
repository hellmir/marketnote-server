package com.personal.marketnote.user.exception;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.springframework.security.access.AccessDeniedException;

public class UserNotActiveException extends AccessDeniedException {
    private static final String USER_NOT_ACTIVE_EXCEPTION_MESSAGE = "%s:: 비활성화된 계정입니다. 전송된 이메일 주소: %s";
    private static final String USER_NOT_ACTIVE_OAUTH2_EXCEPTION_MESSAGE = "%s:: 비활성화된 계정입니다. 인증 벤더: %s";

    public UserNotActiveException(String code, String email) {
        super(String.format(USER_NOT_ACTIVE_EXCEPTION_MESSAGE, code, email));
    }

    public UserNotActiveException(String code, AuthVendor authVendor) {
        super(String.format(USER_NOT_ACTIVE_OAUTH2_EXCEPTION_MESSAGE, code, authVendor.name()));
    }
}
