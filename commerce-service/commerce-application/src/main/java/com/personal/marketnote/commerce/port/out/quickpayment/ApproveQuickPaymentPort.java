package com.personal.marketnote.commerce.port.out.quickpayment;

/**
 * 빠른결제 결제 승인 포트
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description KCP 배치키 기반 결제 승인을 요청합니다.
 */
public interface ApproveQuickPaymentPort {
    /**
     * @param command 배치 결제 승인 요청 정보 {@link ApproveQuickPaymentPortCommand}
     * @return 배치 결제 승인 결과 {@link ApproveQuickPaymentPortResult}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description KCP 배치키 기반 결제 승인을 요청합니다.
     */
    ApproveQuickPaymentPortResult approvePayment(ApproveQuickPaymentPortCommand command);
}
