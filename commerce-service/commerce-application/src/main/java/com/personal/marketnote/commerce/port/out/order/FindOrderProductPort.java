package com.personal.marketnote.commerce.port.out.order;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.Optional;

/**
 * 주문 상품 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-12
 * @Description 주문 상품 조회 관련 기능을 제공합니다.
 */
public interface FindOrderProductPort {
    /**
     * @param orderId       주문 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 주문 상품 {@link OrderProduct}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 주문 ID와 가격 정책 ID로 주문 상품을 조회합니다.
     */
    Optional<OrderProduct> findByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId);
}
