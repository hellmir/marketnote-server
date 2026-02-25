package com.personal.marketnote.user.port.out.authentication;

/**
 * 인증 코드 검증 포트
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 인증 코드 검증 기능을 제공합니다.
 */
public interface VerifyCodePort {
    /**
     * @param email      이메일 주소
     * @param targetCode 인증 코드
     * @return 인증 코드 검증 결과
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 인증 코드를 검증합니다. 검증 성공 시 인증 코드를 삭제합니다.
     */
    boolean verify(String email, String targetCode);
}
