package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;

/**
 * 결제 취소 트랜잭션 간 데이터 전달 컨텍스트
 *
 * <p>TX-1(prepareCancel)에서 조회/검증된 도메인 객체와 계산 결과를 담아
 * TX-2A(commitPspEventCancel) / TX-2B(commitPaymentAndPostProcess)에 전달한다.</p>
 *
 * @param payment                결제 도메인
 * @param event                  PSP 결제 이벤트 도메인
 * @param order                  주문 도메인
 * @param cancelAmount           사용자가 요청한 원래 취소 금액(배송비 차감 전)
 * @param adjustedCancelAmount   배송비 차감을 반영한 실제 PG 취소 금액
 * @param remainAmount           부분취소 잔여 금액 (PG 전달용)
 * @param alreadyRefunded        이미 환불된 금액 (이번 취소 전 기준)
 * @param shippingFeeDeduction   배송비 차감액
 * @param isFullCancel           전체 취소 여부
 * @param cancelReason           원래 취소 사유
 * @param adjustedCancelReason   배송비 차감 안내가 덧붙은 취소 사유
 * @param cancelId               부분 취소 식별자 (전체 취소면 null)
 * @param transactionNumber      PG 거래 번호
 * @param modType                PG 취소 타입 (STSC/STPC)
 * @param originalCommand        원본 CancelPaymentCommand
 */
public record PaymentCancelContext(
        Payment payment,
        PspPaymentEvent event,
        Order order,
        Long cancelAmount,
        Long adjustedCancelAmount,
        Long remainAmount,
        Long alreadyRefunded,
        Long shippingFeeDeduction,
        boolean isFullCancel,
        String cancelReason,
        String adjustedCancelReason,
        String cancelId,
        String transactionNumber,
        String modType,
        CancelPaymentCommand originalCommand
) {
    public static PaymentCancelContext of(
            Payment payment,
            PspPaymentEvent event,
            Order order,
            Long cancelAmount,
            Long adjustedCancelAmount,
            Long remainAmount,
            Long alreadyRefunded,
            Long shippingFeeDeduction,
            boolean isFullCancel,
            String cancelReason,
            String adjustedCancelReason,
            String cancelId,
            String transactionNumber,
            String modType,
            CancelPaymentCommand originalCommand
    ) {
        return new PaymentCancelContext(
                payment,
                event,
                order,
                cancelAmount,
                adjustedCancelAmount,
                remainAmount,
                alreadyRefunded,
                shippingFeeDeduction,
                isFullCancel,
                cancelReason,
                adjustedCancelReason,
                cancelId,
                transactionNumber,
                modType,
                originalCommand
        );
    }
}
