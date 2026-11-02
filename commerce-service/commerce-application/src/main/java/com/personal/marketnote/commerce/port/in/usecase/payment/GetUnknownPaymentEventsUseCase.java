package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.result.payment.GetUnknownPaymentEventsResult;

import java.util.List;

/**
 * UNKNOWN 상태 결제 이벤트 조회 유스케이스
 */
public interface GetUnknownPaymentEventsUseCase {
    List<GetUnknownPaymentEventsResult> getUnknownPaymentEvents();
}
