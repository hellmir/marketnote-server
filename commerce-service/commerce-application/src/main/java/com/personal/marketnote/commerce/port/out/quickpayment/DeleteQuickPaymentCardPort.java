package com.personal.marketnote.commerce.port.out.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;

/**
 * 빠른결제 카드 삭제(비활성화) 포트
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description 빠른결제 카드를 DB에서 비활성화합니다.
 */
public interface DeleteQuickPaymentCardPort {
    /**
     * @param quickPaymentCard 비활성화할 빠른결제 카드
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description 빠른결제 카드의 상태를 INACTIVE로 변경합니다.
     */
    void deactivate(QuickPaymentCard quickPaymentCard);
}
