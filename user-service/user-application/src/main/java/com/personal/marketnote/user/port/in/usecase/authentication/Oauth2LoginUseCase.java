package com.personal.marketnote.user.port.in.usecase.authentication;

import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.user.port.in.result.LoginResult;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;

/**
 * OAuth2 소셜 로그인 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description OAuth2 소셜 로그인 기능을 제공합니다.
 */
public interface Oauth2LoginUseCase {
    /**
     * @param code        인증 코드
     * @param redirectUri 리다이렉트 URI
     * @param authVendor  인증 제공자
     * @return 로그인 결과 {@link LoginResult}
     * @throws UnsupportedCodeException 지원하지 않는 인증 코드 예외
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description OAuth2 소셜 로그인을 수행합니다.
     */
    LoginResult loginByOAuth2(String code, String redirectUri, AuthVendor authVendor) throws UnsupportedCodeException;
}
