package com.personal.marketnote.commerce.port.out.event;

import java.time.LocalDateTime;

/**
 * 자동 구매 확정 실패 이벤트 발행 포트
 *
 * @Author 성효빈
 * @Date 2026-10-24
 * @Description 스케줄러의 자동 확정 처리 실패 시 재시도용 이벤트를 발행합니다.
 */
public interface PublishOrderAutoConfirmFailedEventPort {

    /**
     * @param orderId  대상 주문 ID
     * @param buyerId  구매자 ID
     * @param reason   실패 원인 요약
     * @param failedAt 실패 시각
     * @Date 2026-10-24
     * @Author 성효빈
     * @Description 자동 확정 실패 이벤트를 Outbox 경유로 발행합니다.
     */
    void publish(Long orderId, Long buyerId, String reason, LocalDateTime failedAt);
}
