package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.result.order.GetOrderStatusHistoryResult;

/**
 * 주문 상태 이력 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 관리자가 특정 주문의 상태 변경 이력을 조회합니다.
 */
public interface GetOrderStatusHistoryUseCase {
    GetOrderStatusHistoryResult getOrderStatusHistory(Long orderId);
}
