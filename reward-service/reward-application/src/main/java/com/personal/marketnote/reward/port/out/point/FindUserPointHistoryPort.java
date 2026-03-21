package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;

import java.time.LocalDate;
import java.util.List;

/**
 * 회원 포인트 이력 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-19
 * @Description 회원 포인트 이력 조회 관련 기능을 제공합니다.
 */
public interface FindUserPointHistoryPort {
    /**
     * @param userId    회원 식별자
     * @param filter    포인트 이력 필터
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @param cursor    커서 (마지막 조회 항목의 ID, null이면 첫 페이지)
     * @param pageSize  조회 건수 (pageSize + 1 포함)
     * @return 회원 포인트 이력 목록 {@link UserPointHistory}
     * @Date 2026-01-19
     * @Author 성효빈
     * @Description 회원 식별자와 필터/기간/커서 조건으로 포인트 이력을 페이징 조회합니다.
     */
    List<UserPointHistory> findByUserId(Long userId, UserPointHistoryFilter filter,
                                        LocalDate startDate, LocalDate endDate,
                                        Long cursor, int pageSize);

    /**
     * @param userId    회원 식별자
     * @param filter    포인트 이력 필터
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @return 조건에 맞는 전체 포인트 이력 건수
     * @Date 2026-03-21
     * @Author 성효빈
     * @Description 조건에 맞는 포인트 이력 전체 건수를 조회합니다.
     */
    long countByUserId(Long userId, UserPointHistoryFilter filter, LocalDate startDate, LocalDate endDate);

    /**
     * @param userId     회원 식별자
     * @param sourceType 포인트 출처 유형
     * @param sourceId   포인트 출처 식별자
     * @return 미반영 적립 예정 포인트 이력 목록 {@link UserPointHistory}
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 미반영(isReflected = false) 적립 예정 포인트 이력을 출처 기준으로 조회합니다.
     */
    List<UserPointHistory> findUnreflectedByUserIdAndSource(Long userId, UserPointSourceType sourceType, Long sourceId);
}
