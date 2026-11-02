package com.personal.marketnote.common.security.hmac;

import com.personal.marketnote.common.security.hmac.exception.HmacSignatureMismatchException;
import com.personal.marketnote.common.security.hmac.exception.HmacTimestampExpiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacSignatureValidatorTest {
    private static final String SECRET_KEY = "test-hmac-secret-key";
    private static final String NONCE = "550e8400-e29b-41d4-a716-446655440000";
    private static final String HTTP_METHOD = "GET";
    private static final String REQUEST_PATH = "/api/v1/products";
    private static final long FIXED_TIME_MILLIS = 1710000000000L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.ofEpochMilli(FIXED_TIME_MILLIS), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("유효한 서명과 Timestamp로 검증하면 예외가 발생하지 않는다")
    void shouldPassValidation() {
        String timestamp = String.valueOf(FIXED_TIME_MILLIS);
        String signature = HmacSignatureGenerator.generate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThatCode(() ->
                HmacSignatureValidator.validate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH, signature, FIXED_CLOCK)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Timestamp가 허용 오차 범위 내(4분 59초 전)이면 검증을 통과한다")
    void shouldPassWhenTimestampWithinTolerance() {
        long fourMinutes59SecondsAgo = FIXED_TIME_MILLIS - (299 * 1000);
        String timestamp = String.valueOf(fourMinutes59SecondsAgo);
        String signature = HmacSignatureGenerator.generate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThatCode(() ->
                HmacSignatureValidator.validate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH, signature, FIXED_CLOCK)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Timestamp가 정확히 5분이면 검증을 통과한다")
    void shouldPassWhenTimestampExactlyAtBoundary() {
        long exactlyFiveMinutesAgo = FIXED_TIME_MILLIS - (300 * 1000);
        String timestamp = String.valueOf(exactlyFiveMinutesAgo);
        String signature = HmacSignatureGenerator.generate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThatCode(() ->
                HmacSignatureValidator.validate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH, signature, FIXED_CLOCK)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Timestamp가 5분 초과이면 HmacTimestampExpiredException이 발생한다")
    void shouldThrowWhenTimestampExceedsTolerance() {
        long sixMinutesAgo = FIXED_TIME_MILLIS - (360 * 1000);
        String timestamp = String.valueOf(sixMinutesAgo);
        String signature = HmacSignatureGenerator.generate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThatThrownBy(() ->
                HmacSignatureValidator.validate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH, signature, FIXED_CLOCK)
        ).isInstanceOf(HmacTimestampExpiredException.class);
    }

    @Test
    @DisplayName("미래 Timestamp가 5분 초과이면 HmacTimestampExpiredException이 발생한다")
    void shouldThrowWhenFutureTimestampExceedsTolerance() {
        long sixMinutesLater = FIXED_TIME_MILLIS + (360 * 1000);
        String timestamp = String.valueOf(sixMinutesLater);
        String signature = HmacSignatureGenerator.generate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThatThrownBy(() ->
                HmacSignatureValidator.validate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH, signature, FIXED_CLOCK)
        ).isInstanceOf(HmacTimestampExpiredException.class);
    }

    @Test
    @DisplayName("서명이 일치하지 않으면 HmacSignatureMismatchException이 발생한다")
    void shouldThrowWhenSignatureMismatch() {
        String timestamp = String.valueOf(FIXED_TIME_MILLIS);
        String invalidSignature = "00000000000000000000000000000000";

        assertThatThrownBy(() ->
                HmacSignatureValidator.validate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH, invalidSignature, FIXED_CLOCK)
        ).isInstanceOf(HmacSignatureMismatchException.class);
    }

    @Test
    @DisplayName("다른 시크릿 키로 생성된 서명은 검증에 실패한다")
    void shouldThrowWhenSignatureGeneratedWithDifferentKey() {
        String timestamp = String.valueOf(FIXED_TIME_MILLIS);
        String signature = HmacSignatureGenerator.generate("wrong-key", timestamp, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThatThrownBy(() ->
                HmacSignatureValidator.validate(SECRET_KEY, timestamp, NONCE, HTTP_METHOD, REQUEST_PATH, signature, FIXED_CLOCK)
        ).isInstanceOf(HmacSignatureMismatchException.class);
    }

    @Test
    @DisplayName("비숫자 Timestamp는 HmacTimestampExpiredException이 발생한다")
    void shouldThrowWhenTimestampIsNotNumeric() {
        assertThatThrownBy(() ->
                HmacSignatureValidator.validateTimestamp("not-a-number", FIXED_CLOCK)
        ).isInstanceOf(HmacTimestampExpiredException.class);
    }

    @Test
    @DisplayName("빈 Timestamp는 HmacTimestampExpiredException이 발생한다")
    void shouldThrowWhenTimestampIsEmpty() {
        assertThatThrownBy(() ->
                HmacSignatureValidator.validateTimestamp("", FIXED_CLOCK)
        ).isInstanceOf(HmacTimestampExpiredException.class);
    }
}
