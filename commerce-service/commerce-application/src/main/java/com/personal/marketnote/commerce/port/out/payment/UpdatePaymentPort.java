package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;

public interface UpdatePaymentPort {
    /**
     * @param payment 결제 정보
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 정보를 업데이트합니다.
     */
    void update(Payment payment);
}
