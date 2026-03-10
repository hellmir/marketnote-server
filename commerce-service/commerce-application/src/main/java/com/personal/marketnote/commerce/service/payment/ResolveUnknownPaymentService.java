package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentApprovalInfo;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.InvalidPaymentEventResolveStatusException;
import com.personal.marketnote.commerce.exception.PaymentEventNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ResolveUnknownPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ResolveUnknownPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.ResolveUnknownPaymentUseCase;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePaymentPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePspPaymentEventPort;
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
public class ResolveUnknownPaymentService implements ResolveUnknownPaymentUseCase {
    private static final String RESOLVE_STATUS_COMPLETE = "COMPLETE";
    private static final String RESOLVE_STATUS_FAILED = "FAILED";

    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final FindPaymentPort findPaymentPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Override
    public ResolveUnknownPaymentResult resolve(ResolveUnknownPaymentCommand command) {
        PspPaymentEvent event = findPspPaymentEventPort.findByOrderKey(command.orderKey())
                .orElseThrow(() -> new PaymentEventNotFoundException(command.orderKey()));

        Payment payment = findPaymentPort.findByOrderKey(UUID.fromString(command.orderKey()))
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        return switch (command.resolvedStatus()) {
            case RESOLVE_STATUS_COMPLETE -> resolveToComplete(event, payment, command);
            case RESOLVE_STATUS_FAILED -> resolveToFailed(event, payment, command);
            default -> throw new InvalidPaymentEventResolveStatusException(command.resolvedStatus());
        };
    }

    private ResolveUnknownPaymentResult resolveToComplete(
            PspPaymentEvent event, Payment payment, ResolveUnknownPaymentCommand command
    ) {
        PaymentApprovalInfo approvalInfo = PaymentApprovalInfo.builder()
                .pgPaymentKey(command.pgPaymentKey())
                .method(event.getMethod())
                .approvalNumber(command.approvalNumber())
                .resultCode(command.resultCode())
                .resultMessage(command.resultMessage())
                .appTime(command.appTime())
                .build();
        event.resolveToComplete(approvalInfo);
        updatePspPaymentEventPort.update(event);

        payment.markAsSuccess(command.pgPaymentKey());
        updatePaymentPort.update(payment);

        changeOrderStatusUseCase.changeOrderStatus(
                ChangeOrderStatusCommand.builder()
                        .id(payment.getOrderId())
                        .orderStatus(OrderStatus.PAID)
                        .build()
        );

        recordLedgerEntry(payment);

        log.info("UNKNOWN → COMPLETE 해소 - orderKey: {}, orderId: {}", command.orderKey(), payment.getOrderId());

        return ResolveUnknownPaymentResult.builder()
                .orderKey(command.orderKey())
                .resolvedStatus(RESOLVE_STATUS_COMPLETE)
                .orderId(payment.getOrderId())
                .build();
    }

    private ResolveUnknownPaymentResult resolveToFailed(
            PspPaymentEvent event, Payment payment, ResolveUnknownPaymentCommand command
    ) {
        event.resolveToFailed(command.resultCode(), command.resultMessage());
        updatePspPaymentEventPort.update(event);

        payment.markAsFailed();
        updatePaymentPort.update(payment);

        changeOrderStatusUseCase.changeOrderStatus(
                ChangeOrderStatusCommand.builder()
                        .id(payment.getOrderId())
                        .orderStatus(OrderStatus.FAILED)
                        .build()
        );

        log.info("UNKNOWN → FAILED 해소 - orderKey: {}, orderId: {}", command.orderKey(), payment.getOrderId());

        return ResolveUnknownPaymentResult.builder()
                .orderKey(command.orderKey())
                .resolvedStatus(RESOLVE_STATUS_FAILED)
                .orderId(payment.getOrderId())
                .build();
    }

    private void recordLedgerEntry(Payment payment) {
        try {
            recordLedgerEntryUseCase.recordPaymentApproval(
                    payment.getOrderId(), payment.getPaymentAmount()
            );
        } catch (Exception e) {
            log.error("UNKNOWN 해소 분개 기록 실패 - orderId: {}, error: {}", payment.getOrderId(), e.getMessage(), e);
        }
    }
}
