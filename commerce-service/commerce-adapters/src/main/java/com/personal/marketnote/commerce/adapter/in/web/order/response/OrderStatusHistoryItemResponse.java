package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.port.in.result.order.OrderStatusHistoryItem;

import java.time.LocalDateTime;

/**
 * 주문 상태 이력 항목 응답 DTO
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 개별 주문 상태 변경 이력 항목의 응답 DTO입니다.
 */
public record OrderStatusHistoryItemResponse(
        Long id,
        OrderStatus orderStatus,
        String orderStatusDescription,
        OrderStatusReasonCategory reasonCategory,
        String reason,
        LocalDateTime createdAt
) {
    public static OrderStatusHistoryItemResponse from(OrderStatusHistoryItem item) {
        return new OrderStatusHistoryItemResponse(
                item.id(),
                item.orderStatus(),
                item.orderStatusDescription(),
                item.reasonCategory(),
                item.reason(),
                item.createdAt()
        );
    }
}
