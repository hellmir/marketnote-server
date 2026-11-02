package com.personal.marketnote.product.port.out.inventory;

/**
 * 캐시 재고 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 캐시 재고 저장 기능을 제공합니다.
 */
public interface SaveCacheStockPort {
    /**
     * @param pricePolicyId 가격 정책 ID
     * @param stock         재고 수량
     * @return void
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 가격 정책 ID에 해당하는 캐시 재고를 저장합니다.
     */
    void save(Long pricePolicyId, int stock);
}
