package com.personal.marketnote.user.security.token.vendor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OAuth 2.0 Authorization Server 목록을 정리한 enum
 *
 * @author 성효빈
 */
public enum AuthVendor {
    NATIVE("일반 회원"),
    KAKAO("카카오"),
    GOOGLE("구글"),
    APPLE("애플");

    private final String description;

    AuthVendor(String description) {
        this.description = description;
    }

    public static AuthVendor valueOfIgnoreCase(String value) throws IllegalArgumentException {
        return valueOf(value.toUpperCase());
    }

    public static int size() {
        return values().length;
    }

    public boolean isNative() {
        return this == NATIVE;
    }

    public List<AuthVendor> getExceptValues() {
        return Arrays.stream(values())
                .filter(authVendor -> authVendor != this)
                .collect(Collectors.toList());
    }

    public boolean isMe(AuthVendor authVendor) {
        return this == authVendor;
    }

}
