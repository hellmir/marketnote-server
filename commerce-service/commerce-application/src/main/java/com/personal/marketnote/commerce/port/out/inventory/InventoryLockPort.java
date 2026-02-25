package com.personal.marketnote.commerce.port.out.inventory;

import java.util.Set;

/**
 * 재고 락 포트
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 재고 동시성 제어를 위한 락 기능을 제공합니다.
 */
public interface InventoryLockPort {
    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param task           락 획득 후 실행할 작업
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 가격 정책 ID 목록에 대한 분산 락을 획득한 후 작업을 실행합니다.
     */
    void executeWithLock(Set<Long> pricePolicyIds, Runnable task);
}
