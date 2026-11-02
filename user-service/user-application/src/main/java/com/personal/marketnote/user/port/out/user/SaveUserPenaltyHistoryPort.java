package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.UserPenaltyHistory;

/**
 * 회원 패널티 이력 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 회원 패널티 이력 저장 기능을 제공합니다.
 */
public interface SaveUserPenaltyHistoryPort {
    /**
     * @param history 회원 패널티 이력
     * @return 저장된 회원 패널티 이력 {@link UserPenaltyHistory}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 회원 패널티 이력을 저장합니다.
     */
    UserPenaltyHistory save(UserPenaltyHistory history);
}
