package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.ReadyPaymentUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.SavePspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorResult;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final SavePspPaymentEventPort savePspPaymentEventPort;
    private final PaymentVendorPort paymentVendorPort;

    @Override
    public ReadyPaymentResult ready(ReadyPaymentCommand command) {
        UUID orderKey = UUID.fromString(command.orderKey());

        Payment payment = findPaymentPort.findByOrderKey(orderKey)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        verifyOrderForPayment(payment.getOrderId(), command.buyerId());
        verifyNoDuplicatePaymentReady(command.orderKey());
        saveReadyEvent(payment, command.payMethod());

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

    private void verifyOrderForPayment(Long orderId, Long buyerId) {
        Order order = findOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.isBuyer(buyerId)) {
            log.warn("거래 등록 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}", orderId, order.getBuyerId(), buyerId);
            throw new UnauthorizedOrderAccessException();
        }

        if (!order.isPaymentPending()) {
            log.warn("결제 불가 주문 상태에서 거래 등록 시도 - orderId: {}, 주문 상태: {}", orderId, order.getOrderStatus());
            throw new InvalidOrderStatusForPaymentException(order.getOrderStatus());
        }
    }

    private void verifyNoDuplicatePaymentReady(String orderKey) {
        findPspPaymentEventPort.findByOrderKey(orderKey)
                .filter(PspPaymentEvent::isUnresolved)
                .ifPresent(event -> {
                    log.warn("중복 거래 등록 시도 - orderKey: {}, 기존 이벤트 상태: {}", orderKey, event.getPoStatus());
                    throw new DuplicatePaymentReadyException(orderKey);
                });
    }

    private void saveReadyEvent(Payment payment, String payMethod) {
        String vendorSiteCd = paymentVendorPort.getVendorSiteCd();
        PspPaymentEvent readyEvent = PspPaymentEvent.createReady(payment, vendorSiteCd, payMethod);

        try {
            savePspPaymentEventPort.save(readyEvent);
        } catch (DataIntegrityViolationException e) {
            log.warn("Race Condition으로 인한 중복 거래 등록 감지 - orderKey: {}", payment.getOrderKey(), e);
            throw new DuplicatePaymentReadyException(payment.getOrderKey().toString());
        }
    }
}
