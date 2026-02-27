package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

/**
 * 주문에 대한 접근 권한이 없는 경우 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-02-27
 * @Description 타 사용자의 주문에 접근하려 할 때 발생합니다.
 */
public class UnauthorizedOrderAccessException extends BusinessSecurityException {
    private static final String UNAUTHORIZED_ORDER_ACCESS_EXCEPTION_MESSAGE
            = "ERR_ORDER_AUTH_01::해당 주문에 대한 접근 권한이 없습니다.";

    public UnauthorizedOrderAccessException() {
        super(UNAUTHORIZED_ORDER_ACCESS_EXCEPTION_MESSAGE);
    }
}
