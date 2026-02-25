package com.personal.marketnote.community.port.in.usecase.review;

/**
 * 리뷰 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-12
 * @Description 리뷰 삭제 기능을 제공합니다.
 */
public interface DeleteReviewUseCase {
    /**
     * @param id         리뷰 ID
     * @param reviewerId 리뷰 작성자 ID
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰를 삭제합니다.
     */
    void deleteReview(Long id, Long reviewerId);
}
