package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;

/**
 * 결제 승인 트랜잭션 간 데이터 전달 컨텍스트
 *
 * @param payment 결제 정보
 * @param event PSP 결제 이벤트
 * @param orderId 주문 ID
 * @param orderKey 주문 키
 * @param paymentAmount 결제 금액
 * @param pgShopKey PG 가맹점 키
 * @param method 결제 수단
 */
public record PaymentApprovalContext(
        Payment payment,
        PspPaymentEvent event,
        Long orderId,
        String orderKey,
        Long paymentAmount,
        String pgShopKey,
        String method
) {
    public static PaymentApprovalContext of(Payment payment, PspPaymentEvent event) {
        return new PaymentApprovalContext(
                payment,
                event,
                payment.getOrderId(),
                payment.getOrderKey().toString(),
                payment.getPaymentAmount(),
                event.getPgShopKey(),
                event.getMethod()
        );
    }
}
