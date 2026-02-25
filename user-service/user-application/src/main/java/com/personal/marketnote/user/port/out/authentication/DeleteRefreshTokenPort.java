package com.personal.marketnote.user.port.out.authentication;

/**
 * 리프레시 토큰 삭제 포트
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 리프레시 토큰 삭제 기능을 제공합니다.
 */
public interface DeleteRefreshTokenPort {
    /**
     * @param userId 회원 ID
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 회원의 리프레시 토큰을 삭제합니다.
     */
    void deleteByUserId(Long userId);
}


