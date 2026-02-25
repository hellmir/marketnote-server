package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;

import java.util.Set;

/**
 * 재고 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-06
 * @Description 재고 조회 관련 기능을 제공합니다.
 */
public interface FindInventoryPort {
    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @return 재고 도메인 목록 {@link Set}
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 가격 정책 ID 목록으로 재고를 조회합니다.
     */
    Set<Inventory> findByPricePolicyIds(Set<Long> pricePolicyIds);

    /**
     * @param productIds 상품 ID 목록
     * @return 재고 도메인 목록 {@link Set}
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 상품 ID 목록으로 재고를 조회합니다.
     */
    Set<Inventory> findByProductIds(Set<Long> productIds);

    /**
     * @param pricePolicyId 가격 정책 ID
     * @return 재고 존재 여부 {@link boolean}
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 가격 정책 ID에 해당하는 재고가 존재하는지 확인합니다.
     */
    boolean existsByPricePolicyId(Long pricePolicyId);
}
