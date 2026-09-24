package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RandomCodeGeneratorTest {

    @Nested
    @DisplayName("generateProductKey")
    class GenerateProductKeyTest {

        @Test
        @DisplayName("generateProductKey는 null이 아닌 UUID를 반환한다")
        void shouldReturnNonNullUuid() {
            UUID productKey = RandomCodeGenerator.generateProductKey();

            assertThat(productKey).isNotNull();
        }

        @Test
        @DisplayName("generateProductKey는 호출할 때마다 서로 다른 UUID를 반환한다")
        void shouldReturnUniqueUuidPerCall() {
            int count = 1000;
            Set<UUID> generated = new HashSet<>();

            for (int i = 0; i < count; i++) {
                generated.add(RandomCodeGenerator.generateProductKey());
            }

            assertThat(generated).hasSize(count);
        }

        @Test
        @DisplayName("generateProductKey가 반환한 UUID는 버전 7이다")
        void shouldReturnUuidVersion7() {
            UUID productKey = RandomCodeGenerator.generateProductKey();

            assertThat(productKey.version()).isEqualTo(7);
        }
    }
}
