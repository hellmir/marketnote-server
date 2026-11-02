package com.personal.marketnote.community.port.out.review;

import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.Review;

/**
 * 리뷰 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-10
 * @Description 리뷰 수정 기능을 제공합니다.
 */
public interface UpdateReviewPort {
    /**
     * @param review 리뷰
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰를 수정합니다.
     */
    void update(Review review);

    /**
     * @param productReviewAggregate 상품 리뷰 집계
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 상품 리뷰 집계를 수정합니다.
     */
    void update(ProductReviewAggregate productReviewAggregate);
}
