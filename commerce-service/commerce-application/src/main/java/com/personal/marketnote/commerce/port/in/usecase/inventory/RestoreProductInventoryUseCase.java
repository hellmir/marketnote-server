package com.personal.marketnote.commerce.port.in.usecase.inventory;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.List;

public interface RestoreProductInventoryUseCase {
    /**
     * @param orderProducts 주문 상품 목록
     * @param reason        재고 복원 이유
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 주문 상품 재고를 복원합니다.
     */
    void restore(List<OrderProduct> orderProducts, String reason);
}
