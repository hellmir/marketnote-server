package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 회원 패널티 이력 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 회원 패널티 이력 조회 기능을 제공합니다.
 */
public interface FindUserPenaltyHistoryPort {
    /**
     * @param pageable 페이징 정보
     * @param userId   회원 ID
     * @return 패널티 이력 페이지 {@link Page}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 특정 회원의 패널티 이력을 페이징 조회합니다.
     */
    Page<UserPenaltyHistory> findUserPenaltyHistoriesByUserId(Pageable pageable, Long userId);
}
