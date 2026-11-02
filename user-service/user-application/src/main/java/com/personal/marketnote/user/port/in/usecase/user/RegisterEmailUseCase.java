package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;

/**
 * 이메일 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 회원 이메일 등록 기능을 제공합니다.
 */
public interface RegisterEmailUseCase {
    /**
     * @param id         회원 ID
     * @param authVendor 인증 제공자
     * @param email      이메일 주소
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 사용자의 이메일 주소를 등록합니다.
     */
    void registerEmail(Long id, AuthVendor authVendor, String email);
}
