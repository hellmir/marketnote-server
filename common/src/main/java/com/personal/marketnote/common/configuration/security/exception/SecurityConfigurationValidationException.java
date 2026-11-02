package com.personal.marketnote.common.configuration.security.exception;

public class SecurityConfigurationValidationException extends IllegalStateException {

    public SecurityConfigurationValidationException(String violations) {
        super("보안 설정 검증 실패. 다음 환경변수를 확인하세요:\n  - " + violations);
    }
}
