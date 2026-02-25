package com.personal.marketnote.commerce.domain.payment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class PaymentApprovalInfo {
    private final String pgPaymentKey;
    private final String method;
    private final String cardNumber;
    private final String approvalNumber;
    private final Short installment;
    private final String issueCompanyCode;
    private final String issueCompanyName;
    private final String resultCode;
    private final String resultMessage;
    private final String pgApprovalResult;
    private final String appTime;
}
