package com.personal.marketnote.user.port.in.usecase.authentication;

/**
 * 이메일 인증 요청 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 이메일 인증 요청 기능을 제공합니다.
 */
public interface SendEmailVerificationUseCase {
    /**
     * @param email 이메일 주소
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 이메일 인증 요청을 전송합니다.
     */
    void sendEmailVerification(String email);
}
