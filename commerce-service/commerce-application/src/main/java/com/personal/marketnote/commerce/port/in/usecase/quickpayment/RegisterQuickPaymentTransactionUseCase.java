package com.personal.marketnote.commerce.port.in.usecase.quickpayment;

import com.personal.marketnote.commerce.port.in.command.quickpayment.RegisterQuickPaymentTransactionCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.RegisterQuickPaymentTransactionResult;

/**
 * 빠른결제 거래 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description 빠른결제 카드 등록을 위한 KCP 거래 등록 기능을 제공합니다.
 */
public interface RegisterQuickPaymentTransactionUseCase {
    /**
     * @param command 빠른결제 거래 등록 커맨드
     * @return 거래 등록 결과 {@link RegisterQuickPaymentTransactionResult}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description KCP에 빠른결제 카드 등록용 거래를 등록합니다.
     */
    RegisterQuickPaymentTransactionResult register(RegisterQuickPaymentTransactionCommand command);
}
