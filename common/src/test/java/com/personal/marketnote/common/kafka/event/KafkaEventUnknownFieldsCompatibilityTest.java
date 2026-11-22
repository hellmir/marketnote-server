package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 운영 ObjectMapper(default 설정 = FAIL_ON_UNKNOWN_PROPERTIES=true) 시뮬레이션 라운드트립 테스트.
 *
 * <p>운영 환경의 모든 ObjectMapper(ObjectMapperConfig 빈 + Spring Kafka JsonDeserializer 내부 mapper)는
 * Jackson 기본값(FAIL_ON_UNKNOWN_PROPERTIES=true)을 따르므로, Producer가 신규 필드를 추가한
 * 이벤트를 발행했을 때 구버전 Consumer가 UnrecognizedPropertyException을 던지지 않고
 * 정상 역직렬화되어야 한다.</p>
 *
 * <p>각 record에 부착된 {@code @JsonIgnoreProperties(ignoreUnknown = true)}가
 * 운영 ObjectMapper 환경에서도 동작함을 검증한다.</p>
 */
@DisplayName("Kafka 이벤트 record의 알 수 없는 필드 무시 호환성 검증 (운영 ObjectMapper 시뮬)")
class KafkaEventUnknownFieldsCompatibilityTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-22T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    /**
     * 운영 ObjectMapperConfig는 FAIL_ON_UNKNOWN_PROPERTIES를 명시 설정하지 않으므로
     * Jackson 기본값(true)이 적용된다. 본 테스트는 그 환경을 명시적으로 시뮬한다.
     */
    private static ObjectMapper createStrictObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return mapper;
    }

    @Test
    @DisplayName("ProductRegisteredEvent에 알 수 없는 필드가 추가되어도 운영 ObjectMapper에서 역직렬화에 성공한다")
    void productRegisteredEvent_unknownFields_deserializedWithStrictMapper() throws Exception {
        // given — V2 payload with unknown fields
        ObjectMapper strictMapper = createStrictObjectMapper();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("productId", 1L);
        payload.put("productKey", UUID.randomUUID().toString());
        payload.put("pricePolicyId", 2L);
        payload.put("sellerId", 3L);
        payload.put("productName", "V2 상품");
        payload.put("goodsType", "1");
        payload.put("brandName", "브랜드");
        payload.put("price", 10000L);
        payload.put("discountPrice", 8000L);
        payload.put("accumulatedPoint", 100L);
        payload.put("sales", 0L);
        payload.put("fasstoProductCode", "FASSTO0000000000000001");
        payload.put("imported", false);
        // V2 추가 필드
        payload.put("v2NewField1", "신규 필드 값");
        payload.put("v2NewField2", 42);

        String json = strictMapper.writeValueAsString(payload);

        // when & then — strict mapper 환경에서 역직렬화 성공
        assertThatCode(() -> {
            ProductRegisteredEvent result = strictMapper.readValue(json, ProductRegisteredEvent.class);
            assertThat(result.productId()).isEqualTo(1L);
            assertThat(result.productName()).isEqualTo("V2 상품");
            assertThat(result.fasstoProductCode()).isEqualTo("FASSTO0000000000000001");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("OrderPaymentCompletedEvent의 외부 record에 알 수 없는 필드가 있어도 운영 ObjectMapper에서 역직렬화에 성공한다")
    void orderPaymentCompletedEvent_unknownFieldsOnOuter_deserializedWithStrictMapper() throws Exception {
        // given
        ObjectMapper strictMapper = createStrictObjectMapper();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", 1L);
        payload.put("buyerId", 10L);
        payload.put("totalAmount", 50000L);
        payload.put("pointAmount", 5000L);
        payload.put("recipientName", "홍길동");
        payload.put("recipientPhoneNumber", "01012345678");
        payload.put("zipCode", "12345");
        payload.put("address", "서울시 강남구");
        payload.put("addressDetail", "101호");
        payload.put("deliveryRequestMessage", "문 앞");
        payload.put("orderProducts", List.of());
        payload.put("totalAccumulatedPoint", 500L);
        // V2 추가 필드
        payload.put("v2OuterField", "outer-extra");

        String json = strictMapper.writeValueAsString(payload);

        // when & then
        assertThatCode(() -> {
            OrderPaymentCompletedEvent result = strictMapper.readValue(json, OrderPaymentCompletedEvent.class);
            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.totalAmount()).isEqualTo(50000L);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("OrderPaymentCompletedEvent의 nested OrderProductItem에 알 수 없는 필드가 있어도 운영 ObjectMapper에서 역직렬화에 성공한다")
    void orderPaymentCompletedEvent_unknownFieldsOnNested_deserializedWithStrictMapper() throws Exception {
        // given
        ObjectMapper strictMapper = createStrictObjectMapper();

        Map<String, Object> nestedItem = new LinkedHashMap<>();
        nestedItem.put("pricePolicyId", 100L);
        nestedItem.put("productId", 1L);
        nestedItem.put("productKey", UUID.randomUUID().toString());
        nestedItem.put("sharerKey", UUID.randomUUID().toString());
        nestedItem.put("quantity", 2);
        nestedItem.put("unitAmount", 25000L);
        // V2 nested 추가 필드
        nestedItem.put("v2NestedField", "nested-extra");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", 1L);
        payload.put("buyerId", 10L);
        payload.put("totalAmount", 50000L);
        payload.put("pointAmount", 5000L);
        payload.put("recipientName", "홍길동");
        payload.put("recipientPhoneNumber", "01012345678");
        payload.put("zipCode", "12345");
        payload.put("address", "서울시 강남구");
        payload.put("addressDetail", "101호");
        payload.put("deliveryRequestMessage", "문 앞");
        payload.put("orderProducts", List.of(nestedItem));
        payload.put("totalAccumulatedPoint", 500L);

        String json = strictMapper.writeValueAsString(payload);

        // when & then — nested record에도 어노테이션이 부착되어 있어야 성공
        assertThatCode(() -> {
            OrderPaymentCompletedEvent result = strictMapper.readValue(json, OrderPaymentCompletedEvent.class);
            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
            assertThat(result.orderProducts().get(0).quantity()).isEqualTo(2);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("FulfillmentInventorySyncedEvent의 nested InventoryItem에 알 수 없는 필드가 있어도 운영 ObjectMapper에서 역직렬화에 성공한다")
    void fulfillmentInventorySyncedEvent_unknownFieldsOnNested_deserializedWithStrictMapper() throws Exception {
        // given
        ObjectMapper strictMapper = createStrictObjectMapper();

        Map<String, Object> nestedItem = new LinkedHashMap<>();
        nestedItem.put("productKey", UUID.randomUUID().toString());
        nestedItem.put("stock", 100);
        // V2 nested 추가 필드
        nestedItem.put("v2WarehouseId", "WH001");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inventories", List.of(nestedItem));
        // V2 outer 추가 필드
        payload.put("v2SyncedAt", "2026-11-22T10:00:00");

        String json = strictMapper.writeValueAsString(payload);

        // when & then — outer/nested 모두 어노테이션 보유 시 성공
        assertThatCode(() -> {
            FulfillmentInventorySyncedEvent result = strictMapper.readValue(
                    json, FulfillmentInventorySyncedEvent.class);
            assertThat(result.inventories()).hasSize(1);
            assertThat(result.inventories().get(0).stock()).isEqualTo(100);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("EventEnvelope payload(LinkedHashMap)와 함께 알 수 없는 envelope 필드가 있어도 운영 ObjectMapper에서 역직렬화에 성공한다")
    void eventEnvelope_unknownFields_deserializedWithStrictMapper() throws Exception {
        // given
        ObjectMapper strictMapper = createStrictObjectMapper();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", 10L);
        payload.put("orderKey", "key");
        payload.put("paymentAmount", 1000L);

        Map<String, Object> envelopeMap = new LinkedHashMap<>();
        envelopeMap.put("eventId", UUID.randomUUID().toString());
        envelopeMap.put("eventType", "commerce.payment.approved");
        envelopeMap.put("source", "commerce-service");
        envelopeMap.put("timestamp", "2026-11-22T19:00:00");
        envelopeMap.put("payload", payload);
        // V2 envelope 추가 필드
        envelopeMap.put("v2TraceId", "trace-001");
        envelopeMap.put("v2Origin", "kafka");

        String json = strictMapper.writeValueAsString(envelopeMap);

        // when & then
        assertThatCode(() -> {
            EventEnvelope<?> envelope = strictMapper.readValue(json, EventEnvelope.class);
            assertThat(envelope.eventType()).isEqualTo("commerce.payment.approved");
            PaymentApprovedEvent result = envelope.getPayloadAs(PaymentApprovedEvent.class, strictMapper);
            assertThat(result.orderId()).isEqualTo(10L);
        }).doesNotThrowAnyException();
    }
}
