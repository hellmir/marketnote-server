package com.personal.marketnote.common.security.hmac;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

class HmacServiceAuthHeaderBuilderTest {
    private static final String SECRET_KEY = "test-hmac-secret-key";
    private static final long FIXED_TIME_MILLIS = 1710000000000L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.ofEpochMilli(FIXED_TIME_MILLIS), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("applyHeaders 호출 시 3개의 HMAC 헤더가 설정된다")
    void shouldApplyThreeHmacHeaders() {
        HmacServiceAuthHeaderBuilder builder = createBuilder();
        HttpHeaders headers = new HttpHeaders();

        builder.applyHeaders(headers, "GET", "/api/v1/products");

        assertThat(headers.getFirst(HEADER_SIGNATURE)).isNotNull();
        assertThat(headers.getFirst(HEADER_TIMESTAMP)).isEqualTo(String.valueOf(FIXED_TIME_MILLIS));
        assertThat(headers.getFirst(HEADER_NONCE)).isNotNull();
    }

    @Test
    @DisplayName("생성된 서명은 검증 가능하다")
    void shouldGenerateVerifiableSignature() {
        HmacServiceAuthHeaderBuilder builder = createBuilder();
        HttpHeaders headers = new HttpHeaders();

        builder.applyHeaders(headers, "POST", "/api/v1/users/1/points");

        String signature = headers.getFirst(HEADER_SIGNATURE);
        String timestamp = headers.getFirst(HEADER_TIMESTAMP);
        String nonce = headers.getFirst(HEADER_NONCE);

        String expectedSignature = HmacSignatureGenerator.generate(
                SECRET_KEY, timestamp, nonce, "POST", "/api/v1/users/1/points");
        assertThat(signature).isEqualTo(expectedSignature);
    }

    @Test
    @DisplayName("매 호출마다 서로 다른 Nonce가 생성된다")
    void shouldGenerateUniqueNoncePerCall() {
        HmacServiceAuthHeaderBuilder builder = createBuilder();
        HttpHeaders headers1 = new HttpHeaders();
        HttpHeaders headers2 = new HttpHeaders();

        builder.applyHeaders(headers1, "GET", "/api/v1/products");
        builder.applyHeaders(headers2, "GET", "/api/v1/products");

        assertThat(headers1.getFirst(HEADER_NONCE)).isNotEqualTo(headers2.getFirst(HEADER_NONCE));
    }

    private HmacServiceAuthHeaderBuilder createBuilder() {
        return new HmacServiceAuthHeaderBuilder(SECRET_KEY, FIXED_CLOCK);
    }
}
