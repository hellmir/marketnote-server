package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;

/**
 * 결제 준비 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description 결제 준비 기능을 제공합니다.
 */
public interface ReadyPaymentUseCase {
    /**
     * @param command 결제 준비 커맨드
     * @return 결제 준비 결과 {@link ReadyPaymentResult}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사에 거래를 등록하고 결제를 준비합니다.
     */
    ReadyPaymentResult ready(ReadyPaymentCommand command);
}
