package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;

/**
 * 결제 승인 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description 결제 승인 기능을 제공합니다.
 */
public interface ApprovePaymentUseCase {
    /**
     * @param command 결제 승인 커맨드
     * @return 결제 승인 결과 {@link ApprovePaymentResult}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사에 결제 승인을 요청합니다.
     */
    ApprovePaymentResult approve(ApprovePaymentCommand command);
}
