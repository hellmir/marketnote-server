package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.exception.PaymentApprovalException;
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.ApprovePaymentUseCase;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 승인 서비스
 *
 * <p>트랜잭션을 3단계로 분리하여 오케스트레이션만 수행한다:
 * <ol>
 *   <li>TX-1: 검증 + EXECUTING 커밋 (prepareExecution)</li>
 *   <li>KCP 호출 (트랜잭션 외부)</li>
 *   <li>TX-2: 결과 반영 커밋 (commitSuccess / commitFailure / commitUnknown)</li>
 * </ol>
 *
 * <p>이 구조로 handleFailure/commitUnknown 후 예외 throw 시에도 상태 변경이 롤백되지 않는다. (#1161 해결)
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class ApprovePaymentService implements ApprovePaymentUseCase {
    private final PaymentApprovalTransactionHelper txHelper;
    private final PaymentVendorPort paymentVendorPort;

    @Override
    public ApprovePaymentResult approve(ApprovePaymentCommand command) {
        // TX-1: 검증 + EXECUTING 커밋
        PaymentApprovalContext context = txHelper.prepareExecution(command);

        PaymentApprovalVendorResult vendorResult = requestPaymentApprovalToPsp(command, context);

        // TX-2: 성공 또는 실패 커밋
        if (vendorResult.isSuccess()) {
            Short installment = parseInstallment(vendorResult.quota());
            return txHelper.commitSuccess(context, vendorResult, installment);
        }

        txHelper.commitFailure(context, vendorResult.resCd(), vendorResult.resMsg());
        throw PaymentApprovalException.kcpApprovalFailed(vendorResult.resCd(), vendorResult.resMsg());
    }

    private PaymentApprovalVendorResult requestPaymentApprovalToPsp(
            ApprovePaymentCommand command, PaymentApprovalContext context
    ) {
        // KCP 호출 (트랜잭션 외부 — 어댑터에서 재시도 수행)
        try {
            return paymentVendorPort.approvePayment(buildVendorCommand(command, context));
        } catch (PaymentVendorConnectionFailedException e) {
            // TX-2: 연결 실패 → FAILED (요청이 KCP에 도달하지 않음)
            txHelper.commitFailure(context, "CONN_FAIL", e.getMessage());
            throw PaymentApprovalException.kcpApprovalRequestFailed(e);
        } catch (Exception e) {
            // TX-2: 기타 통신 오류 → UNKNOWN (요청이 KCP에 도달했을 수 있음)
            txHelper.commitUnknown(context, "UNKNOWN", "KCP 통신 오류: " + e.getMessage());
            throw PaymentApprovalException.kcpApprovalRequestFailed(e);
        }
    }

    private PaymentApprovalVendorCommand buildVendorCommand(
            ApprovePaymentCommand command, PaymentApprovalContext context
    ) {
        return PaymentApprovalVendorCommand.builder()
                .encData(command.encData())
                .encInfo(command.encInfo())
                .ordrMony(String.valueOf(context.paymentAmount()))
                .ordrNo(command.orderKey())
                .payType(command.payType())
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
