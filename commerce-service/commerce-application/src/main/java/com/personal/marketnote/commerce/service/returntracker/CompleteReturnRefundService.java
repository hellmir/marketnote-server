package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.exception.ReturnPgRefundFailedException;
import com.personal.marketnote.commerce.exception.ReturnTrackerNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.RefundPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnRefundUseCase;
import com.personal.marketnote.commerce.port.out.returntracker.FindReturnTrackerPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CompleteReturnRefundService implements CompleteReturnRefundUseCase {

    private final RefundPaymentUseCase refundPaymentUseCase;
    private final FindReturnTrackerPort findReturnTrackerPort;
    private final UpdateReturnTrackerPort updateReturnTrackerPort;
    private final Clock clock;

    @Override
    public void completeReturnRefund(CompleteReturnRefundCommand command) {
        ReturnTracker tracker = findReturnTrackerPort.findByOrderId(command.orderId())
                .orElseThrow(() -> new ReturnTrackerNotFoundException(command.orderId()));

        if (tracker.isRefundCompleted()) {
            log.info("이미 환불 완료된 ReturnTracker (멱등). orderId={}", command.orderId());
            return;
        }

        if (tracker.isRefundFailed()) {
            tracker.retryRefund();
            updateReturnTrackerPort.update(tracker);
            log.info("FAILED 상태 ReturnTracker를 PENDING으로 리셋 (재시도). orderId={}", command.orderId());
        }

        RefundPaymentCommand refundCommand = buildRefundCommand(command);

        try {
            refundPaymentUseCase.refund(refundCommand);
            completeRefund(tracker);
        } catch (PaymentAlreadyRefundedException e) {
            log.info("이미 환불 처리된 결제 (멱등 처리). orderId={}, message={}",
                    command.orderId(), e.getMessage());
            completeRefund(tracker);
        } catch (PaymentCancelException e) {
            log.error("PG 환불 실패. orderId={}, message={}", command.orderId(), e.getMessage());
            failRefund(tracker);
            throw new ReturnPgRefundFailedException(command.orderId(), e);
        }
    }

    private RefundPaymentCommand buildRefundCommand(CompleteReturnRefundCommand command) {
        return RefundPaymentCommand.builder()
                .orderKey(command.orderKey())
                .orderId(command.orderId())
                .cancelAmount(command.returnAmount())
                .paymentAmount(command.paymentAmount())
                .isFullCancel(command.isFullReturn())
                .returnShippingFee(command.returnShippingFee())
                .build();
    }

    private void completeRefund(ReturnTracker tracker) {
        LocalDateTime now = LocalDateTime.now(clock);
        tracker.completeRefund(now);
        updateReturnTrackerPort.update(tracker);

        log.info("반품 PG 환불 완료. orderId={}", tracker.getOrderId());
    }

    private void failRefund(ReturnTracker tracker) {
        tracker.failRefund();
        updateReturnTrackerPort.update(tracker);

        log.warn("반품 PG 환불 실패. orderId={}", tracker.getOrderId());
    }
}
