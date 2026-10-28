package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.reward.configuration.GifticonPinProperties;
import com.personal.marketnote.reward.domain.exception.GifticonPinDecryptionFailedException;
import com.personal.marketnote.reward.domain.exception.GifticonPinEncryptKeyNoValueException;
import com.personal.marketnote.reward.domain.exception.InvalidGifticonPinEncryptKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GifticonPinEncryptAdapter 테스트")
class GifticonPinEncryptAdapterTest {

    private static final String AES_256_KEY = "0123456789ABCDEF0123456789ABCDEF";
    private static final byte[] GCM_MAGIC = "GCM1".getBytes(StandardCharsets.US_ASCII);

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
    @DisplayName("GCM 포맷 (AEAD)")
    class GcmFormat {

        @Test
        @DisplayName("GCM으로 암호화한 암호문을 복호화하면 원본과 동일하다")
        void gcmRoundTripReturnsOriginal() {
            // given
            String plainPin = "PIN1234";

            // when
            String encrypted = adapter.encrypt(plainPin);
            String decrypted = adapter.decrypt(encrypted);

            // then
            assertThat(decrypted).isEqualTo(plainPin);
        }

        @Test
        @DisplayName("GCM 암호문의 첫 4바이트는 매직 프리픽스 GCM1이다")
        void gcmCiphertextStartsWithMagicPrefix() {
            // given
            String encrypted = adapter.encrypt("test-pin");

            // when
            byte[] decoded = Base64.getDecoder().decode(encrypted);

            // then
            assertThat(decoded).hasSizeGreaterThanOrEqualTo(4);
            byte[] prefix = new byte[4];
            System.arraycopy(decoded, 0, prefix, 0, 4);
            assertThat(prefix).isEqualTo(GCM_MAGIC);
        }

        @Test
        @DisplayName("GCM 암호문 바이트 레이아웃은 MAGIC(4) + IV(12) + Ciphertext+Tag(16바이트 이상)이다")
        void gcmCiphertextLayoutMatchesSpec() {
            // given
            String encrypted = adapter.encrypt("A");

            // when
            byte[] decoded = Base64.getDecoder().decode(encrypted);

            // then
            // MAGIC(4) + IV(12) + Ciphertext(1) + Tag(16) = 33
            assertThat(decoded).hasSize(4 + 12 + 1 + 16);
        }

        @Test
        @DisplayName("GCM 암호문의 태그(마지막 16바이트)를 변조하면 GifticonPinDecryptionFailedException이 발생한다")
        void gcmDecryptFailsWhenTagTampered() {
            // given
            String encrypted = adapter.encrypt("PIN1234");
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            decoded[decoded.length - 1] ^= (byte) 0x01;
            String tampered = Base64.getEncoder().encodeToString(decoded);

            // when & then
            assertThatThrownBy(() -> adapter.decrypt(tampered))
                    .isInstanceOf(GifticonPinDecryptionFailedException.class);
        }

        @Test
        @DisplayName("GCM 암호문의 IV 바이트를 변조하면 GifticonPinDecryptionFailedException이 발생한다")
        void gcmDecryptFailsWhenIvTampered() {
            // given
            String encrypted = adapter.encrypt("PIN1234");
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            // IV는 MAGIC(4) 이후 12바이트
            decoded[4] ^= (byte) 0x01;
            String tampered = Base64.getEncoder().encodeToString(decoded);

            // when & then
            assertThatThrownBy(() -> adapter.decrypt(tampered))
                    .isInstanceOf(GifticonPinDecryptionFailedException.class);
        }

        @Test
        @DisplayName("GCM 암호문의 본문(Ciphertext) 바이트를 변조하면 GifticonPinDecryptionFailedException이 발생한다")
        void gcmDecryptFailsWhenCiphertextTampered() {
            // given
            String encrypted = adapter.encrypt("PIN1234-안녕");
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            // MAGIC(4) + IV(12) = 16 이후가 ciphertext 시작
            decoded[16] ^= (byte) 0x01;
            String tampered = Base64.getEncoder().encodeToString(decoded);

            // when & then
            assertThatThrownBy(() -> adapter.decrypt(tampered))
                    .isInstanceOf(GifticonPinDecryptionFailedException.class);
        }

        @Test
        @DisplayName("GCM 암호문 길이(PIN '1234')가 pin_no VARCHAR(2048) 제한 내에 들어온다")
        void gcmCiphertextLengthFitsWithinColumnLimit() {
            // given
            String plainPin = "1234";

            // when
            String encrypted = adapter.encrypt(plainPin);

            // then
            assertThat(encrypted.length()).isLessThanOrEqualTo(2048);
            // 실제 크기: base64(4+12+4+16=36) = 48자
            assertThat(encrypted.length()).isLessThanOrEqualTo(48);
        }
    }

    @Nested
    @DisplayName("레거시 CBC 차단")
    class LegacyCbcRejection {

