package com.personal.marketnote.commerce.domain.payment;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PspPaymentEvent {
    private Long id;
    private Long orderId;
    private String orderKey;
    private String pgCompanyKey;
    private String pgShopKey;
    private String pgPaymentKey;
    private String pgApprovalResult;
    private String pgCancelApprovalResult;
    private String shopTransactionId;
    private PaymentEventStatus poStatus;
    private String method;
    private Long amount;
    private Long vatAmount;
    private Long natAmount;
    private String cardNumber;
    private String approvalNumber;
    private Short installment;
    private String issueCompanyCode;
    private String issueCompanyName;
    private String purchaseCompanyCode;
    private String purchaseCompanyName;
    private String bankCode;
    private String bankName;
    private String billNumber;
    private String billSequenceNumber;
    private String userIp;
    private String paymentInfo;
    private String paymentSource;
    private String resultCode;
    private String resultMessage;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static PspPaymentEvent createReady(Payment payment, String siteCd, String payType) {
        return PspPaymentEvent.builder()
                .orderId(payment.getOrderId())
                .orderKey(payment.getOrderKey().toString())
                .pgCompanyKey("NHN_KCP")
                .pgShopKey(siteCd)
                .poStatus(PaymentEventStatus.READY)
                .method(payType)
                .amount(payment.getPaymentAmount())
                .build();
    }

    public static PspPaymentEvent from(PspPaymentEventSnapshotState state) {
        return PspPaymentEvent.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .orderKey(state.getOrderKey())
                .pgCompanyKey(state.getPgCompanyKey())
                .pgShopKey(state.getPgShopKey())
                .pgPaymentKey(state.getPgPaymentKey())
                .pgApprovalResult(state.getPgApprovalResult())
                .pgCancelApprovalResult(state.getPgCancelApprovalResult())
                .shopTransactionId(state.getShopTransactionId())
                .poStatus(state.getPoStatus())
                .method(state.getMethod())
                .amount(state.getAmount())
                .vatAmount(state.getVatAmount())
                .natAmount(state.getNatAmount())
                .cardNumber(state.getCardNumber())
                .approvalNumber(state.getApprovalNumber())
                .installment(state.getInstallment())
                .issueCompanyCode(state.getIssueCompanyCode())
                .issueCompanyName(state.getIssueCompanyName())
                .purchaseCompanyCode(state.getPurchaseCompanyCode())
                .purchaseCompanyName(state.getPurchaseCompanyName())
                .bankCode(state.getBankCode())
                .bankName(state.getBankName())
                .billNumber(state.getBillNumber())
                .billSequenceNumber(state.getBillSequenceNumber())
                .userIp(state.getUserIp())
                .paymentInfo(state.getPaymentInfo())
                .paymentSource(state.getPaymentSource())
                .resultCode(state.getResultCode())
                .resultMessage(state.getResultMessage())
                .paidAt(state.getPaidAt())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void startExecution() {
        if (!this.poStatus.isReady()) {
            throw new IllegalStateException("READY 상태에서만 EXECUTING으로 전이할 수 있습니다. 현재 상태: " + this.poStatus);
        }
        this.poStatus = PaymentEventStatus.EXECUTING;
    }

    public void completeWithApproval(PaymentApprovalInfo info) {
        if (!this.poStatus.isExecuting()) {
            throw new IllegalStateException("EXECUTING 상태에서만 승인 완료 처리할 수 있습니다. 현재 상태: " + this.poStatus);
        }
        this.poStatus = PaymentEventStatus.COMPLETE;
        this.pgPaymentKey = info.getPgPaymentKey();
        this.method = info.getMethod();
        this.cardNumber = info.getCardNumber();
        this.approvalNumber = info.getApprovalNumber();
        this.installment = info.getInstallment();
        this.issueCompanyCode = info.getIssueCompanyCode();
        this.issueCompanyName = info.getIssueCompanyName();
        this.resultCode = info.getResultCode();
        this.resultMessage = info.getResultMessage();
        this.pgApprovalResult = info.getPgApprovalResult();
        this.paidAt = parseAppTime(info.getAppTime());
    }

    private LocalDateTime parseAppTime(String appTime) {
        if (FormatValidator.hasNoValue(appTime)) {
            return LocalDateTime.now();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.parse(appTime, formatter);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public void failExecution(String resultCode, String resultMessage) {
        if (!this.poStatus.isExecuting()) {
            throw new IllegalStateException("EXECUTING 상태에서만 실행 실패 처리할 수 있습니다. 현재 상태: " + this.poStatus);
        }
        this.poStatus = PaymentEventStatus.READY;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public void cancel(String pgCancelApprovalResult) {
        if (!this.poStatus.isComplete()) {
            throw new IllegalStateException("COMPLETE 상태에서만 취소할 수 있습니다. 현재 상태: " + this.poStatus);
        }
        this.poStatus = PaymentEventStatus.CANCELLED;
        this.pgCancelApprovalResult = pgCancelApprovalResult;
    }

    public void partialRefund(String pgCancelApprovalResult) {
        if (!this.poStatus.isRefundable()) {
            throw new IllegalStateException("COMPLETE 또는 PARTIALLY_REFUNDED 상태에서만 부분 환불할 수 있습니다. 현재 상태: " + this.poStatus);
        }
        this.poStatus = PaymentEventStatus.PARTIALLY_REFUNDED;
        this.pgCancelApprovalResult = pgCancelApprovalResult;
    }

    public void refund(String pgCancelApprovalResult) {
        if (!this.poStatus.isRefundable()) {
            throw new IllegalStateException("COMPLETE 또는 PARTIALLY_REFUNDED 상태에서만 환불할 수 있습니다. 현재 상태: " + this.poStatus);
        }
        this.poStatus = PaymentEventStatus.REFUNDED;
        this.pgCancelApprovalResult = pgCancelApprovalResult;
    }
}
