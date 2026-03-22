package com.personal.marketnote.common.security.hmac;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HmacSignatureGeneratorTest {
    private static final String SECRET_KEY = "test-hmac-secret-key";
    private static final String TIMESTAMP = "1710000000000";
    private static final String NONCE = "550e8400-e29b-41d4-a716-446655440000";
    private static final String HTTP_METHOD = "GET";
    private static final String REQUEST_PATH = "/api/v1/products";

    @Test
    @DisplayName("동일한 입력으로 서명을 생성하면 항상 동일한 결과를 반환한다")
    void shouldGenerateSameSignatureForSameInput() {
        String signature1 = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, HTTP_METHOD, REQUEST_PATH);
        String signature2 = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThat(signature1).isEqualTo(signature2);
    }

    @Test
    @DisplayName("서로 다른 시크릿 키로 서명을 생성하면 다른 결과를 반환한다")
    void shouldGenerateDifferentSignatureForDifferentKey() {
        String signature1 = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, HTTP_METHOD, REQUEST_PATH);
        String signature2 = HmacSignatureGenerator.generate("different-key", TIMESTAMP, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThat(signature1).isNotEqualTo(signature2);
    }

    @Test
    @DisplayName("서로 다른 HTTP 메서드로 서명을 생성하면 다른 결과를 반환한다")
    void shouldGenerateDifferentSignatureForDifferentMethod() {
        String signatureGet = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, "GET", REQUEST_PATH);
        String signaturePost = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, "POST", REQUEST_PATH);

        assertThat(signatureGet).isNotEqualTo(signaturePost);
    }

    @Test
    @DisplayName("서로 다른 요청 경로로 서명을 생성하면 다른 결과를 반환한다")
    void shouldGenerateDifferentSignatureForDifferentPath() {
        String signature1 = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, HTTP_METHOD, "/api/v1/products");
        String signature2 = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, HTTP_METHOD, "/api/v1/users");

        assertThat(signature1).isNotEqualTo(signature2);
    }

    @Test
    @DisplayName("서명은 32자리 Hex 문자열로 반환된다")
    void shouldReturnHexStringWith32Characters() {
        String signature = HmacSignatureGenerator.generate(SECRET_KEY, TIMESTAMP, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThat(signature).hasSize(32);
        assertThat(signature).matches("[0-9a-f]{32}");
    }

    @Test
    @DisplayName("서명 입력값은 콜론으로 구분되어 조합된다")
    void shouldBuildSigningInputWithColonDelimiter() {
        String signingInput = HmacSignatureGenerator.buildSigningInput(TIMESTAMP, NONCE, HTTP_METHOD, REQUEST_PATH);

        assertThat(signingInput).isEqualTo(TIMESTAMP + ":" + NONCE + ":" + HTTP_METHOD + ":" + REQUEST_PATH);
    }

    @Test
    @DisplayName("constantTimeEquals는 동일한 문자열에 대해 true를 반환한다")
    void shouldReturnTrueForEqualStrings() {
        assertThat(HmacSignatureGenerator.constantTimeEquals("abc123", "abc123")).isTrue();
    }

    @Test
    @DisplayName("constantTimeEquals는 다른 문자열에 대해 false를 반환한다")
    void shouldReturnFalseForDifferentStrings() {
        assertThat(HmacSignatureGenerator.constantTimeEquals("abc123", "abc456")).isFalse();
    }

    @Test
    @DisplayName("constantTimeEquals는 null 입력에 대해 false를 반환한다")
    void shouldReturnFalseForNullInput() {
        assertThat(HmacSignatureGenerator.constantTimeEquals(null, "abc")).isFalse();
        assertThat(HmacSignatureGenerator.constantTimeEquals("abc", null)).isFalse();
        assertThat(HmacSignatureGenerator.constantTimeEquals(null, null)).isFalse();
    }

    @Test
    @DisplayName("constantTimeEquals는 길이가 다른 문자열에 대해 false를 반환한다")
    void shouldReturnFalseForDifferentLengthStrings() {
        assertThat(HmacSignatureGenerator.constantTimeEquals("abc", "abcd")).isFalse();
    }
}
