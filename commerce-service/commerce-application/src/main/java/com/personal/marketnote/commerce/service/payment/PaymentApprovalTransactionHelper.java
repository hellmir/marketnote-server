package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentApprovalInfo;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePaymentPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * 결제 승인 트랜잭션 헬퍼
 *
 * <p>결제 승인 플로우의 각 단계를 독립 트랜잭션(REQUIRES_NEW)으로 분리하여
 * 실패/UNKNOWN 상태 반영이 RuntimeException throw에 의해 롤백되지 않도록 보장한다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentApprovalTransactionHelper {
    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    /**
     * TX-1: 검증 + EXECUTING 상태 커밋
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public PaymentApprovalContext prepareExecution(ApprovePaymentCommand command) {
        UUID orderKey = UUID.fromString(command.orderKey());
        Payment payment = findPaymentPort.findByOrderKey(orderKey)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        Order order = findVerifiedOrder(payment.getOrderId(), command.buyerId());
        verifyPaymentAmount(order, payment);

        PspPaymentEvent event = findPspPaymentEventPort.findByOrderKey(command.orderKey())
                .orElseThrow(() -> new PaymentEventNotFoundException(command.orderKey()));
        event.startExecution();
        updatePspPaymentEventPort.update(event);

        return PaymentApprovalContext.of(payment, event);
    }

    /**
     * TX-2: 성공 상태 커밋 (COMPLETE + PAID + 분개)
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public ApprovePaymentResult commitSuccess(
            PaymentApprovalContext context, PaymentApprovalVendorResult vendorResult, Short installment
    ) {
        Payment payment = context.payment();
        PspPaymentEvent event = context.event();

        payment.markAsSuccess(vendorResult.tno());
        updatePaymentPort.update(payment);

        PaymentApprovalInfo approvalInfo = PaymentApprovalInfo.builder()
                .pgPaymentKey(vendorResult.tno())
                .method(vendorResult.payMethod())
                .cardNumber(vendorResult.cardNo())
                .approvalNumber(vendorResult.appNo())
                .installment(installment)
                .issueCompanyCode(vendorResult.cardCd())
                .issueCompanyName(vendorResult.cardName())
                .resultCode(vendorResult.resCd())
                .resultMessage(vendorResult.resMsg())
                .pgApprovalResult(vendorResult.rawResponse())
                .appTime(vendorResult.appTime())
                .build();
        event.completeWithApproval(approvalInfo);
        updatePspPaymentEventPort.update(event);

        changeOrderStatusUseCase.changeOrderStatus(
                ChangeOrderStatusCommand.builder()
                        .id(payment.getOrderId())
                        .orderStatus(OrderStatus.PAID)
                        .build()
        );

        recordLedgerEntryForPaymentApproval(payment);

        return ApprovePaymentResult.builder()
                .orderId(payment.getOrderId())
                .orderKey(payment.getOrderKey().toString())
                .pgPaymentKey(vendorResult.tno())
                .amount(payment.getPaymentAmount())
                .resultCode(vendorResult.resCd())
                .resultMessage(vendorResult.resMsg())
                .payMethod(vendorResult.payMethod())
                .build();
    }

    /**
     * TX-2: 실패 상태 커밋 (FAILED + order FAILED)
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitFailure(PaymentApprovalContext context, String resultCode, String resultMessage) {
        Payment payment = context.payment();
        PspPaymentEvent event = context.event();

        payment.markAsFailed();
        updatePaymentPort.update(payment);

        event.failExecution(resultCode, resultMessage);
        updatePspPaymentEventPort.update(event);

        changeOrderStatusUseCase.changeOrderStatus(
                ChangeOrderStatusCommand.builder()
                        .id(payment.getOrderId())
                        .orderStatus(OrderStatus.FAILED)
                        .build()
        );
    }

    /**
     * TX-2: UNKNOWN 상태 커밋 (이벤트만 UNKNOWN, payment/order 변경 없음)
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitUnknown(PaymentApprovalContext context, String resultCode, String resultMessage) {
        PspPaymentEvent event = context.event();

        event.markUnknown(resultCode, resultMessage);
        updatePspPaymentEventPort.update(event);
    }

    private void verifyPaymentAmount(Order order, Payment payment) {
        Long couponAmount = FormatValidator.hasValue(order.getCouponAmount())
                ? order.getCouponAmount()
                : 0L;
        Long pointAmount = FormatValidator.hasValue(order.getPointAmount())
                ? order.getPointAmount()
                : 0L;
        Long expectedAmount = Math.subtractExact(
                Math.subtractExact(order.getTotalAmount(), couponAmount),
                pointAmount
        );

        if (FormatValidator.notEquals(expectedAmount, payment.getPaymentAmount())) {
            log.error("결제 금액 불일치: orderId={}, 주문금액={}, 쿠폰={}, 포인트={}, 예상결제금액={}, 실제결제금액={}",
                    order.getId(), order.getTotalAmount(), couponAmount, pointAmount,
                    expectedAmount, payment.getPaymentAmount());
            throw new PaymentAmountMismatchException(expectedAmount, payment.getPaymentAmount());
        }
    }

    private Order findVerifiedOrder(Long orderId, Long buyerId) {
        Order order = findOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.isBuyer(buyerId)) {
            log.warn("결제 승인 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}",
                    orderId, order.getBuyerId(), buyerId);
            throw new UnauthorizedOrderAccessException();
        }
        return order;
    }

    private void recordLedgerEntryForPaymentApproval(Payment payment) {
        try {
            recordLedgerEntryUseCase.recordPaymentApproval(
                    payment.getOrderId(), payment.getPaymentAmount()
            );
        } catch (Exception e) {
            log.error("결제 승인 분개 기록 실패 - orderId: {}, error: {}", payment.getOrderId(), e.getMessage(), e);
        }
    }
}
