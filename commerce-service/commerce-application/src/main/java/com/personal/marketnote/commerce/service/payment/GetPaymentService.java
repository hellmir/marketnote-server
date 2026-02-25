package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.result.payment.GetPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.GetPaymentUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = READ_COMMITTED)
public class GetPaymentService implements GetPaymentUseCase {
    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;

    @Override
    public GetPaymentResult getPayment(Long buyerId, String orderKey) {
        UUID orderKeyUuid = UUID.fromString(orderKey);

        Payment payment = findPaymentPort.findByOrderKey(orderKeyUuid)
                .orElseThrow(() -> new PaymentNotFoundException(orderKey));

        verifyOrderOwnership(payment.getOrderId(), buyerId);

        PspPaymentEvent event = findPspPaymentEventPort.findByOrderKey(orderKey)
                .orElse(null);

        return GetPaymentResult.of(payment, event);
    }

    private void verifyOrderOwnership(Long orderId, Long buyerId) {
        Order order = findOrderPort.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("주문 정보를 찾을 수 없습니다: " + orderId));
        if (!order.getBuyerId().equals(buyerId)) {
            throw new IllegalStateException("해당 주문에 대한 권한이 없습니다");
        }
    }
}
