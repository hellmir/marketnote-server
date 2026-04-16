package com.personal.marketnote.commerce.port.out.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;

/**
 * 빠른결제 카드 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description 빠른결제 카드를 DB에 저장합니다.
 */
public interface SaveQuickPaymentCardPort {
    /**
     * @param quickPaymentCard 저장할 빠른결제 카드 도메인
     * @return 저장된 빠른결제 카드 {@link QuickPaymentCard}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description 빠른결제 카드를 DB에 저장합니다.
     */
    QuickPaymentCard save(QuickPaymentCard quickPaymentCard);
}
