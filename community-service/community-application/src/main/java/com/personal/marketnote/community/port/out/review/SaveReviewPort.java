package com.personal.marketnote.community.port.out.review;

import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewVersionHistory;

/**
 * 리뷰 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-09
 * @Description 리뷰 저장 기능을 제공합니다.
 */
public interface SaveReviewPort {
    /**
     * @param review 리뷰
     * @return 저장된 리뷰 {@link Review}
     * @Date 2026-01-09
     * @Author 성효빈
     * @Description 리뷰를 저장합니다.
     */
    Review save(Review review);

    /**
     * @param productReviewAggregate 상품 리뷰 집계
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 상품 리뷰 집계를 저장합니다.
     */
    void saveAggregate(ProductReviewAggregate productReviewAggregate);

    /**
     * @param reviewVersionHistory 리뷰 버전 이력
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰 버전 이력을 저장합니다.
     */
    void saveVersionHistory(ReviewVersionHistory reviewVersionHistory);
}
