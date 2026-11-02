package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.RequestReturnCommand;

/**
 * 반품 요청 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 구매자의 반품 요청 기능을 제공합니다.
 */
public interface RequestReturnUseCase {
    /**
     * @param command 반품 요청 커맨드
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 구매자의 주문을 반품 요청 상태로 변경합니다.
     */
    void requestReturn(RequestReturnCommand command);
}
