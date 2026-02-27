package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.DuplicatePaymentReadyException;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusForPaymentException;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentApprovalException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.ReadyPaymentUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorResult;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class ReadyPaymentService implements ReadyPaymentUseCase {
    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final PaymentVendorPort paymentVendorPort;

    @Override
    public ReadyPaymentResult ready(ReadyPaymentCommand command) {
        UUID orderKey = UUID.fromString(command.orderKey());

        Payment payment = findPaymentPort.findByOrderKey(orderKey)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        verifyOrderStatusForPayment(payment.getOrderId());
        verifyNoDuplicatePaymentReady(command.orderKey());

        TradeRegisterVendorCommand vendorCommand = TradeRegisterVendorCommand.builder()
                .orderKey(payment.getOrderKey().toString())
                .goodMny(String.valueOf(payment.getPaymentAmount()))
                .payMethod(command.payMethod())
                .goodName(command.goodName())
                .build();

        TradeRegisterVendorResult vendorResult = paymentVendorPort.registerTrade(vendorCommand);

        if (!vendorResult.isSuccess()) {
            throw PaymentApprovalException.kcpTradeRegisterFailed(vendorResult.resCd(), vendorResult.resMsg());
        }

        return ReadyPaymentResult.builder()
                .orderKey(payment.getOrderKey().toString())
                .approvalKey(vendorResult.approvalKey())
                .payUrl(vendorResult.payUrl())
                .traceNo(vendorResult.traceNo())
                .build();
    }

    private void verifyOrderStatusForPayment(Long orderId) {
        Order order = findOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.isPaymentPending()) {
            log.warn("결제 불가 주문 상태에서 거래 등록 시도 - orderId: {}, 주문 상태: {}",
                    orderId, order.getOrderStatus());
            throw new InvalidOrderStatusForPaymentException(order.getOrderStatus());
        }
    }

    private void verifyNoDuplicatePaymentReady(String orderKey) {
        findPspPaymentEventPort.findByOrderKey(orderKey)
                .filter(PspPaymentEvent::isActiveEvent)
                .ifPresent(event -> {
                    log.warn("중복 거래 등록 시도 - orderKey: {}, 기존 이벤트 상태: {}",
                            orderKey, event.getPoStatus());
                    throw new DuplicatePaymentReadyException(orderKey);
                });
    }
}
