package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;

import java.util.Set;

public interface SaveCacheStockPort {
    /**
     * @param inventories 재고 도메인 목록
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 재고 목록을 캐시에 저장합니다.
     */
    void save(Set<Inventory> inventories);

    /**
     * @param pricePolicyId 가격 정책 ID
     * @param stock         재고 수량
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 단일 가격 정책의 재고 수량을 캐시에 저장합니다.
     */
    void save(Long pricePolicyId, int stock);
}
