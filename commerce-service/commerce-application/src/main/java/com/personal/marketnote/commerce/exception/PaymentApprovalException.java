package com.personal.marketnote.commerce.exception;

public class PaymentApprovalException extends IllegalStateException {
    private static final String KCP_TRADE_REGISTER_FAILED = "KCP 거래등록 실패 [%s]: %s";
    private static final String KCP_APPROVAL_REQUEST_FAILED = "KCP 결제 승인 요청 실패";
    private static final String KCP_APPROVAL_FAILED = "KCP 결제 승인 실패 [%s]: %s";

    private PaymentApprovalException(String message) {
        super(message);
    }

    private PaymentApprovalException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PaymentApprovalException kcpTradeRegisterFailed(String resCd, String resMsg) {
        return new PaymentApprovalException(String.format(KCP_TRADE_REGISTER_FAILED, resCd, resMsg));
    }

    public static PaymentApprovalException kcpApprovalRequestFailed(Throwable cause) {
        return new PaymentApprovalException(KCP_APPROVAL_REQUEST_FAILED, cause);
    }

    public static PaymentApprovalException kcpApprovalFailed(String resCd, String resMsg) {
        return new PaymentApprovalException(String.format(KCP_APPROVAL_FAILED, resCd, resMsg));
    }
}
