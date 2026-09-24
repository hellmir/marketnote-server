package com.personal.marketnote.community.port.in.usecase.review;

import com.personal.marketnote.community.port.in.result.review.GetReviewKeyResult;

/**
 * 리뷰 reviewKey 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 리뷰의 reviewKey를 조회합니다. 작성자 본인만 조회할 수 있습니다.
 */
public interface GetReviewKeyUseCase {
    /**
     * @param reviewId 리뷰 ID
     * @param userId   요청자 회원 ID
     * @return reviewKey 조회 결과 {@link GetReviewKeyResult}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 리뷰의 reviewKey를 조회합니다. 작성자 본인만 조회 가능합니다.
     */
    GetReviewKeyResult getReviewKey(Long reviewId, Long userId);
}
