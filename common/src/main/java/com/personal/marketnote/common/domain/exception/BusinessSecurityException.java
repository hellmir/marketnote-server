package com.personal.marketnote.common.domain.exception;

/**
 * 비즈니스 보안 예외 추상 클래스
 *
 * <p>도메인에서 발생하는 인가/접근 제어 관련 예외의 공통 부모 클래스입니다.
 * java.lang.SecurityException 대신 이 클래스를 상속하여 비즈니스 보안 예외를 정의합니다.</p>
 *
 * @Author 성효빈
 * @Date 2026-02-27
 */
public abstract class BusinessSecurityException extends RuntimeException {
    protected BusinessSecurityException(String message) {
        super(message);
    }
}
