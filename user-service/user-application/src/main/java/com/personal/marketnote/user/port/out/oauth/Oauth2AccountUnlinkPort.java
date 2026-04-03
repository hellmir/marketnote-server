package com.personal.marketnote.user.port.out.oauth;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;
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
     * @param vendor     OAuth2 벤더
     * @param credential 벤더별 연결 해제에 필요한 자격 증명 (OIDC ID 또는 액세스 토큰)
     * @throws UnlinkOauth2AccountFailedException 계정 연결 해제 실패 시
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 지정된 벤더의 계정 연결을 해제합니다.
     */
    void unlinkAccount(AuthVendor vendor, String credential) throws UnlinkOauth2AccountFailedException;
}
