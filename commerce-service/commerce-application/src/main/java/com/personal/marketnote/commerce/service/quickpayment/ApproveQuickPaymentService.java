package com.personal.marketnote.commerce.service.quickpayment;

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
                command.buyerId(), command.orderKey(), command.quickPaymentCardId());

        QuickPaymentCard card = findQuickPaymentCardPort
                .findActiveByIdAndUserId(command.quickPaymentCardId(), command.buyerId())
                .orElseThrow(() -> new QuickPaymentCardNotFoundException(
                        command.quickPaymentCardId(), command.buyerId()));

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
            Short installment = parseInstallment(portResult.installmentMonths());
            PaymentApprovalVendorResult vendorResult = toVendorResult(portResult);
            ApprovePaymentResult paymentResult = txHelper.commitSuccess(context, vendorResult, installment);
            return toQuickPaymentResult(paymentResult);
        }

        txHelper.commitFailure(context, portResult.resultCode(), portResult.resultMessage());
        throw QuickPaymentApprovalFailedException.approvalFailed(portResult.resultCode(), portResult.resultMessage());
    }

    private ApproveQuickPaymentPortResult requestBatchPaymentApproval(
            ApproveQuickPaymentCommand command, QuickPaymentCard card, PaymentApprovalContext context
    ) {
        try {
            ApproveQuickPaymentPortCommand portCommand = ApproveQuickPaymentPortCommand.builder()
                    .batchKey(card.getBatchKey())
                    .groupId(card.getGroupId())
                    .orderKey(command.orderKey())
                    .amount(String.valueOf(context.paymentAmount()))
                    .goodName(command.goodName())
                    .build();
            return approveQuickPaymentPort.approvePayment(portCommand);
        } catch (PaymentVendorConnectionFailedException e) {
            txHelper.commitFailure(context, "CONN_FAIL", e.getMessage());
            throw QuickPaymentApprovalFailedException.approvalRequestFailed(e);
        } catch (Exception e) {
            txHelper.commitUnknown(context, "UNKNOWN", "PG사 통신 오류: " + e.getMessage());
            throw QuickPaymentApprovalFailedException.approvalRequestFailed(e);
        }
    }

    private PaymentApprovalVendorResult toVendorResult(ApproveQuickPaymentPortResult portResult) {
        return PaymentApprovalVendorResult.builder()
                .success(portResult.success())
                .resultCode(portResult.resultCode())
                .resultMessage(portResult.resultMessage())
                .transactionId(portResult.transactionId())
                .amount(portResult.amount())
                .payMethod(portResult.payMethod())
                .cardCode(portResult.cardCode())
                .cardName(portResult.cardName())
                .cardNumber(portResult.cardNumber())
                .approvalNumber(portResult.approvalNumber())
                .approvalTime(portResult.approvalTime())
                .installmentMonths(portResult.installmentMonths())
                .cardAmount(portResult.cardAmount())
                .partialCancelYn(portResult.partialCancelYn())
                .cardBinType01(portResult.cardBinType01())
                .cardBinType02(portResult.cardBinType02())
                .rawResponse(portResult.rawResponse())
                .build();
    }

    private ApproveQuickPaymentResult toQuickPaymentResult(ApprovePaymentResult paymentResult) {
        return ApproveQuickPaymentResult.builder()
                .orderId(paymentResult.orderId())
                .orderKey(paymentResult.orderKey())
                .pgPaymentKey(paymentResult.pgPaymentKey())
                .amount(paymentResult.amount())
                .resultCode(paymentResult.resultCode())
                .resultMessage(paymentResult.resultMessage())
                .payMethod(paymentResult.payMethod())
                .build();
    }

    private Short parseInstallment(String quota) {
        try {
            return Short.parseShort(quota);
        } catch (NumberFormatException e) {
            return (short) 0;
        }
    }
}
