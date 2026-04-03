package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EventEnvelopeTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-02-27T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("of 메서드로 생성 시 UUID v7 형식의 eventId가 생성된다")
    void of_generatesTimeOrderedUuidEventId() {
        // when
        EventEnvelope<String> envelope = EventEnvelope.of("test.topic", "test-source", "payload", FIXED_CLOCK);

        // then
        assertThat(envelope.eventId()).isNotNull();
        assertThat(envelope.eventId()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("of 메서드로 생성 시 eventType과 source가 올바르게 설정된다")
    void of_setsEventTypeAndSource() {
        // when
        EventEnvelope<String> envelope = EventEnvelope.of("product.product.registered", "product-service", "payload", FIXED_CLOCK);

        // then
        assertThat(envelope.eventType()).isEqualTo("product.product.registered");
        assertThat(envelope.source()).isEqualTo("product-service");
    }

    @Test
    @DisplayName("of 메서드로 생성 시 Clock 기반 timestamp가 설정된다")
    void of_setsTimestampFromClock() {
        // when
        EventEnvelope<String> envelope = EventEnvelope.of("test.topic", "test-source", "payload", FIXED_CLOCK);

        // then
        LocalDateTime expectedTimestamp = LocalDateTime.now(FIXED_CLOCK);
        assertThat(envelope.timestamp()).isEqualTo(expectedTimestamp);
    }

    @Test
    @DisplayName("of 메서드로 생성 시 payload가 올바르게 설정된다")
    void of_setsPayload() {
        // given
        ProductRegisteredEvent event = new ProductRegisteredEvent(1L, 2L, 3L, "상품명", "1", "브랜드", 10000L, 8000L, 100L);

        // when
        EventEnvelope<ProductRegisteredEvent> envelope = EventEnvelope.of(
                "product.product.registered", "product-service", event, FIXED_CLOCK
        );

        // then
        assertThat(envelope.payload()).isEqualTo(event);
        assertThat(envelope.payload().productId()).isEqualTo(1L);
        assertThat(envelope.payload().pricePolicyId()).isEqualTo(2L);
        assertThat(envelope.payload().sellerId()).isEqualTo(3L);
        assertThat(envelope.payload().productName()).isEqualTo("상품명");
        assertThat(envelope.payload().goodsType()).isEqualTo("1");
    }

    @Test
    @DisplayName("getPayloadAs 호출 시 이미 올바른 타입이면 캐스트하여 반환한다")
    void getPayloadAs_alreadyCorrectType_returnsCast() {
        // given
        ProductRegisteredEvent event = new ProductRegisteredEvent(10L, 20L, 30L, "테스트", "2", "테스트 브랜드", 20000L, 15000L, 200L);
        EventEnvelope<ProductRegisteredEvent> envelope = EventEnvelope.of(
                "product.product.registered", "product-service", event, FIXED_CLOCK
        );

        // when
        ProductRegisteredEvent result = envelope.getPayloadAs(ProductRegisteredEvent.class, OBJECT_MAPPER);

        // then
        assertThat(result).isSameAs(event);
    }

    @Test
    @DisplayName("getPayloadAs 호출 시 LinkedHashMap 타입이면 ObjectMapper로 변환한다")
    void getPayloadAs_linkedHashMap_convertsViaObjectMapper() {
        // given — Kafka 역직렬화 시 payload가 LinkedHashMap으로 들어오는 케이스
        Map<String, Object> rawPayload = new LinkedHashMap<>();
        rawPayload.put("productId", 100L);
        rawPayload.put("pricePolicyId", 200L);
        rawPayload.put("sellerId", 300L);
        rawPayload.put("productName", "맵 변환 상품");
        rawPayload.put("goodsType", "1");
        rawPayload.put("brandName", "맵 변환 브랜드");
        rawPayload.put("price", 10000L);
        rawPayload.put("discountPrice", 8000L);
        rawPayload.put("accumulatedPoint", 100L);

        EventEnvelope<Map<String, Object>> envelope = new EventEnvelope<>(
                "test-event-id", "product.product.registered", "product-service",
                LocalDateTime.now(FIXED_CLOCK), rawPayload
        );

        // when
        @SuppressWarnings("unchecked")
        ProductRegisteredEvent result = ((EventEnvelope<?>) envelope)
                .getPayloadAs(ProductRegisteredEvent.class, OBJECT_MAPPER);

        // then
        assertThat(result.productId()).isEqualTo(100L);
        assertThat(result.pricePolicyId()).isEqualTo(200L);
        assertThat(result.sellerId()).isEqualTo(300L);
        assertThat(result.productName()).isEqualTo("맵 변환 상품");
        assertThat(result.goodsType()).isEqualTo("1");
    }

    @Test
    @DisplayName("서로 다른 of 호출은 서로 다른 eventId를 생성한다")
    void of_generatesUniqueEventIds() {
        // when
        EventEnvelope<String> first = EventEnvelope.of("topic", "source", "a", FIXED_CLOCK);
        EventEnvelope<String> second = EventEnvelope.of("topic", "source", "b", FIXED_CLOCK);

        // then
        assertThat(first.eventId()).isNotEqualTo(second.eventId());
    }
}
