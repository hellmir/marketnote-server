package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.LoginHistory;

/**
 * 로그인 이력 저장 포트
 *
 * @Author 성효빈
 * @Date 2025-12-30
 * @Description 로그인 이력 저장 기능을 제공합니다.
 */
public interface SaveLoginHistoryPort {
    /**
     * @param loginHistory 로그인 이력
     * @Date 2025-12-30
     * @Author 성효빈
     * @Description 로그인 이력을 저장합니다.
     */
    void saveLoginHistory(LoginHistory loginHistory);
}
