package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;

import java.util.Optional;
import java.util.UUID;

public interface FindPaymentPort {
    /**
     * @param orderId 주문 ID
     * @return 결제 정보 {@link Payment}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 주문 ID로 결제 정보를 조회합니다.
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * @param orderKey 주문 키
     * @return 결제 정보 {@link Payment}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 주문 키로 결제 정보를 조회합니다.
     */
    Optional<Payment> findByOrderKey(UUID orderKey);
}
