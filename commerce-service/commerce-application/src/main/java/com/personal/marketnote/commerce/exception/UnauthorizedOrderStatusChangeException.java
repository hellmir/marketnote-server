package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

/**
 * 주문 상태 변경 권한이 없는 경우 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-02-27
 * @Description 구매자가 허용되지 않은 상태로 주문 상태를 변경하려 할 때 발생합니다.
 */
public class UnauthorizedOrderStatusChangeException extends BusinessSecurityException {
    private static final String UNAUTHORIZED_ORDER_STATUS_CHANGE_EXCEPTION_MESSAGE
            = "ERR_ORDER_AUTH_02::해당 상태로 변경할 권한이 없습니다.";

    public UnauthorizedOrderStatusChangeException() {
        super(UNAUTHORIZED_ORDER_STATUS_CHANGE_EXCEPTION_MESSAGE);
    }
}
