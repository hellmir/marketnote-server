package com.personal.marketnote.common.security.vendor;

import com.personal.marketnote.common.domain.exception.token.VendorVerificationFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VendorVerificationProcessor 테스트")
class VendorVerificationProcessorTest {

    @Nested
    @DisplayName("validateSignature(secret, text, signature) — HmacMD5")
    class HmacMD5Validation {

        private final String secret = "test-secret-key";
        private final String text = "userKey=user-1&amount=1000";

        @Test
        @DisplayName("올바른 서명이면 예외를 발생시키지 않는다")
        void validatesSuccessfully_whenSignatureMatches() throws Exception {
            // given
            String signature = computeHmacMd5(secret, text);

            // when & then
            assertThatCode(() -> VendorVerificationProcessor.validateSignature(secret, text, signature))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("서명이 일치하지 않으면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenSignatureMismatch() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature(secret, text, "invalid-sig"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("signed value 검증");
        }

        @Test
        @DisplayName("secret 이 null 이면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenSecretIsNull() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature(null, text, "any"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("유효하지 않은 인증키");
        }

        @Test
        @DisplayName("secret 이 blank 이면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenSecretIsBlank() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature("   ", text, "any"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("유효하지 않은 인증키");
        }

        @Test
        @DisplayName("text 가 null 이면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenTextIsNull() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature(secret, null, "any"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("유효하지 않은 인증키");
        }

        @Test
        @DisplayName("text 가 blank 이면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenTextIsBlank() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature(secret, "", "any"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("유효하지 않은 인증키");
        }

        private String computeHmacMd5(String secret, String text) throws Exception {
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacMD5"));
            byte[] result = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    @Nested
    @DisplayName("validateSignature(text, signature) — DigestUtils MD5")
    class Md5Validation {

        private final String text = "orderKey=order-1&status=APPROVED";

        @Test
        @DisplayName("올바른 MD5 다이제스트면 예외를 발생시키지 않는다")
        void validatesSuccessfully_whenSignatureMatches() {
            // given
            String signature = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));

            // when & then
            assertThatCode(() -> VendorVerificationProcessor.validateSignature(text, signature))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("서명이 일치하지 않으면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenSignatureMismatch() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature(text, "invalid-sig"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("signed value 검증");
        }

        @Test
        @DisplayName("text 가 null 이면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenTextIsNull() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature(null, "any"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("유효하지 않은 인증키");
        }

        @Test
        @DisplayName("text 가 blank 이면 VendorVerificationFailedException 을 던진다")
        void throwsException_whenTextIsBlank() {
            // when & then
            assertThatThrownBy(() -> VendorVerificationProcessor.validateSignature("   ", "any"))
                    .isInstanceOf(VendorVerificationFailedException.class)
                    .hasMessageContaining("유효하지 않은 인증키");
        }
    }
}
