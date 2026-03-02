package com.personal.marketnote.commerce.port.out.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;

import java.util.List;

/**
 * 주문 상태 이력 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 주문 상태 변경 이력을 조회하는 기능을 제공합니다.
 */
public interface FindOrderStatusHistoryPort {
    /**
     * @param orderId 주문 ID
     * @return 주문 상태 이력 목록 {@link List}
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 주문 ID로 전체 상태 변경 이력을 생성시간 오름차순으로 조회합니다.
     */
    List<OrderStatusHistory> findAllByOrderId(Long orderId);
}
