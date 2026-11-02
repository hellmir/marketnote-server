package com.personal.marketnote.community.port.in.usecase.review;

import com.personal.marketnote.community.port.in.command.review.UpdateReviewCommand;
import com.personal.marketnote.community.port.in.result.review.UpdateReviewResult;

/**
 * 리뷰 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-12
 * @Description 리뷰 수정 기능을 제공합니다.
 */
public interface UpdateReviewUseCase {
    /**
     * @param command 리뷰 수정 커맨드
     * @return 리뷰 수정 결과 {@link UpdateReviewResult}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰를 수정합니다.
     */
    UpdateReviewResult updateReview(UpdateReviewCommand command);
}
