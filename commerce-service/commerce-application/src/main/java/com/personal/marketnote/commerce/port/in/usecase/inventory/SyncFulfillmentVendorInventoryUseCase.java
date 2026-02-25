package com.personal.marketnote.commerce.port.in.usecase.inventory;

import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;

/**
 * 풀필먼트 벤더 재고 동기화 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-07
 * @Description 풀필먼트 벤더 재고 동기화 기능을 제공합니다.
 */
public interface SyncFulfillmentVendorInventoryUseCase {
    /**
     * @param command 풀필먼트 벤더 재고 동기화 커맨드
     * @Date 2026-02-07
     * @Author 성효빈
     * @Description 풀필먼트 벤더 재고 정보를 동기화합니다.
     */
    void syncInventories(SyncFulfillmentVendorInventoryCommand command);
}
