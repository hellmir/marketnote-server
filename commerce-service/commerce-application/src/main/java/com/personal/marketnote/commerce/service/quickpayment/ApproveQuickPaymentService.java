package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.domain.payment.Installment;
import com.personal.marketnote.commerce.domain.payment.InvalidInstallmentException;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.exception.QuickPaymentApprovalFailedException;
import com.personal.marketnote.commerce.exception.QuickPaymentCardNotFoundException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.ApproveQuickPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.in.result.quickpayment.ApproveQuickPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.ApproveQuickPaymentUseCase;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPort;
import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.FindQuickPaymentCardPort;
import com.personal.marketnote.commerce.service.payment.PaymentApprovalContext;
import com.personal.marketnote.commerce.service.payment.PaymentApprovalTransactionHelper;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class ApproveQuickPaymentService implements ApproveQuickPaymentUseCase {
    private final PaymentApprovalTransactionHelper txHelper;
    private final FindQuickPaymentCardPort findQuickPaymentCardPort;
    private final ApproveQuickPaymentPort approveQuickPaymentPort;
    private final PaymentVendorPort paymentVendorPort;

    @Override
    public ApproveQuickPaymentResult approve(ApproveQuickPaymentCommand command) {
        log.info("빠른결제 승인 요청 - buyerId: {}, orderKey: {}, cardId: {}",
                command.buyerId(), command.orderKey(), command.quickPaymentCardId()
        );

        QuickPaymentCard card = findQuickPaymentCardPort
                .findActiveByIdAndUserId(command.quickPaymentCardId(), command.buyerId())
                .orElseThrow(() -> {
                    log.warn(
                            "빠른결제 카드를 찾을 수 없습니다. quickPaymentCardId={}, buyerId={}",
                            command.quickPaymentCardId(), command.buyerId()
                    );
                    return new QuickPaymentCardNotFoundException();
                });

        // TX-1: 검증 + PspPaymentEvent 생성 + EXECUTING
        PaymentApprovalContext context = txHelper.prepareExecutionForQuickPayment(
                command.buyerId(),
                command.orderKey(),
                paymentVendorPort.getVendorKey(),
                paymentVendorPort.getShopCode()
        );

        // PG사 호출 (트랜잭션 외부)
        ApproveQuickPaymentPortResult portResult = requestBatchPaymentApproval(command, card, context);

        // TX-2: 성공 또는 실패
        if (portResult.success()) {
            Installment installment = parseInstallment(portResult.installmentMonths());
            PaymentApprovalVendorResult vendorResult = PaymentApprovalVendorResult.from(portResult);
            ApprovePaymentResult paymentResult = txHelper.commitSuccess(context, vendorResult, installment);
            return ApproveQuickPaymentResult.from(paymentResult);
        }

        txHelper.commitFailure(context, portResult.resultCode(), portResult.resultMessage());
        throw QuickPaymentApprovalFailedException.approvalFailed(portResult.resultCode(), portResult.resultMessage());
    }

    private ApproveQuickPaymentPortResult requestBatchPaymentApproval(
            ApproveQuickPaymentCommand command, QuickPaymentCard card, PaymentApprovalContext context
    ) {
        try {
            ApproveQuickPaymentPortCommand portCommand = ApproveQuickPaymentPortCommand.of(
                    command, card, context.payment()
            );
            return approveQuickPaymentPort.approvePayment(portCommand);
        } catch (PaymentVendorConnectionFailedException e) {
            txHelper.commitFailure(context, "CONN_FAIL", e.getMessage());
            throw QuickPaymentApprovalFailedException.approvalRequestFailed(e);
        } catch (Exception e) {
            txHelper.commitUnknown(context, "UNKNOWN", "PG사 통신 오류: " + e.getMessage());
            throw QuickPaymentApprovalFailedException.approvalRequestFailed(e);
        }
    }

    private Installment parseInstallment(String quota) {
        try {
            return Installment.of(Short.parseShort(quota));
        } catch (NumberFormatException | InvalidInstallmentException e) {
            log.warn("할부 개월 수 파싱 실패, 일시불로 폴백합니다. quota={}", quota, e);
            return Installment.of((short) 0);
        }
    }
}
