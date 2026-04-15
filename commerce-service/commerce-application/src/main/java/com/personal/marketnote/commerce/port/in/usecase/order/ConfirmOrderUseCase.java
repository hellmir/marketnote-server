package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.ConfirmOrderCommand;

/**
 * 구매 확정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 구매자의 구매 확정 기능을 제공합니다.
 */
public interface ConfirmOrderUseCase {
    /**
     * @param command 구매 확정 커맨드
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 구매자의 주문을 구매 확정 상태로 변경합니다.
     */
    void confirmOrder(ConfirmOrderCommand command);
}
