package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.exception.InventoryNotFoundException;

import java.util.Set;

public interface UpdateInventoryPort {
    /**
     * @param inventories 재고 도메인 목록
     * @throws InventoryNotFoundException 재고를 찾을 수 없는 경우
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 재고 정보를 업데이트합니다.
     */
    void update(Set<Inventory> inventories) throws InventoryNotFoundException;
}
