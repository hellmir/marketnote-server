package com.personal.marketnote.community.port.in.usecase.review;

import com.personal.marketnote.community.port.in.command.review.UpdateReviewCommand;

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
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰를 수정합니다.
     */
    void updateReview(UpdateReviewCommand command);
}
