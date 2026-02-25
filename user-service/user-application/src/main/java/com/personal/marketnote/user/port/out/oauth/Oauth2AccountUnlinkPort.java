package com.personal.marketnote.user.port.out.oauth;

import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;

/**
 * OAuth2 계정 연결 해제 포트
 *
 * @Author 성효빈
 * @Date 2025-12-29
 * @Description 외부 OAuth2 API와의 연결 해제 기능을 제공합니다.
 */
public interface Oauth2AccountUnlinkPort {
    /**
     * @param oidcId 카카오 OIDC ID
     * @throws UnlinkOauth2AccountFailedException 카카오 계정 연결 해제 실패 시
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 카카오 계정 연결을 해제합니다.
     */
    void unlinkKakaoAccount(String oidcId) throws UnlinkOauth2AccountFailedException;

    /**
     * @param accessToken 구글 액세스 토큰
     * @throws UnlinkOauth2AccountFailedException 구글 계정 연결 해제 실패 시
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 구글 계정 연결을 해제합니다.
     */
    void unlinkGoogleAccount(String accessToken) throws UnlinkOauth2AccountFailedException;
}
