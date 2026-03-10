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
import com.personal.marketnote.commerce.port.in.usecase.payment.ApprovePaymentUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class ApprovePaymentService implements ApprovePaymentUseCase {
    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final PaymentVendorPort paymentVendorPort;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Override
    public ApprovePaymentResult approve(ApprovePaymentCommand command) {
        UUID orderKey = UUID.fromString(command.orderKey());
        Payment payment = findPaymentPort.findByOrderKey(orderKey)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        Order order = findVerifiedOrder(payment.getOrderId(), command.buyerId());
        verifyPaymentAmount(order, payment);

        PspPaymentEvent event = findPspPaymentEventPort.findByOrderKey(command.orderKey())
                .orElseThrow(() -> new PaymentEventNotFoundException(command.orderKey()));
        event.startExecution();
        updatePspPaymentEventPort.update(event);

        PaymentApprovalVendorResult vendorResult = requestPaymentApprovalToPsp(
                command, payment, event, payment.getPaymentAmount()
        );

        if (vendorResult.isSuccess()) {
            return handleSuccess(payment, event, vendorResult);
        }

        handleFailure(payment, event, vendorResult.resMsg());
        throw PaymentApprovalException.kcpApprovalFailed(vendorResult.resCd(), vendorResult.resMsg());
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

    private PaymentApprovalVendorResult requestPaymentApprovalToPsp(
            ApprovePaymentCommand command, Payment payment, PspPaymentEvent event, Long amount
    ) {
        PaymentApprovalVendorCommand vendorCommand = PaymentApprovalVendorCommand.builder()
                .encData(command.encData())
                .encInfo(command.encInfo())
                .ordrMony(String.valueOf(amount))
                .ordrNo(command.orderKey())
                .payType(command.payType())
                .build();

        try {
            return paymentVendorPort.approvePayment(vendorCommand);
        } catch (Exception e) {
            handleFailure(payment, event, "KCP 통신 오류: " + e.getMessage());
            throw PaymentApprovalException.kcpApprovalRequestFailed(e);
        }
    }

    private ApprovePaymentResult handleSuccess(
            Payment payment, PspPaymentEvent event, PaymentApprovalVendorResult vendorResult
    ) {
        payment.markAsSuccess(vendorResult.tno());
        updatePaymentPort.update(payment);

        Short installment = parseInstallment(vendorResult.quota());
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

    private void handleFailure(Payment payment, PspPaymentEvent event, String reason) {
        payment.markAsFailed();
        updatePaymentPort.update(payment);

        event.failExecution("FAIL", reason);
        updatePspPaymentEventPort.update(event);

        changeOrderStatusUseCase.changeOrderStatus(
                ChangeOrderStatusCommand.builder()
                        .id(payment.getOrderId())
                        .orderStatus(OrderStatus.FAILED)
                        .build()
        );
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

    private Short parseInstallment(String quota) {
        try {
            return Short.parseShort(quota);
        } catch (NumberFormatException e) {
            return (short) 0;
        }
    }
}