        @Test
        @DisplayName("GCM1 매직 프리픽스가 없는 (레거시 CBC-like) 암호문은 복호화 실패 예외를 던진다")
        void rejectsCiphertextWithoutGcmMagic() throws Exception {
            // given
            String legacyEncrypted = legacyCbcEncrypt("LEGACY-PIN-1234", AES_256_KEY);

            // when & then
            assertThatThrownBy(() -> adapter.decrypt(legacyEncrypted))
                    .isInstanceOf(GifticonPinDecryptionFailedException.class)
                    .hasNoCause();
        }

        /**
         * 기존 AES/CBC/PKCS5Padding 방식 암호화를 재현한 헬퍼.
         * CBC 경로 제거 후 "GCM1 프리픽스 없음 → 차단"을 검증하기 위해서만 사용한다.
         */
        private String legacyCbcEncrypt(String plainPin, String key) throws Exception {
            byte[] iv = new byte[16];
            for (int i = 0; i < iv.length; i++) {
                iv[i] = (byte) i;
            }
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainPin.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
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

    @Nested
    @DisplayName("키 길이 검증")
    class KeyLengthValidation {

        @Test
        @DisplayName("16바이트 키(AES-128)로 어댑터가 정상 생성되고 encrypt/decrypt 왕복이 성공한다")
        void acceptsAes128Key() {
            // given
            GifticonPinEncryptAdapter adapter = buildAdapter("0123456789ABCDEF");

            // when
            String encrypted = adapter.encrypt("test-pin");
            String decrypted = adapter.decrypt(encrypted);

            // then
            assertThat(decrypted).isEqualTo("test-pin");
        }

        @Test
        @DisplayName("24바이트 키(AES-192)로 어댑터가 정상 생성되고 encrypt/decrypt 왕복이 성공한다")
        void acceptsAes192Key() {
            // given
            GifticonPinEncryptAdapter adapter = buildAdapter("0123456789ABCDEF01234567");

            // when
            String encrypted = adapter.encrypt("test-pin");
            String decrypted = adapter.decrypt(encrypted);

            // then
            assertThat(decrypted).isEqualTo("test-pin");
        }

        @Test
        @DisplayName("32바이트 키(AES-256)로 어댑터가 정상 생성되고 encrypt/decrypt 왕복이 성공한다")
        void acceptsAes256Key() {
            // given
            GifticonPinEncryptAdapter adapter = buildAdapter("0123456789ABCDEF0123456789ABCDEF");

            // when
            String encrypted = adapter.encrypt("test-pin");
            String decrypted = adapter.decrypt(encrypted);

            // then
            assertThat(decrypted).isEqualTo("test-pin");
        }

        @Test
        @DisplayName("15바이트 키이면 InvalidGifticonPinEncryptKeyException이 발생하고 메시지에 actualLength=15가 포함된다")
        void rejectsFifteenByteKey() {
            assertThatThrownBy(() -> buildAdapter("0123456789ABCDE"))
                    .isInstanceOf(InvalidGifticonPinEncryptKeyException.class)
                    .hasMessageContaining("actualLength=15");
        }

        @Test
        @DisplayName("17바이트 키이면 InvalidGifticonPinEncryptKeyException이 발생하고 메시지에 actualLength=17이 포함된다")
        void rejectsSeventeenByteKey() {
            assertThatThrownBy(() -> buildAdapter("0123456789ABCDEF0"))
                    .isInstanceOf(InvalidGifticonPinEncryptKeyException.class)
                    .hasMessageContaining("actualLength=17");
        }

        @Test
        @DisplayName("21바이트 키이면 InvalidGifticonPinEncryptKeyException이 발생하고 메시지에 actualLength=21이 포함된다 (기존 기본값 부적절 증명)")
        void rejectsLegacyDefaultKey() {
            assertThatThrownBy(() -> buildAdapter("dev-pin-key-change-me"))
                    .isInstanceOf(InvalidGifticonPinEncryptKeyException.class)
                    .hasMessageContaining("actualLength=21");
        }

        @Test
        @DisplayName("빈 문자열 키이면 GifticonPinEncryptKeyNoValueException이 발생한다")
        void rejectsEmptyKey() {
            assertThatThrownBy(() -> buildAdapter(""))
                    .isInstanceOf(GifticonPinEncryptKeyNoValueException.class);
        }

        @Test
        @DisplayName("null 키이면 GifticonPinEncryptKeyNoValueException이 발생한다")
        void rejectsNullKey() {
            assertThatThrownBy(() -> buildAdapter(null))
                    .isInstanceOf(GifticonPinEncryptKeyNoValueException.class);
        }
    }

    private static GifticonPinEncryptAdapter buildAdapter(String key) {
        GifticonPinProperties properties = new GifticonPinProperties();
        properties.setEncryptKey(key);
        return new GifticonPinEncryptAdapter(properties);
    }
}
