package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;

import java.time.LocalDateTime;

/**
 * 주문 상태 이력 항목
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 개별 주문 상태 변경 이력 항목을 표현합니다.
 */
public record OrderStatusHistoryItem(
        Long id,
        OrderStatus orderStatus,
        String orderStatusDescription,
        OrderStatusReasonCategory reasonCategory,
        String reason,
        LocalDateTime createdAt
) {
    public static OrderStatusHistoryItem from(OrderStatusHistory history) {
        return new OrderStatusHistoryItem(
                history.getId(),
                history.getOrderStatus(),
                history.getOrderStatus().getDescription(),
                history.getReasonCategory(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
