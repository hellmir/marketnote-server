package com.personal.marketnote.commerce.port.out.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;

import java.util.Optional;

/**
 * 빠른결제 카드 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description 저장된 빠른결제 카드를 조회합니다.
 */
public interface FindQuickPaymentCardPort {
    /**
     * @param id     빠른결제 카드 ID
     * @param userId 사용자 ID
     * @return 활성 상태의 빠른결제 카드 {@link QuickPaymentCard}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description 사용자 소유의 활성 빠른결제 카드를 조회합니다.
     */
    Optional<QuickPaymentCard> findActiveByIdAndUserId(Long id, Long userId);
}
