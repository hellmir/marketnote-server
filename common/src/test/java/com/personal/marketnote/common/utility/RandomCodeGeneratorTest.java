package com.personal.marketnote.common.utility;

import com.personal.marketnote.common.kafka.event.EventEnvelope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RandomCodeGeneratorTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-20T00:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("generateOrderKey()는 UUID V7을 반환한다")
    void generateOrderKey_returnsUuidV7() {
        UUID orderKey = RandomCodeGenerator.generateOrderKey();

        assertThat(orderKey.version()).isEqualTo(7);
    }

    @Test
    @DisplayName("generatePostKey()는 UUID V7을 반환한다")
    void generatePostKey_returnsUuidV7() {
        UUID postKey = RandomCodeGenerator.generatePostKey();

        assertThat(postKey.version()).isEqualTo(7);
    }

    @Test
    @DisplayName("generateOrderProductKey()는 UUID V7을 반환한다")
    void generateOrderProductKey_returnsUuidV7() {
        UUID orderProductKey = RandomCodeGenerator.generateOrderProductKey();

        assertThat(orderProductKey.version()).isEqualTo(7);
    }

    @Test
    @DisplayName("EventEnvelope 생성 시 eventId가 UUID V7 형식이다")
    void eventEnvelopeEventIdIsUuidV7() {
        EventEnvelope<String> envelope = EventEnvelope.of("test.topic", "test-source", "payload", FIXED_CLOCK);

        UUID eventId = UUID.fromString(envelope.eventId());
        assertThat(eventId.version()).isEqualTo(7);
    }

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
