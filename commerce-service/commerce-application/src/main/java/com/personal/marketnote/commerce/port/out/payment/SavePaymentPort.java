package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;

/**
 * 결제 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description 결제 저장 기능을 제공합니다.
 */
public interface SavePaymentPort {
    /**
     * @param payment 결제 정보
     * @return 저장된 결제 정보 {@link Payment}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 정보를 저장합니다.
     */
    Payment save(Payment payment);
}
