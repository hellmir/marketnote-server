package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;

/**
 * 주문 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 주문 등록 기능을 제공합니다.
 */
public interface RegisterOrderUseCase {
    /**
     * @param command 주문 등록 커맨드
     * @return 주문 등록 결과 {@link RegisterOrderResult}
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 주문을 등록합니다.
     */
    RegisterOrderResult registerOrder(RegisterOrderCommand command);
}
