package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.port.out.payment.idempotency.DeletePaymentCancelIdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 결제 취소 멱등성 레코드 TTL 배치.
 *
 * <p>매일 03:00 (Asia/Seoul) 에 실행되어 24시간 이전에 생성된 레코드를 삭제한다.
 * TTL 은 사용자 재시도 윈도우와 운영 분석 여유를 반영한 값이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelIdempotencyCleanupScheduler {

    private static final long TTL_HOURS = 24L;

    private final DeletePaymentCancelIdempotencyPort deletePort;
    private final Clock commerceClock;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional(isolation = READ_COMMITTED)
    public void cleanupExpiredIdempotencyRecords() {
        LocalDateTime threshold = LocalDateTime.now(commerceClock).minusHours(TTL_HOURS);
        try {
            int deleted = deletePort.deleteOlderThan(threshold);
            log.info("결제 취소 멱등성 레코드 TTL 정리 완료 - threshold: {}, deleted: {}", threshold, deleted);
        } catch (Exception e) {
            log.error("결제 취소 멱등성 레코드 TTL 정리 실패 - threshold: {}", threshold, e);
        }
    }
}
