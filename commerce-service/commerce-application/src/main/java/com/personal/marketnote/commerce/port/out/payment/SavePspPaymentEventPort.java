package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;

/**
 * PSP 결제 이벤트 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description PSP 결제 이벤트 저장 기능을 제공합니다.
 */
public interface SavePspPaymentEventPort {
    /**
     * @param event PSP 결제 이벤트
     * @return 저장된 PSP 결제 이벤트 {@link PspPaymentEvent}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description PSP 결제 이벤트를 저장합니다.
     */
    PspPaymentEvent save(PspPaymentEvent event);
}
