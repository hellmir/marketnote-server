package com.personal.marketnote.commerce.exception;

public class QuickPaymentApprovalFailedException extends RuntimeException {
    private static final String APPROVAL_FAILED = "빠른결제 승인 실패 [%s]: %s";
    private static final String APPROVAL_REQUEST_FAILED = "빠른결제 승인 요청 실패";

    private QuickPaymentApprovalFailedException(String message) {
        super(message);
    }

    private QuickPaymentApprovalFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static QuickPaymentApprovalFailedException approvalFailed(String resultCode, String resultMessage) {
        return new QuickPaymentApprovalFailedException(String.format(APPROVAL_FAILED, resultCode, resultMessage));
    }

    public static QuickPaymentApprovalFailedException approvalRequestFailed(Throwable cause) {
        return new QuickPaymentApprovalFailedException(APPROVAL_REQUEST_FAILED, cause);
    }
}
