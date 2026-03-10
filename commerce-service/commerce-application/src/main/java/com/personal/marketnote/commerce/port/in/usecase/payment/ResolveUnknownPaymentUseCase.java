package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.command.payment.ResolveUnknownPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ResolveUnknownPaymentResult;

/**
 * UNKNOWN 상태 결제 이벤트 수동 해소 유스케이스
 */
public interface ResolveUnknownPaymentUseCase {
    ResolveUnknownPaymentResult resolve(ResolveUnknownPaymentCommand command);
}
