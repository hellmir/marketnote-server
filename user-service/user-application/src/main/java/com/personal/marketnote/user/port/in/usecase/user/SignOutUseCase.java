package com.personal.marketnote.user.port.in.usecase.user;

import org.springframework.http.HttpHeaders;

/**
 * 로그아웃 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 회원 로그아웃 기능을 제공합니다.
 */
public interface SignOutUseCase {
    /**
     * @return HttpHeaders {@link HttpHeaders}
     * @Author 성효빈
     * @Date 2026-01-02
     * @Description 회원 로그아웃을 수행합니다.
     */
    HttpHeaders signOut(String refreshToken);
}
