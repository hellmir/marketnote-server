package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentStockCommand;

/**
 * 파스토 재고 동기화 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-07
 * @Description 파스토 재고 동기화 기능을 제공합니다.
 */
public interface SyncFulfillmentStockUseCase {
    /**
     * @param command 파스토 재고 동기화 커맨드
     * @Date 2026-02-07
     * @Author 성효빈
     * @Description 파스토 상품 재고를 커머스 재고와 동기화합니다.
     */
    void sync(SyncFulfillmentStockCommand command);
}
