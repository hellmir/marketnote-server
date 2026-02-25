package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.result.payment.GetPaymentResult;

public interface GetPaymentUseCase {
    /**
     * @param buyerId  구매자 ID
     * @param orderKey 주문 키
     * @return 결제 조회 결과 {@link GetPaymentResult}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 구매자 ID와 주문 키로 결제 정보를 조회합니다.
     */
    GetPaymentResult getPayment(Long buyerId, String orderKey);
}
