package com.personal.marketnote.product.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FasstoProductCodeGeneratorTest {
    private static final Pattern BASE62_PATTERN = Pattern.compile("^[0-9A-Za-z]+$");
    private static final int ENCODED_LENGTH = 22;

    @Nested
    @DisplayName("Encode")
    class Encode {

        @Test
        @DisplayName("UUID를 Base62로 인코딩하면 22자 문자열을 반환한다")
        void shouldReturn22CharStringForUuid() {
            UUID productKey = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            String encoded = FasstoProductCodeGenerator.encode(productKey);

            assertThat(encoded).hasSize(ENCODED_LENGTH);
        }

        @Test
        @DisplayName("인코딩 결과는 Base62 alphabet(0-9, A-Z, a-z) 문자만 포함한다")
        void shouldOnlyContainBase62AlphabetCharacters() {
            UUID productKey = UUID.fromString("01890d0a-1234-7000-89ab-0123456789ab");

            String encoded = FasstoProductCodeGenerator.encode(productKey);

            assertThat(BASE62_PATTERN.matcher(encoded).matches()).isTrue();
        }

        @Test
        @DisplayName("같은 UUID는 항상 같은 인코딩 결과를 반환한다 (결정론적)")
        void shouldBeDeterministic() {
            UUID productKey = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            String first = FasstoProductCodeGenerator.encode(productKey);
            String second = FasstoProductCodeGenerator.encode(productKey);
            String third = FasstoProductCodeGenerator.encode(productKey);

            assertThat(first).isEqualTo(second);
            assertThat(second).isEqualTo(third);
        }

        @Test
        @DisplayName("100건 무작위 UUID 인코딩 결과는 모두 22자 + Base62 + 충돌 없음")
        void shouldEncode100RandomUuidsWithoutCollision() {
            Set<String> encodedSet = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                String encoded = FasstoProductCodeGenerator.encode(UUID.randomUUID());
                assertThat(encoded).hasSize(ENCODED_LENGTH);
                assertThat(BASE62_PATTERN.matcher(encoded).matches()).isTrue();
                encodedSet.add(encoded);
            }
            assertThat(encodedSet).hasSize(100);
        }

        @Test
        @DisplayName("최소값(0 UUID) 인코딩 시 22자 0 패딩 문자열을 반환한다")
        void shouldReturnPaddedZerosForZeroUuid() {
            UUID zeroUuid = new UUID(0L, 0L);

            String encoded = FasstoProductCodeGenerator.encode(zeroUuid);

            assertThat(encoded).hasSize(ENCODED_LENGTH);
            assertThat(encoded).isEqualTo("0".repeat(ENCODED_LENGTH));
        }

        @Test
        @DisplayName("null UUID 인코딩 시 NullPointerException 전파")
        void shouldThrowNpeForNullProductKey() {
            assertThatThrownBy(() -> FasstoProductCodeGenerator.encode(null))
                .isInstanceOf(NullPointerException.class);
        }

        // 본 fixture는 fulfillment-adapters의 FasstoCustomerGoodsCodeEncoderTest와 동일한 expected를 공유한다.
        // 양쪽 알고리즘이 어긋나면 두 테스트가 동시에 실패하여 silent drift가 차단된다.
        // 알고리즘 수정 시 양쪽 테스트와 expected를 함께 갱신해야 한다.
        @Test
        @DisplayName("호환성 fixture: UUID 0x...0001 인코딩 결과는 정확히 '0000000000000000000001' 이다")
        void shouldEncodeFixtureUuidToKnownValue() {
            UUID fixtureUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

            String encoded = FasstoProductCodeGenerator.encode(fixtureUuid);

            assertThat(encoded).isEqualTo("0000000000000000000001");
        }
    }
}
