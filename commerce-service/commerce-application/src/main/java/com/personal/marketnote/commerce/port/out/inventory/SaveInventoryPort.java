package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;

import java.util.Set;

public interface SaveInventoryPort {
    /**
     * @param inventory 재고 도메인
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 단일 재고 정보를 저장합니다.
     */
    void save(Inventory inventory);

    /**
     * @param inventories 재고 도메인 목록
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 재고 목록을 일괄 저장합니다.
     */
    void save(Set<Inventory> inventories);
}
