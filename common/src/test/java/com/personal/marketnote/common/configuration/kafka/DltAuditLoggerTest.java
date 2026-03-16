package com.personal.marketnote.common.configuration.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("DltAuditLogger 테스트")
class DltAuditLoggerTest {

    private final DltAuditLogger dltAuditLogger = new DltAuditLogger();

    @Test
    @DisplayName("재처리 시작 감사 로그를 기록한다")
    void logReprocessStart_logsWithoutException() {
        // when & then
        assertThatCode(() -> dltAuditLogger.logReprocessStart(
                "commerce.order.payment-completed", "admin@personal.com"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("재처리 완료 감사 로그를 기록한다")
    void logReprocessComplete_logsWithoutException() {
        // when & then
        assertThatCode(() -> dltAuditLogger.logReprocessComplete(
                "commerce.order.payment-completed", "admin@personal.com", 5, 1
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("재처리 에러 감사 로그를 기록한다")
    void logReprocessError_logsWithoutException() {
        // when & then
        assertThatCode(() -> dltAuditLogger.logReprocessError(
                "commerce.order.payment-completed", "admin@personal.com",
                new RuntimeException("Kafka 연결 실패")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DLT 메시지 조회 감사 로그를 기록한다")
    void logQuery_logsWithoutException() {
        // when & then
        assertThatCode(() -> dltAuditLogger.logQuery(
                "commerce.order.payment-completed", 100, "admin@personal.com"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DLT 토픽 요약 조회 감사 로그를 기록한다")
    void logSummaryQuery_logsWithoutException() {
        // when & then
        assertThatCode(() -> dltAuditLogger.logSummaryQuery("admin@personal.com"))
                .doesNotThrowAnyException();
    }
}
