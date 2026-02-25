package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.CancelPaymentUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
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
public class CancelPaymentService implements CancelPaymentUseCase {
    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final PaymentVendorPort paymentVendorPort;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Override
    public void cancel(CancelPaymentCommand command) {
        UUID orderKeyUuid = UUID.fromString(command.orderKey());

        Payment payment = findPaymentPort.findByOrderKey(orderKeyUuid)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        verifyOrderOwnership(payment.getOrderId(), command.buyerId());

        PspPaymentEvent event = findPspPaymentEventPort.findByOrderKey(command.orderKey())
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        String tno = event.getPgPaymentKey();
        boolean isFullCancel = command.isFullCancel();
        String modType = isFullCancel ? "STSC" : "STPC";

        Long alreadyRefunded = FormatValidator.hasValue(payment.getRefundAmount()) ? payment.getRefundAmount() : 0L;
        Long refundableAmount = payment.getPaymentAmount() - alreadyRefunded;

        Long cancelAmount;
        if (isFullCancel) {
            cancelAmount = refundableAmount;
        } else {
            cancelAmount = command.cancelAmount();
            if (FormatValidator.hasNoValue(cancelAmount) || cancelAmount <= 0L) {
                throw new IllegalArgumentException("부분취소 금액은 0보다 커야 합니다");
            }
            if (cancelAmount > refundableAmount) {
                throw new IllegalArgumentException(
                        "취소 금액(" + cancelAmount + ")이 환불 가능 금액(" + refundableAmount + ")을 초과합니다"
                );
            }
        }

        Long remainAmount = refundableAmount - cancelAmount;

        PaymentCancelVendorCommand vendorCommand = PaymentCancelVendorCommand.builder()
                .tno(tno)
                .modType(modType)
                .modMny(cancelAmount)
                .remMny(remainAmount)
                .modDesc(command.cancelReason())
                .build();

        PaymentCancelVendorResult vendorResult = paymentVendorPort.cancelPayment(vendorCommand);

        if (!vendorResult.isSuccess()) {
            throw new PaymentCancelException(
                    "KCP 결제 취소 실패 [" + vendorResult.resCd() + "]: " + vendorResult.resMsg()
            );
        }

        if (isFullCancel) {
            payment.markAsRefunded();
            event.cancel(vendorResult.rawResponse());
        } else {
            payment.markAsPartiallyRefunded(cancelAmount);
            event.partialRefund(vendorResult.rawResponse());
        }

        updatePaymentPort.update(payment);
        updatePspPaymentEventPort.update(event);

        if (isFullCancel) {
            changeOrderStatusUseCase.changeOrderStatus(
                    ChangeOrderStatusCommand.builder()
                            .id(payment.getOrderId())
                            .orderStatus(OrderStatus.CANCEL_REQUESTED)
                            .build()
            );
        }
    }

    private void verifyOrderOwnership(Long orderId, Long buyerId) {
        Order order = findOrderPort.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("주문 정보를 찾을 수 없습니다: " + orderId));
        if (!order.getBuyerId().equals(buyerId)) {
            throw new IllegalStateException("해당 주문에 대한 권한이 없습니다");
        }
    }
}
