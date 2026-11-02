package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.domain.user.UserStatusHistorySortProperty;
import com.personal.marketnote.user.port.in.result.GetUserStatusHistoryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 회원 상태 변경 이력 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 관리자가 회원의 상태 변경 이력을 조회하는 기능을 제공합니다.
 */
public interface GetUserStatusHistoryUseCase {
    /**
     * @param userId        회원 ID
     * @param pageSize      페이지 크기
     * @param pageNumber    페이지 번호 (0-indexed)
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @return 상태 변경 이력 조회 결과 페이지 {@link Page}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 특정 회원의 상태 변경 내역을 페이징 조회합니다.
     */
    Page<GetUserStatusHistoryResult> getUserStatusHistories(
            Long userId,
            int pageSize,
            int pageNumber,
            Sort.Direction sortDirection,
            UserStatusHistorySortProperty sortProperty
    );
}
