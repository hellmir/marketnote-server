package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.domain.user.LoginHistorySortProperty;
import com.personal.marketnote.user.port.in.result.GetLoginHistoryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 로그인 이력 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-30
 * @Description 로그인 이력 조회 기능을 제공합니다.
 */
public interface GetLoginHistoryUseCase {
    /**
     * @param userId        회원 ID
     * @param pageSize      페이지 크기
     * @param pageNumber    페이지 번호
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @return 로그인 이력 목록 {@link Page<GetLoginHistoryResult>}
     * @Date 2025-12-30
     * @Author 성효빈
     * @Description 회원의 로그인 이력 목록을 조회합니다.
     */
    Page<GetLoginHistoryResult> getLoginHistories(
            Long userId,
            int pageSize,
            int pageNumber,
            Sort.Direction sortDirection,
            LoginHistorySortProperty sortProperty
    );
}
