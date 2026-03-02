package com.personal.marketnote.commerce.port.out.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;

/**
 * 주문 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 주문 수정 기능을 제공합니다.
 */
public interface UpdateOrderPort {
    /**
     * @param order              주문 도메인
     * @param orderStatusHistory 주문 상태 변경 이력
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 주문 정보를 업데이트하고 상태 변경 이력을 저장합니다.
     */
    void update(Order order, OrderStatusHistory orderStatusHistory);

    /**
     * @param order 주문 도메인
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 주문의 송장 정보(택배사, 송장번호)를 업데이트합니다.
     */
    void updateTrackingInfo(Order order);
}
