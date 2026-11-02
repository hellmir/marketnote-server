package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;

import java.util.Set;

/**
 * 재고 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-06
 * @Description 재고 저장 관련 기능을 제공합니다.
 */
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
