package com.personal.marketnote.community.port.out.order;

/**
 * 주문 상품 리뷰 상태 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-12
 * @Description 주문 상품의 리뷰 작성 여부를 수정합니다.
 */
public interface UpdateOrderProductReviewStatusPort {
    /**
     * @param orderId       주문 ID
     * @param pricePolicyId 가격 정책 ID
     * @param isReviewed    리뷰 작성 여부
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 주문 상품의 리뷰 작성 여부를 업데이트합니다.
     */
    void update(Long orderId, Long pricePolicyId, boolean isReviewed);
}
