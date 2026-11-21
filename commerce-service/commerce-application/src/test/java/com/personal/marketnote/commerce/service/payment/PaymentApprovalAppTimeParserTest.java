package com.personal.marketnote.commerce.service.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentApprovalAppTimeParser 테스트")
class PaymentApprovalAppTimeParserTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-21T03:00:00Z"), ZONE
    );

    private final PaymentApprovalAppTimeParser parser = new PaymentApprovalAppTimeParser(FIXED_CLOCK);

    @Test
    @DisplayName("정상 형식 yyyyMMddHHmmss는 LocalDateTime으로 파싱된다")
    void shouldParseValidFormat() {
        LocalDateTime result = parser.parse("20260210153000");

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 2, 10, 15, 30, 0));
    }

    @Test
    @DisplayName("appTime이 null이면 Clock 기반 현재 시각을 반환한다")
    void shouldFallbackToNowWhenNull() {
        LocalDateTime result = parser.parse(null);

        assertThat(result).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    @DisplayName("appTime이 빈 문자열이면 Clock 기반 현재 시각을 반환한다")
    void shouldFallbackToNowWhenBlank() {
        LocalDateTime result = parser.parse("");

        assertThat(result).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    @DisplayName("appTime이 잘못된 형식이면 Clock 기반 현재 시각을 반환한다 (분류 C 폴백)")
    void shouldFallbackToNowWhenInvalidFormat() {
        LocalDateTime result = parser.parse("invalid-format");

        assertThat(result).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    @DisplayName("appTime이 자릿수 부족이면 Clock 기반 현재 시각을 반환한다")
    void shouldFallbackToNowWhenTruncated() {
        LocalDateTime result = parser.parse("20262");

        assertThat(result).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    @DisplayName("appTime이 비숫자 문자 포함이면 Clock 기반 현재 시각을 반환한다")
    void shouldFallbackToNowWhenContainsNonDigit() {
        LocalDateTime result = parser.parse("2026021015300X");

        assertThat(result).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }
}
