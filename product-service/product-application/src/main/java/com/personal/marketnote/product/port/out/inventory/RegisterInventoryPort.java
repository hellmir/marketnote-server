package com.personal.marketnote.product.port.out.inventory;

/**
 * 재고 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-06
 * @Description 재고 등록 기능을 제공합니다.
 */
public interface RegisterInventoryPort {
    /**
     * @param productId     상품 ID
     * @param pricePolicyId 가격 정책 ID
     * @return void
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 상품 ID와 가격 정책 ID로 재고를 등록합니다.
     */
    void registerInventory(Long productId, Long pricePolicyId);
}
