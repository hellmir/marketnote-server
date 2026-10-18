package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentApiResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorInfo;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentResponseHeader;
import com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FasstoInvalidAccessDetector 테스트")
class FasstoInvalidAccessDetectorTest {

    @Nested
    @DisplayName("isInvalidAccess(parsed, raw)")
    class IsInvalidAccessTest {

        @Test
        @DisplayName("errorInfo.errorCode가 INVALID_ACCESS면 true")
        void returnsTrueWhenErrorCodeMatches() {
            FulfillmentApiResponse parsed = new FulfillmentErrorResponse(
                    new FulfillmentResponseHeader("401", "denied", 0),
                    new FulfillmentErrorInfo("INVALID_ACCESS", "권한 없음", null)
            );
            assertThat(FasstoInvalidAccessDetector.isInvalidAccess(parsed, null)).isTrue();
        }

        @Test
        @DisplayName("errorInfo.errorMessage가 INVALID_ACCESS 문자열을 포함하면 true")
        void returnsTrueWhenErrorMessageContainsMarker() {
            FulfillmentApiResponse parsed = new FulfillmentErrorResponse(
                    new FulfillmentResponseHeader("500", "server err", 0),
                    new FulfillmentErrorInfo("ERR", "invalid_access token expired", null)
            );
            assertThat(FasstoInvalidAccessDetector.isInvalidAccess(parsed, null)).isTrue();
        }

        @Test
        @DisplayName("header.msg가 INVALID_ACCESS 문자열을 포함하면 true")
        void returnsTrueWhenHeaderMessageContainsMarker() {
            FulfillmentApiResponse parsed = new FulfillmentErrorResponse(
                    new FulfillmentResponseHeader("401", "INVALID_ACCESS: token expired", 0),
                    new FulfillmentErrorInfo(null, null, null)
            );
            assertThat(FasstoInvalidAccessDetector.isInvalidAccess(parsed, null)).isTrue();
        }

        @Test
        @DisplayName("대소문자 무시로 매칭된다")
        void caseInsensitiveMatching() {
            FulfillmentApiResponse parsed = new FulfillmentErrorResponse(
                    new FulfillmentResponseHeader("200", "ok", 0),
                    new FulfillmentErrorInfo("invalid_access", null, null)
            );
            assertThat(FasstoInvalidAccessDetector.isInvalidAccess(parsed, null)).isTrue();
        }

        @Test
        @DisplayName("파싱 실패로 parsed가 null이어도 raw body에서 감지된다")
        void fallbackToRawBodyWhenParsedIsNull() {
            ResponseEntity<String> rawResponse = ResponseEntity.ok(
                    "{\"header\":{\"code\":\"401\"},\"errorInfo\":{\"errorCode\":\"INVALID_ACCESS\"}}"
            );
            assertThat(FasstoInvalidAccessDetector.isInvalidAccess(null, rawResponse)).isTrue();
        }

        @Test
        @DisplayName("parsed/raw 모두에 INVALID_ACCESS 마커가 없으면 false")
        void returnsFalseWhenNoMarker() {
            FulfillmentApiResponse parsed = new FulfillmentErrorResponse(
                    new FulfillmentResponseHeader("500", "server error", 0),
                    new FulfillmentErrorInfo("INTERNAL_ERROR", "something failed", null)
            );
            ResponseEntity<String> rawResponse = ResponseEntity.internalServerError().body("{\"ok\":false}");
            assertThat(FasstoInvalidAccessDetector.isInvalidAccess(parsed, rawResponse)).isFalse();
        }

        @Test
        @DisplayName("parsed/raw 둘 다 null이면 false")
        void returnsFalseWhenBothNull() {
            assertThat(FasstoInvalidAccessDetector.isInvalidAccess(null, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("throwIfInvalidAccess(parsed, raw)")
    class ThrowIfInvalidAccessTest {

        @Test
        @DisplayName("INVALID_ACCESS 감지 시 FulfillmentInvalidAccessException을 throw한다")
        void throwsWhenDetected() {
            FulfillmentApiResponse parsed = new FulfillmentErrorResponse(
                    new FulfillmentResponseHeader("401", "denied", 0),
                    new FulfillmentErrorInfo("INVALID_ACCESS", "권한 없음", null)
            );
            assertThatThrownBy(() -> FasstoInvalidAccessDetector.throwIfInvalidAccess(parsed, null))
                    .isInstanceOf(FulfillmentInvalidAccessException.class);
        }

        @Test
        @DisplayName("INVALID_ACCESS 아니면 예외 없이 지나간다")
        void doesNothingWhenNotInvalidAccess() {
            FulfillmentApiResponse parsed = new FulfillmentErrorResponse(
                    new FulfillmentResponseHeader("500", "server error", 0),
                    new FulfillmentErrorInfo("INTERNAL_ERROR", "failed", null)
            );
            assertThatCode(() -> FasstoInvalidAccessDetector.throwIfInvalidAccess(parsed, null))
                    .doesNotThrowAnyException();
        }
    }
}
