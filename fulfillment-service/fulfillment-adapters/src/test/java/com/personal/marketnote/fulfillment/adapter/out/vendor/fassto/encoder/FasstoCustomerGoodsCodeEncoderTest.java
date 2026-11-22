package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

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

        // 본 fixture는 product-domain의 FasstoProductCodeGeneratorTest와 동일한 expected를 공유한다.
        // 양쪽 알고리즘이 어긋나면 두 테스트가 동시에 실패하여 silent drift가 차단된다.
        // 알고리즘 수정 시 양쪽 테스트와 expected를 함께 갱신해야 한다.
        @Test
        @DisplayName("호환성 fixture: UUID 0x...0001 인코딩 결과는 정확히 '0000000000000000000001' 이다")
        void shouldEncodeFixtureUuidToKnownValue() {
            UUID fixtureUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

            String encoded = FasstoCustomerGoodsCodeEncoder.encode(fixtureUuid);

            assertThat(encoded).isEqualTo("0000000000000000000001");
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
