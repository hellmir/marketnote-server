package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;

public interface UpdatePspPaymentEventPort {
    /**
     * @param event PSP 결제 이벤트
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description PSP 결제 이벤트를 업데이트합니다.
     */
    void update(PspPaymentEvent event);
}
