package com.personal.marketnote.commerce.port.in.usecase.inventory;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.List;

public interface ReleaseInventoryReservationUseCase {
    /**
     * @param orderProducts 주문 상품 목록
     * @param orderId       주문 ID
     * @param reason        해소 이유
     * @Description SAGA 보상 시 예약 해소. 예약 레코드가 존재하면 releaseReservation (reserved 감소 + 레코드 삭제),
     * 이미 confirmReservation 완료(레코드 부재)된 경우 restore (stock 복원)로 fallback.
     */
    void release(List<OrderProduct> orderProducts, Long orderId, String reason);
}
