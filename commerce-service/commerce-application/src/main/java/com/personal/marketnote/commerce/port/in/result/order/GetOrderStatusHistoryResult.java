package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;

import java.util.List;

/**
 * 주문 상태 이력 조회 결과
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 주문의 전체 상태 변경 이력 목록을 감싸는 결과 record입니다.
 */
public record GetOrderStatusHistoryResult(
        Long orderId,
        List<OrderStatusHistoryItem> statusHistory
) {
    public static GetOrderStatusHistoryResult from(Long orderId, List<OrderStatusHistory> histories) {
        List<OrderStatusHistoryItem> items = histories.stream()
                .map(OrderStatusHistoryItem::from)
                .toList();
        return new GetOrderStatusHistoryResult(orderId, items);
    }
}
