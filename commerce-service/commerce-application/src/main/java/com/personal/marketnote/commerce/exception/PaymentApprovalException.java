package com.personal.marketnote.commerce.exception;

public class PaymentApprovalException extends IllegalStateException {
    private static final String TRADE_REGISTER_FAILED = "거래등록 실패 [%s]: %s";
    private static final String APPROVAL_REQUEST_FAILED = "결제 승인 요청 실패";
    private static final String APPROVAL_FAILED = "결제 승인 실패 [%s]: %s";

    private PaymentApprovalException(String message) {
        super(message);
    }

    private PaymentApprovalException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PaymentApprovalException tradeRegisterFailed(String resultCode, String resultMessage) {
        return new PaymentApprovalException(String.format(TRADE_REGISTER_FAILED, resultCode, resultMessage));
    }

    public static PaymentApprovalException approvalRequestFailed(Throwable cause) {
        return new PaymentApprovalException(APPROVAL_REQUEST_FAILED, cause);
    }

    public static PaymentApprovalException approvalFailed(String resultCode, String resultMessage) {
        return new PaymentApprovalException(String.format(APPROVAL_FAILED, resultCode, resultMessage));
    }
}
