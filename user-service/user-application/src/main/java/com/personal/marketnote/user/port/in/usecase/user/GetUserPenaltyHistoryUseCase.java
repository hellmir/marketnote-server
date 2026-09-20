package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.domain.user.UserPenaltyHistorySortProperty;
import com.personal.marketnote.user.port.in.result.GetUserPenaltyHistoryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 회원 패널티 이력 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 관리자가 회원의 패널티 이력을 조회하는 기능을 제공합니다.
 */
public interface GetUserPenaltyHistoryUseCase {
    /**
     * @param userId        회원 ID
     * @param pageSize      페이지 크기
     * @param pageNumber    페이지 번호 (0-indexed)
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @return 패널티 이력 조회 결과 페이지 {@link Page}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 특정 회원의 패널티 부과 내역을 페이징 조회합니다.
     */
    Page<GetUserPenaltyHistoryResult> getUserPenaltyHistories(
            Long userId,
            int pageSize,
            int pageNumber,
            Sort.Direction sortDirection,
            UserPenaltyHistorySortProperty sortProperty
    );
}
