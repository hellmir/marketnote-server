package com.personal.marketnote.product.port.out.review;

import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;

import java.util.List;
import java.util.Map;

/**
 * 상품 리뷰 집계 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 상품 리뷰 집계 조회 기능을 제공합니다.
 */
public interface FindProductReviewAggregatesPort {
    /**
     * @param productIds 상품 ID 목록
     * @return 상품 ID별 리뷰 집계 결과 맵 {@link Map<Long, ProductReviewAggregateResult>}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 상품 ID 목록으로 리뷰 집계 정보를 조회합니다.
     */
    Map<Long, ProductReviewAggregateResult> findByProductIds(List<Long> productIds);
}
