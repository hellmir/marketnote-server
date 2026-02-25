package com.personal.marketnote.commerce.domain.payment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PspPaymentEventSnapshotState {
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
}
