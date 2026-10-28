package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.PaymentCancelIdempotencyCleanupProperties;
import com.personal.marketnote.commerce.port.out.payment.idempotency.DeletePaymentCancelIdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 결제 취소 멱등성 레코드 TTL 배치.
 *
 * <p>TTL(시간) 및 cron 표현식은 {@link PaymentCancelIdempotencyCleanupProperties}
 * 를 통해 외부 설정(application.yml / 환경 변수)으로 관리된다. 기본값은
 * 매일 03:00 (Asia/Seoul) 실행 + 24시간 이전 레코드 삭제이며, 운영 환경별로
 * 재정의할 수 있다. TTL 은 사용자 재시도 윈도우와 운영 분석 여유를 반영한 값이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(PaymentCancelIdempotencyCleanupProperties.class)
public class PaymentCancelIdempotencyCleanupScheduler {

    private final DeletePaymentCancelIdempotencyPort deletePort;
    private final Clock commerceClock;
    private final PaymentCancelIdempotencyCleanupProperties properties;

    @Scheduled(cron = "${commerce.payment.idempotency-cleanup.cron}", zone = "Asia/Seoul")
    @Transactional(isolation = READ_COMMITTED)
    public void cleanupExpiredIdempotencyRecords() {
        LocalDateTime threshold = LocalDateTime.now(commerceClock).minusHours(properties.getTtlHours());
        try {
            int deleted = deletePort.deleteOlderThan(threshold);
            log.info("결제 취소 멱등성 레코드 TTL 정리 완료 - threshold: {}, deleted: {}", threshold, deleted);
        } catch (Exception e) {
            log.error("결제 취소 멱등성 레코드 TTL 정리 실패 - threshold: {}", threshold, e);
        }
    }
}
