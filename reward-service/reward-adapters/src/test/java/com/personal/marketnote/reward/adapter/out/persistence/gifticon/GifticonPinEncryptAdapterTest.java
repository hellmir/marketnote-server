package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.reward.configuration.GifticonPinProperties;
import com.personal.marketnote.reward.domain.exception.GifticonPinDecryptionFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GifticonPinEncryptAdapter 테스트")
class GifticonPinEncryptAdapterTest {

    private static final String AES_256_KEY = "0123456789ABCDEF0123456789ABCDEF";

    private final GifticonPinEncryptAdapter adapter = buildAdapter(AES_256_KEY);

    @Nested
    @DisplayName("encrypt / decrypt 왕복")
    class EncryptDecryptRoundTrip {

        @Test
        @DisplayName("평문 PIN을 암호화한 뒤 복호화하면 원본과 동일하다")
        void roundTripReturnsOriginal() {
            // given
            String plainPin = "1234-5678-9012-3456";

            // when
            String encrypted = adapter.encrypt(plainPin);
            String decrypted = adapter.decrypt(encrypted);

            // then
            assertThat(decrypted).isEqualTo(plainPin);
        }

        @Test
        @DisplayName("같은 평문을 두 번 암호화해도 결과는 매번 다르다 (랜덤 IV)")
        void encryptProducesDifferentCiphertextEachTime() {
            // given
            String plainPin = "동일한-핀";

            // when
            String first = adapter.encrypt(plainPin);
            String second = adapter.encrypt(plainPin);

            // then
            assertThat(first).isNotEqualTo(second);
            assertThat(adapter.decrypt(first)).isEqualTo(plainPin);
            assertThat(adapter.decrypt(second)).isEqualTo(plainPin);
        }

        @Test
        @DisplayName("한글/특수문자 혼합 PIN도 왕복 후 동일하다")
        void roundTripSupportsUtf8() {
            // given
            String plainPin = "핀-!@#$%-1234-🎁";

            // when
            String encrypted = adapter.encrypt(plainPin);
            String decrypted = adapter.decrypt(encrypted);

            // then
            assertThat(decrypted).isEqualTo(plainPin);
        }

        @Test
        @DisplayName("암호문은 Base64로 인코딩된다")
        void encryptProducesBase64String() {
            // when
            String encrypted = adapter.encrypt("test-pin");

            // then
            assertThat(encrypted).matches("^[A-Za-z0-9+/]+={0,2}$");
        }
    }

    @Nested
    @DisplayName("복호화 실패")
    class DecryptFailure {

        @Test
        @DisplayName("잘못된 Base64 문자열 복호화 시 GifticonPinDecryptionFailedException이 발생한다")
        void throwsWhenDecryptingInvalidBase64() {
            assertThatThrownBy(() -> adapter.decrypt("!!!-not-valid-base64-!!!"))
                    .isInstanceOf(GifticonPinDecryptionFailedException.class);
        }

        @Test
        @DisplayName("다른 키로 암호화된 값을 복호화하면 GifticonPinDecryptionFailedException이 발생한다")
        void throwsWhenDecryptingWithDifferentKey() {
            // given
            GifticonPinEncryptAdapter otherAdapter = buildAdapter("FEDCBA9876543210FEDCBA9876543210");
            String encrypted = otherAdapter.encrypt("비밀-핀");

            // when & then
            assertThatThrownBy(() -> adapter.decrypt(encrypted))
                    .isInstanceOf(GifticonPinDecryptionFailedException.class);
        }
    }

    private static GifticonPinEncryptAdapter buildAdapter(String key) {
        GifticonPinProperties properties = new GifticonPinProperties();
        properties.setEncryptKey(key);
        return new GifticonPinEncryptAdapter(properties);
    }
}
