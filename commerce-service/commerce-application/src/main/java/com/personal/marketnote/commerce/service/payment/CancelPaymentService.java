package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.CancelPaymentUseCase;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 취소 서비스
 *
 * <p>트랜잭션을 분리하여 오케스트레이션만 수행한다:
 * <ol>
 *   <li>TX-1 prepareCancel: 검증 + Context 생성 (readOnly, DB 변경 없음)</li>
 *   <li>배송비 차감으로 환불액 0원이면 TX-2 commitCancelSkipPg 단일 처리 후 종료</li>
 *   <li>PG 취소 API 호출 (트랜잭션 외부)</li>
 *   <li>TX-2A commitPspEventCancel: PSP Event만 커밋 (수동 복구 근거)</li>
 *   <li>TX-2B commitPaymentAndPostProcess: payment/refund/재고/포인트/Outbox 커밋</li>
 * </ol>
 *
 * <p>TX-2B 실패 시에도 TX-2A의 PSP Event 기록으로 PG 상태와의 불일치를 운영자가 인지/복구할 수 있다. (#4097 해결)</p>
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class CancelPaymentService implements CancelPaymentUseCase {
    private final PaymentCancelTransactionHelper txHelper;
    private final PaymentVendorPort paymentVendorPort;

    @Override
    public void cancel(CancelPaymentCommand command) {
        // TX-1: 검증 + Context 생성 (DB 쓰기 없음)
        PaymentCancelContext context = txHelper.prepareCancel(command);

        // 배송비 차감으로 환불액 0원 경로 — PG 호출 없이 통합 커밋
        if (context.adjustedCancelAmount() == 0L && context.shippingFeeDeduction() > 0L) {
            txHelper.commitCancelSkipPg(context);
            return;
        }

        // PG 취소 API 호출 (트랜잭션 외부)
        PaymentCancelVendorResult vendorResult = requestPaymentCancellationToPsp(
                context.transactionNumber(),
                context.modType(),
                context.adjustedCancelAmount(),
                context.remainAmount(),
                context.adjustedCancelReason()
        );

        if (!vendorResult.success()) {
            log.error("PG 결제 취소 실패 - transactionNumber: {}, resultCode: {}, resultMessage: {}",
                    context.transactionNumber(), vendorResult.resultCode(), vendorResult.resultMessage()
            );
            throw new PaymentCancelException("결제 취소에 실패했습니다.");
        }

        // TX-2A: PSP Event만 커밋 (PG 취소 완료 근거 확보)
        txHelper.commitPspEventCancel(context, vendorResult);

        // TX-2B: payment/refund/재고/포인트/Outbox 커밋
        txHelper.commitPaymentAndPostProcess(context, vendorResult);
    }

    private PaymentCancelVendorResult requestPaymentCancellationToPsp(
            String transactionNumber, String cancelType, Long cancelAmount, Long remainAmount, String cancelReason
    ) {
        PaymentCancelVendorCommand vendorCommand = PaymentCancelVendorCommand.builder()
                .transactionId(transactionNumber)
                .cancelType(cancelType)
                .cancelAmount(cancelAmount)
                .remainAmount(remainAmount)
                .cancelReason(cancelReason)
                .build();

        return paymentVendorPort.cancelPayment(vendorCommand);
    }
}
