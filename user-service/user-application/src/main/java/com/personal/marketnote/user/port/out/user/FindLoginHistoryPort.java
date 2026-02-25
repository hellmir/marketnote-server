package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 로그인 이력 조회 포트
 *
 * @Author 성효빈
 * @Date 2025-12-30
 * @Description 로그인 이력 조회 기능을 제공합니다.
 */
public interface FindLoginHistoryPort {
    /**
     * @param pageable 페이지네이션 정보
     * @param userId   회원 ID
     * @return 로그인 이력 목록 {@link Page<LoginHistory>}
     * @Date 2025-12-30
     * @Author 성효빈
     * @Description 회원의 로그인 이력을 조회합니다.
     */
    Page<LoginHistory> findLoginHistoriesByUserId(Pageable pageable, Long userId);
}
