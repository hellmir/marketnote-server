package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetOrderStatusHistoryResult;

import java.util.List;

/**
 * 주문 상태 이력 조회 응답 DTO
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 주문의 전체 상태 변경 이력 조회 응답 DTO입니다.
 */
public record GetOrderStatusHistoryResponse(
        Long orderId,
        List<OrderStatusHistoryItemResponse> statusHistory
) {
    public static GetOrderStatusHistoryResponse from(GetOrderStatusHistoryResult result) {
        List<OrderStatusHistoryItemResponse> items = result.statusHistory().stream()
                .map(OrderStatusHistoryItemResponse::from)
                .toList();
        return new GetOrderStatusHistoryResponse(result.orderId(), items);
    }
}
