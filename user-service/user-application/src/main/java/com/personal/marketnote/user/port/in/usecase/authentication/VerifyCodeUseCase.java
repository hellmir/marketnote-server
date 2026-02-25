package com.personal.marketnote.user.port.in.usecase.authentication;

import com.personal.marketnote.user.port.in.command.VerifyCodeCommand;
import com.personal.marketnote.user.port.in.result.VerifyCodeResult;

/**
 * 인증 코드 검증 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 이메일 인증 코드 검증 기능을 제공합니다.
 */
public interface VerifyCodeUseCase {
    /**
     * @param verifyCodeCommand 이메일 인증 코드 검증 요청 커맨드
     * @return 이메일 인증 코드 검증 결과 {@link VerifyCodeResult}
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 이메일 인증 코드를 검증합니다.
     */
    VerifyCodeResult verifyCode(VerifyCodeCommand verifyCodeCommand);
}
