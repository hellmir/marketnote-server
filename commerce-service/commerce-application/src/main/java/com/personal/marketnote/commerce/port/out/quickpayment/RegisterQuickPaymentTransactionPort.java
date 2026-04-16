package com.personal.marketnote.commerce.port.out.quickpayment;

/**
 * 빠른결제 거래 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description KCP에 빠른결제 카드 등록용 거래를 등록합니다.
 */
public interface RegisterQuickPaymentTransactionPort {
    /**
     * @param command 거래 등록 요청 정보 {@link RegisterQuickPaymentTransactionPortCommand}
     * @return 거래 등록 결과 {@link RegisterQuickPaymentTransactionPortResult}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description KCP에 빠른결제 거래를 등록합니다.
     */
    RegisterQuickPaymentTransactionPortResult registerTransaction(RegisterQuickPaymentTransactionPortCommand command);
}
