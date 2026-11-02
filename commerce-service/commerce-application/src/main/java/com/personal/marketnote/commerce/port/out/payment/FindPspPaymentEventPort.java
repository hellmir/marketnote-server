package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;

import java.util.List;
import java.util.Optional;

/**
 * PSP 결제 이벤트 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description PSP 결제 이벤트 조회 관련 기능을 제공합니다.
 */
public interface FindPspPaymentEventPort {
    /**
     * @param orderKey 주문 키
     * @return PSP 결제 이벤트 {@link PspPaymentEvent}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 주문 키로 PSP 결제 이벤트를 조회합니다.
     */
    Optional<PspPaymentEvent> findByOrderKey(String orderKey);

    /**
     * UNKNOWN 상태의 PSP 결제 이벤트 전체를 조회합니다.
     *
     * @return UNKNOWN 상태의 PSP 결제 이벤트 목록
     */
    List<PspPaymentEvent> findAllByUnknownStatus();
}
