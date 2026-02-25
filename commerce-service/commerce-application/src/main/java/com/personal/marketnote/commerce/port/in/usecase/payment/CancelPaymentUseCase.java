package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;

public interface CancelPaymentUseCase {
    /**
     * @param command 결제 취소 커맨드
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사에 결제 취소를 요청합니다.
     */
    void cancel(CancelPaymentCommand command);
}
