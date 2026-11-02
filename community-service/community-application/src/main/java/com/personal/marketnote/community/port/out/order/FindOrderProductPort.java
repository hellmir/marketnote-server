package com.personal.marketnote.community.port.out.order;

import java.util.Optional;

/**
 * 주문 상품 조회 Out Port
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 커머스 서비스에서 주문 상품의 구매 시점 단가를 조회합니다.
 */
public interface FindOrderProductPort {
    /**
     * @param orderId       주문 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 구매 시점 단가 {@link Optional}
     */
    Optional<Long> findUnitAmountByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId);
}
