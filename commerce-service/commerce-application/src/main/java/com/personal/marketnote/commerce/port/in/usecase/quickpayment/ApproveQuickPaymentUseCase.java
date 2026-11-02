package com.personal.marketnote.commerce.port.in.usecase.quickpayment;

import com.personal.marketnote.commerce.port.in.command.quickpayment.ApproveQuickPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.ApproveQuickPaymentResult;

/**
 * 빠른결제 결제 승인 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description 저장된 배치키로 KCP 서버 투 서버 결제 승인을 수행합니다.
 */
public interface ApproveQuickPaymentUseCase {
    /**
     * @param command 빠른결제 승인 커맨드
     * @return 승인 결과 {@link ApproveQuickPaymentResult}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description 배치키 기반 KCP 결제 승인을 수행합니다.
     */
    ApproveQuickPaymentResult approve(ApproveQuickPaymentCommand command);
}
