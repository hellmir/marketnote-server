package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistories;

public interface SaveInventoryDeductionHistoryPort {
    /**
     * @param inventoryDeductionHistories 재고 차감 이력 목록
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 재고 차감 이력을 저장합니다.
     */
    void save(InventoryDeductionHistories inventoryDeductionHistories);
}
