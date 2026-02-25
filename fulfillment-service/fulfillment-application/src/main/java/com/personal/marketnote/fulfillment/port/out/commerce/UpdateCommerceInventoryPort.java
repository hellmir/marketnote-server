package com.personal.marketnote.fulfillment.port.out.commerce;

/**
 * 커머스 재고 업데이트 포트
 *
 * @Author 성효빈
 * @Date 2026-02-07
 * @Description 커머스 재고 업데이트 기능을 제공합니다.
 */
public interface UpdateCommerceInventoryPort {

    /**
     * @param command 커머스 재고 업데이트 커맨드
     * @Date 2026-02-07
     * @Author 성효빈
     * @Description 커머스 재고를 업데이트합니다.
     */
    void updateInventories(UpdateCommerceInventoryCommand command);
}
