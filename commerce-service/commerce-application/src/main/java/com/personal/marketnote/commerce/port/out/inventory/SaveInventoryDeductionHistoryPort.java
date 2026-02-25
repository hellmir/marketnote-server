package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistories;

/**
 * 재고 차감 이력 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 재고 차감 이력 저장 기능을 제공합니다.
 */
public interface SaveInventoryDeductionHistoryPort {
    /**
     * @param inventoryDeductionHistories 재고 차감 이력 목록
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 재고 차감 이력을 저장합니다.
     */
    void save(InventoryDeductionHistories inventoryDeductionHistories);
}
