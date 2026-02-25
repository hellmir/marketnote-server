package com.personal.marketnote.community.port.in.usecase.review;

import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.result.review.RegisterReviewResult;

/**
 * 리뷰 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-09
 * @Description 리뷰 등록 기능을 제공합니다.
 */
public interface RegisterReviewUseCase {
    /**
     * @param command 리뷰 등록 커맨드
     * @return 리뷰 등록 결과 {@link RegisterReviewResult}
     * @Date 2026-01-09
     * @Author 성효빈
     * @Description 리뷰를 등록합니다.
     */
    RegisterReviewResult registerReview(RegisterReviewCommand command);
}
