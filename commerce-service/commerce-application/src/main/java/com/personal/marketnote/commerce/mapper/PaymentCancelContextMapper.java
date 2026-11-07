package com.personal.marketnote.commerce.mapper;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.service.payment.PaymentCancelComputation;
import com.personal.marketnote.commerce.service.payment.PaymentCancelContext;

/**
 * {@link PaymentCancelContext} 생성 매퍼
 *
 * <p>Service 레이어 본문에서 {@code PaymentCancelContext.builder()} 체인을 직접 호출하지 않도록
 * 빌더 체인을 이 매퍼로 개입화한다.</p>
 *
 * <p>원본 도메인/Command 네 개와 Helper에서 계산된 중간 값({@link PaymentCancelComputation})을
 * 받아 Context를 조립한다.</p>
 */
public final class PaymentCancelContextMapper {
    private PaymentCancelContextMapper() {
    }

    public static PaymentCancelContext from(
            Payment payment,
            PspPaymentEvent event,
            Order order,
            CancelPaymentCommand command,
            PaymentCancelComputation computation
    ) {
        return PaymentCancelContext.builder()
                .payment(payment)
                .event(event)
                .order(order)
                .cancelAmount(computation.cancelAmount())
                .adjustedCancelAmount(computation.adjustedCancelAmount())
                .remainAmount(computation.remainAmount())
                .alreadyRefunded(computation.alreadyRefunded())
                .shippingFeeDeduction(computation.shippingFeeDeduction())
                .isFullCancel(computation.isFullCancel())
                .cancelReason(command.cancelReason())
                .adjustedCancelReason(computation.adjustedCancelReason())
                .cancelId(computation.cancelId())
                .transactionNumber(computation.transactionNumber())
                .modType(computation.modType())
                .originalCommand(command)
                .build();
    }
}
