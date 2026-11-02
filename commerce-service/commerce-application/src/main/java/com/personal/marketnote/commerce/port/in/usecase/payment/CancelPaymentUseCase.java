package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;

/**
 * 결제 취소 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description 결제 취소 기능을 제공합니다.
 */
public interface CancelPaymentUseCase {
    /**
     * @param command 결제 취소 커맨드
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사에 결제 취소를 요청합니다.
     */
    void cancel(CancelPaymentCommand command);
}
