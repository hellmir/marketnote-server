package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FasstoCustomerGoodsCodeEncoder 테스트")
class FasstoCustomerGoodsCodeEncoderTest {

    private static final Pattern BASE62_PATTERN = Pattern.compile("^[0-9A-Za-z]+$");
    private static final int ENCODED_LENGTH = 22;

    @Nested
    @DisplayName("encode")
    class Encode {

        @Test
        @DisplayName("UUID를 인코딩하면 결과 길이가 정확히 22자다")
        void shouldReturnTwentyTwoCharacters() {
            UUID productKey = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);

            assertThat(encoded).hasSize(ENCODED_LENGTH);
        }

        @Test
        @DisplayName("UUID 인코딩 결과는 영숫자(0-9 A-Z a-z)만 포함한다")
        void shouldContainOnlyAlphanumeric() {
            UUID productKey = UUID.randomUUID();

            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);

            assertThat(BASE62_PATTERN.matcher(encoded).matches()).isTrue();
        }

        @Test
        @DisplayName("100건의 무작위 UUID를 인코딩해도 모두 22자 영숫자다")
        void shouldEncodeAllRandomUuidsToTwentyTwoAlphanumericCharacters() {
            IntStream.range(0, 100).forEach(i -> {
                UUID productKey = UUID.randomUUID();

                String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);

                assertThat(encoded).hasSize(ENCODED_LENGTH);
                assertThat(BASE62_PATTERN.matcher(encoded).matches()).isTrue();
            });
        }

        @Test
        @DisplayName("최상위 비트가 1인 UUID도 22자 영숫자로 인코딩된다 (BigInteger sign 처리)")
        void shouldEncodeUuidWithHighestBitSet() {
            UUID productKey = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);

            assertThat(encoded).hasSize(ENCODED_LENGTH);
            assertThat(BASE62_PATTERN.matcher(encoded).matches()).isTrue();
        }

        @Test
        @DisplayName("최하위 비트만 1인 UUID도 22자로 zero-pad되어 인코딩된다")
        void shouldZeroPadSmallValueUuid() {
            UUID productKey = UUID.fromString("00000000-0000-0000-0000-000000000001");

            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);

            assertThat(encoded).hasSize(ENCODED_LENGTH);
        }
    }

    @Nested
    @DisplayName("decode")
    class Decode {

        @Test
        @DisplayName("encode(uuid) 결과를 decode하면 원본 UUID를 반환한다 (라운드트립)")
        void shouldRoundtripSpecificUuid() {
            UUID original = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            UUID decoded = FasstoCustomerGoodsCodeEncoder.decode(FasstoCustomerGoodsCodeEncoder.encode(original));

            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("100건의 무작위 UUID에 대해 라운드트립이 보장된다")
        void shouldRoundtripRandomUuids() {
            IntStream.range(0, 100).forEach(i -> {
                UUID original = UUID.randomUUID();

                UUID decoded = FasstoCustomerGoodsCodeEncoder.decode(FasstoCustomerGoodsCodeEncoder.encode(original));

                assertThat(decoded).isEqualTo(original);
            });
        }

        @Test
        @DisplayName("22자 초과 입력(prefix 포함 27자 등)은 InvalidFasstoCustomerGoodsCodeLengthException을 던진다 (strict 매칭)")
        void shouldThrowOnLongerThanTwentyTwo() {
            UUID original = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(original);
            String prefixed = "94388" + encoded;

            assertThatThrownBy(() -> FasstoCustomerGoodsCodeEncoder.decode(prefixed))
                    .isInstanceOf(InvalidFasstoCustomerGoodsCodeLengthException.class);
        }

        @Test
        @DisplayName("36자 UUID 형태 입력(이전 마이그레이션 잔존)은 InvalidFasstoCustomerGoodsCodeLengthException을 던진다")
        void shouldThrowOnLegacyUuidString() {
            String legacyUuid = UUID.randomUUID().toString();

            assertThatThrownBy(() -> FasstoCustomerGoodsCodeEncoder.decode(legacyUuid))
                    .isInstanceOf(InvalidFasstoCustomerGoodsCodeLengthException.class);
        }

        @Test
        @DisplayName("null 입력은 InvalidFasstoCustomerGoodsCodeLengthException을 던진다")
        void shouldThrowOnNull() {
            assertThatThrownBy(() -> FasstoCustomerGoodsCodeEncoder.decode(null))
                    .isInstanceOf(InvalidFasstoCustomerGoodsCodeLengthException.class);
        }

        @Test
        @DisplayName("빈 문자열 입력은 InvalidFasstoCustomerGoodsCodeLengthException을 던진다")
        void shouldThrowOnBlank() {
            assertThatThrownBy(() -> FasstoCustomerGoodsCodeEncoder.decode(""))
                    .isInstanceOf(InvalidFasstoCustomerGoodsCodeLengthException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 21})
        @DisplayName("22자 미만 입력은 InvalidFasstoCustomerGoodsCodeLengthException을 던진다")
        void shouldThrowOnShorterThanTwentyTwo(int length) {
            String shortInput = "a".repeat(length);

            assertThatThrownBy(() -> FasstoCustomerGoodsCodeEncoder.decode(shortInput))
                    .isInstanceOf(InvalidFasstoCustomerGoodsCodeLengthException.class);
        }

        @Test
        @DisplayName("정확히 22자이지만 영숫자 외 문자 포함 시 InvalidFasstoCustomerGoodsCodeCharsetException을 던진다")
        void shouldThrowOnNonAlphanumericTwentyTwo() {
            String invalid = "550e8400-e29b-41d4-a71";

            assertThatThrownBy(() -> FasstoCustomerGoodsCodeEncoder.decode(invalid))
                    .isInstanceOf(InvalidFasstoCustomerGoodsCodeCharsetException.class);
        }
    }

    @Nested
    @DisplayName("decodeCustomerProductCode")
    class DecodeCustomerProductCode {

        @Test
        @DisplayName("null 입력은 그대로 null을 반환한다 (폴백 productCode 호환)")
        void shouldReturnNullForNullInput() {
            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열 입력은 그대로 빈 문자열을 반환한다 (폴백 productCode 호환)")
        void shouldReturnBlankForBlankInput() {
            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode("");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("22자 인코딩값은 디코딩된 UUID 문자열을 반환한다")
        void shouldDecodeEncodedValue() {
            UUID original = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(original);

            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode(encoded);

            assertThat(result).isEqualTo(original.toString());
        }

        @Test
        @DisplayName("22자 미만 입력(폴백 productCode echo)은 raw 통과한다")
        void shouldPassThroughShorterThanTwentyTwo() {
            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode("GOD001");

            assertThat(result).isEqualTo("GOD001");
        }

        @Test
        @DisplayName("36자 UUID 형태 입력(이전 마이그레이션 잔존)은 raw 통과한다")
        void shouldPassThroughLegacyUuidString() {
            String legacyUuid = UUID.randomUUID().toString();

            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode(legacyUuid);

            assertThat(result).isEqualTo(legacyUuid);
        }

        @Test
        @DisplayName("22자 초과 입력(prefix 포함 27자 등)은 raw 통과한다")
        void shouldPassThroughLongerThanTwentyTwo() {
            UUID original = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(original);
            String prefixed = "94388" + encoded;

            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode(prefixed);

            assertThat(result).isEqualTo(prefixed);
        }

        @Test
        @DisplayName("25자 영숫자 입력(가설적 vendor 자체 코드)은 raw 통과한다")
        void shouldPassThroughNonEncodedAlphanumeric() {
            String vendorCode = "ABCDEFGHIJ1234567890ABCDE";

            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode(vendorCode);

            assertThat(result).isEqualTo(vendorCode);
        }

        @Test
        @DisplayName("22자이지만 영숫자 외 문자 포함 시 raw 통과한다")
        void shouldPassThroughTwentyTwoCharsWithNonAlphanumeric() {
            String invalid = "550e8400-e29b-41d4-a71";

            String result = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode(invalid);

            assertThat(result).isEqualTo(invalid);
        }
    }

    @Nested
    @DisplayName("encodeOrFallback")
    class EncodeOrFallback {

        @Test
        @DisplayName("productKey가 있으면 22자 인코딩값을 반환한다")
        void shouldEncodeWhenProductKeyPresent() {
            UUID productKey = UUID.randomUUID();

            String result = FasstoCustomerGoodsCodeEncoder.encodeOrFallback(productKey, "GOD001");

            assertThat(result).hasSize(ENCODED_LENGTH);
            assertThat(result).isEqualTo(FasstoCustomerGoodsCodeEncoder.encode(productKey));
        }

        @Test
        @DisplayName("productKey가 null이면 fallback productCode를 그대로 반환한다")
        void shouldFallbackWhenProductKeyNull() {
            String result = FasstoCustomerGoodsCodeEncoder.encodeOrFallback(null, "GOD001");

            assertThat(result).isEqualTo("GOD001");
        }

        @Test
        @DisplayName("productKey와 fallback 둘 다 null이면 null을 반환한다")
        void shouldReturnNullWhenBothNull() {
            String result = FasstoCustomerGoodsCodeEncoder.encodeOrFallback(null, null);

            assertThat(result).isNull();
        }
    }
}
