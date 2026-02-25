package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;

import java.util.Optional;

public interface FindPspPaymentEventPort {
    /**
     * @param orderKey 주문 키
     * @return PSP 결제 이벤트 {@link PspPaymentEvent}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 주문 키로 PSP 결제 이벤트를 조회합니다.
     */
    Optional<PspPaymentEvent> findByOrderKey(String orderKey);
}
