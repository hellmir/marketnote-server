package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.PartialCancelOrderCommand;

/**
 * 구매자의 주문 부분 취소 요청 기능을 제공합니다.
 *
 * @Author 성효빈
 * @Date 2026-10-11
 * @Description 일부 주문 상품에 대한 부분 취소를 처리합니다.
 */
public interface PartialCancelOrderUseCase {
    void partialCancelOrder(PartialCancelOrderCommand command);
}
