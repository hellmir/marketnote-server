package com.personal.marketnote.common.outbox.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("OutboxAuditLogger 테스트")
class OutboxAuditLoggerTest {

    private final OutboxAuditLogger outboxAuditLogger = new OutboxAuditLogger();

    @Test
    @DisplayName("FAILED 전환 감사 로그를 기록한다")
    void logFailed_logsWithoutException() {
        // when & then
        assertThatCode(() -> outboxAuditLogger.logFailed(
                "commerce.payment.approved", "event-1", 5, "Kafka 전송 실패"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("resolve 감사 로그를 기록한다")
    void logResolve_logsWithoutException() {
        // when & then
        assertThatCode(() -> outboxAuditLogger.logResolve(
                "commerce.payment.approved", "event-1", "RETRY", "일시적 장애 복구"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("resolve 에러 감사 로그를 기록한다")
    void logResolveError_logsWithoutException() {
        // when & then
        assertThatCode(() -> outboxAuditLogger.logResolveError(
                "commerce.payment.approved", "event-1", "RETRY",
                new RuntimeException("전송 실패")
        )).doesNotThrowAnyException();
    }
}
