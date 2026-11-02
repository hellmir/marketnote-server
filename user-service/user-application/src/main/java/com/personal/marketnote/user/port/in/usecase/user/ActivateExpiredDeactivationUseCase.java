package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.port.in.result.ActivateExpiredDeactivationResult;

/**
 * 비활성화 기간 만료 자동 활성화 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 비활성화 기간이 만료된 회원을 자동으로 활성화합니다.
 */
public interface ActivateExpiredDeactivationUseCase {
    /**
     * @return 활성화 결과 {@link ActivateExpiredDeactivationResult}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 비활성화 기간이 현재 시각보다 이전인 INACTIVE 회원을 일괄 활성화합니다.
     */
    ActivateExpiredDeactivationResult activateExpiredDeactivations();
}
