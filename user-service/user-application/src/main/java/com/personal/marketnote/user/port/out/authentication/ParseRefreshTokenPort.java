package com.personal.marketnote.user.port.out.authentication;

/**
 * 리프레시 토큰 파싱 포트
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 리프레시 토큰 파싱 기능을 제공합니다.
 */
public interface ParseRefreshTokenPort {
    /**
     * @param refreshToken 리프레시 토큰
     * @return 회원 ID {@link Long}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 리프레시 토큰에서 회원 ID를 추출합니다.
     */
    Long extractUserId(String refreshToken);
}


