package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.common.utility.FormatValidator;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;

/**
 * Kafka 메시지 직렬화/역직렬화 및 스키마 호환성 검증 테스트.
 *
 * <p>EmbeddedKafkaKraftBroker(1브로커)를 사용하여 실제 Kafka를 통한
 * JsonSerializer/JsonDeserializer 기반 직렬화/역직렬화 라운드트립을 검증한다.</p>
 *
 * <pre>
 * Phase 1: 전체 이벤트 타입별 직렬화/역직렬화 라운드트립 (12개 이벤트 + null 필드)
 * Phase 2: 스키마 호환성 (필드 추가/제거, 버전 불일치)
 * Phase 3: DLT 전송 + trusted.packages 보안
 * </pre>
 */
@Tag("slow")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Kafka 메시지 직렬화/역직렬화 및 스키마 호환성 검증")
class KafkaMessageSerializationSchemaCompatibilityTest {

    private static final String ROUNDTRIP_TOPIC_PREFIX = "serde-roundtrip-test";
    private static final String DLT_TOPIC = "serde-dlt-test";
    private static final String SECURITY_TOPIC = "serde-security-test";
    private static final int READY_CHECK_MAX_ATTEMPTS = 40;
    private static final String SCHEMA_TEST_TIMESTAMP = "2026-03-21T19:00:00";

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-21T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private EmbeddedKafkaKraftBroker embeddedKafka;
    private String bootstrapServers;

    @BeforeAll
    void setUpCluster() throws Exception {
        embeddedKafka = new EmbeddedKafkaKraftBroker(1, 1);
        embeddedKafka.afterPropertiesSet();
        bootstrapServers = embeddedKafka.getBrokersAsString();
        waitForClusterReady();
    }

    @AfterAll
    void tearDownCluster() {
        if (embeddedKafka != null) {
            embeddedKafka.destroy();
        }
    }

    // === Phase 1: 전체 이벤트 타입별 직렬화/역직렬화 라운드트립 ===

    @Nested
    @DisplayName("전체 이벤트 타입별 직렬화/역직렬화 라운드트립")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class RoundTripTest {

        @Test
        @DisplayName("ProductRegisteredEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void productRegisteredEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            ProductRegisteredEvent event = new ProductRegisteredEvent(1L, 2L, 3L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L);
            EventEnvelope<ProductRegisteredEvent> envelope = EventEnvelope.of(
                    "product.product.registered", "product-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            ProductRegisteredEvent result = deserialized.getPayloadAs(ProductRegisteredEvent.class, OBJECT_MAPPER);
            assertThat(deserialized.eventType()).isEqualTo("product.product.registered");
            assertThat(deserialized.source()).isEqualTo("product-service");
            assertThat(deserialized.timestamp()).isEqualTo(envelope.timestamp());
            assertThat(result.productId()).isEqualTo(1L);
            assertThat(result.pricePolicyId()).isEqualTo(2L);
            assertThat(result.sellerId()).isEqualTo(3L);
            assertThat(result.productName()).isEqualTo("테스트 상품");
            assertThat(result.goodsType()).isEqualTo("1");
            assertThat(result.brandName()).isEqualTo("테스트 브랜드");
            assertThat(result.price()).isEqualTo(10000L);
            assertThat(result.discountPrice()).isEqualTo(8000L);
            assertThat(result.accumulatedPoint()).isEqualTo(100L);
        }

        @Test
        @DisplayName("ProductUpdatedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void productUpdatedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            ProductUpdatedEvent event = buildSampleProductUpdatedEvent();
            EventEnvelope<ProductUpdatedEvent> envelope = EventEnvelope.of(
                    "product.product.updated", "product-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            ProductUpdatedEvent result = deserialized.getPayloadAs(ProductUpdatedEvent.class, OBJECT_MAPPER);
            assertThat(result.productId()).isEqualTo(event.productId());
            assertThat(result.productName()).isEqualTo(event.productName());
            assertThat(result.goodsType()).isEqualTo(event.goodsType());
            assertThat(result.goodsBarcode()).isEqualTo(event.goodsBarcode());
            assertThat(result.safetyStock()).isEqualTo(event.safetyStock());
        }

        @Test
        @DisplayName("PricePolicyCreatedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void pricePolicyCreatedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(10L, 20L);
            EventEnvelope<PricePolicyCreatedEvent> envelope = EventEnvelope.of(
                    "product.price-policy.created", "product-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            PricePolicyCreatedEvent result = deserialized.getPayloadAs(PricePolicyCreatedEvent.class, OBJECT_MAPPER);
            assertThat(result.productId()).isEqualTo(10L);
            assertThat(result.pricePolicyId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("UserSignupCompletedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void userSignupCompletedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            UserSignupCompletedEvent event = new UserSignupCompletedEvent(100L, "user-key-abc");
            EventEnvelope<UserSignupCompletedEvent> envelope = EventEnvelope.of(
                    "user.user.signup-completed", "user-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            UserSignupCompletedEvent result = deserialized.getPayloadAs(UserSignupCompletedEvent.class, OBJECT_MAPPER);
            assertThat(result.userId()).isEqualTo(100L);
            assertThat(result.userKey()).isEqualTo("user-key-abc");
        }

        @Test
        @DisplayName("UserReferralCompletedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void userReferralCompletedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            UserReferralCompletedEvent event = new UserReferralCompletedEvent(1L, 2L);
            EventEnvelope<UserReferralCompletedEvent> envelope = EventEnvelope.of(
                    "user.user.referral-completed", "user-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            UserReferralCompletedEvent result = deserialized.getPayloadAs(UserReferralCompletedEvent.class, OBJECT_MAPPER);
            assertThat(result.requestUserId()).isEqualTo(1L);
            assertThat(result.referredUserId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("ReviewRegisteredEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void reviewRegisteredEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            ReviewRegisteredEvent event = new ReviewRegisteredEvent(50L, 60L);
            EventEnvelope<ReviewRegisteredEvent> envelope = EventEnvelope.of(
                    "community.review.registered", "community-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            ReviewRegisteredEvent result = deserialized.getPayloadAs(ReviewRegisteredEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(50L);
            assertThat(result.pricePolicyId()).isEqualTo(60L);
        }

        @Test
        @DisplayName("PaymentApprovedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void paymentApprovedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            PaymentApprovedEvent event = new PaymentApprovedEvent(10L, "order-key-123", 50000L);
            EventEnvelope<PaymentApprovedEvent> envelope = EventEnvelope.of(
                    "commerce.payment.approved", "commerce-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            PaymentApprovedEvent result = deserialized.getPayloadAs(PaymentApprovedEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(10L);
            assertThat(result.orderKey()).isEqualTo("order-key-123");
            assertThat(result.paymentAmount()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("PaymentFailedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void paymentFailedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            PaymentFailedEvent event = new PaymentFailedEvent(10L, "order-key-456", "E001", "결제 실패");
            EventEnvelope<PaymentFailedEvent> envelope = EventEnvelope.of(
                    "commerce.payment.failed", "commerce-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            PaymentFailedEvent result = deserialized.getPayloadAs(PaymentFailedEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(10L);
            assertThat(result.orderKey()).isEqualTo("order-key-456");
            assertThat(result.resultCode()).isEqualTo("E001");
            assertThat(result.resultMessage()).isEqualTo("결제 실패");
        }

        @Test
        @DisplayName("PaymentCancelledEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 중첩 타입 포함 모든 필드가 보존된다")
        void paymentCancelledEvent_roundTrip_preservesAllFieldsIncludingNestedTypes() throws Exception {
            // given
            PaymentCancelledEvent event = buildSamplePaymentCancelledEvent();
            EventEnvelope<PaymentCancelledEvent> envelope = EventEnvelope.of(
                    "commerce.payment.cancelled", "commerce-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            PaymentCancelledEvent result = deserialized.getPayloadAs(PaymentCancelledEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.orderKey()).isEqualTo("cancel-order-key");
            assertThat(result.buyerId()).isEqualTo(10L);
            assertThat(result.cancelAmount()).isEqualTo(30000L);
            assertThat(result.paymentAmount()).isEqualTo(50000L);
            assertThat(result.pointAmount()).isEqualTo(5000L);
            assertThat(result.isFullCancel()).isTrue();
            assertThat(result.alreadyRefunded()).isEqualTo(0L);
            assertThat(result.cancelId()).isEqualTo("cancel-uuid-123");
            assertThat(result.orderProducts()).hasSize(2);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
            assertThat(result.orderProducts().get(1).sharerId()).isEqualTo(20L);
            assertThat(result.cancelProducts()).hasSize(1);
            assertThat(result.cancelProducts().get(0).quantity()).isEqualTo(1);
            assertThat(result.partialProductPendingDeduction()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("OrderPaymentCompletedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 중첩 타입 포함 모든 필드가 보존된다")
        void orderPaymentCompletedEvent_roundTrip_preservesAllFieldsIncludingNestedTypes() throws Exception {
            // given
            OrderPaymentCompletedEvent event = buildSampleOrderPaymentCompletedEvent();
            EventEnvelope<OrderPaymentCompletedEvent> envelope = EventEnvelope.of(
                    "commerce.order.payment-completed", "commerce-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            OrderPaymentCompletedEvent result = deserialized.getPayloadAs(OrderPaymentCompletedEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.buyerId()).isEqualTo(10L);
            assertThat(result.totalAmount()).isEqualTo(50000L);
            assertThat(result.pointAmount()).isEqualTo(5000L);
            assertThat(result.orderProducts()).hasSize(2);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
            assertThat(result.orderProducts().get(0).unitAmount()).isEqualTo(25000L);
            assertThat(result.totalAccumulatedPoint()).isEqualTo(500L);
        }

        @Test
        @DisplayName("OrderPurchaseConfirmedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void orderPurchaseConfirmedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(1L, 10L, List.of(20L, 30L, 40L));
            EventEnvelope<OrderPurchaseConfirmedEvent> envelope = EventEnvelope.of(
                    "commerce.order.purchase-confirmed", "commerce-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            OrderPurchaseConfirmedEvent result = deserialized.getPayloadAs(OrderPurchaseConfirmedEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.buyerId()).isEqualTo(10L);
            assertThat(result.sharerIds()).containsExactly(20L, 30L, 40L);
        }

        @Test
        @DisplayName("SettlementExecutedEvent를 EventEnvelope에 담아 직렬화/역직렬화 라운드트립 시 모든 필드가 보존된다")
        void settlementExecutedEvent_roundTrip_preservesAllFields() throws Exception {
            // given
            SettlementExecutedEvent event = new SettlementExecutedEvent(1L, 10L, 100000L, 3000L, 5000L, 92000L);
            EventEnvelope<SettlementExecutedEvent> envelope = EventEnvelope.of(
                    "commerce.settlement.executed", "commerce-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            SettlementExecutedEvent result = deserialized.getPayloadAs(SettlementExecutedEvent.class, OBJECT_MAPPER);
            assertThat(result.settlementId()).isEqualTo(1L);
            assertThat(result.sellerId()).isEqualTo(10L);
            assertThat(result.totalAllocatedAmount()).isEqualTo(100000L);
            assertThat(result.pgFeeAmount()).isEqualTo(3000L);
            assertThat(result.platformFeeAmount()).isEqualTo(5000L);
            assertThat(result.sellerPayoutAmount()).isEqualTo(92000L);
        }

        @Test
        @DisplayName("EventEnvelope의 페이로드에 null 필드가 포함되면 역직렬화 후에도 null로 보존된다")
        void nullFieldsInPayload_preservedAfterDeserialization() throws Exception {
            // given — godType, cancelProducts 등 nullable 필드를 null로 설정
            ProductRegisteredEvent event = new ProductRegisteredEvent(1L, 2L, 3L, "상품", null, null, null, null, null);
            EventEnvelope<ProductRegisteredEvent> envelope = EventEnvelope.of(
                    "product.product.registered", "product-service", event, FIXED_CLOCK);

            // when
            EventEnvelope<?> deserialized = publishAndConsume(ROUNDTRIP_TOPIC_PREFIX, envelope);

            // then
            ProductRegisteredEvent result = deserialized.getPayloadAs(ProductRegisteredEvent.class, OBJECT_MAPPER);
            assertThat(FormatValidator.hasNoValue(result.goodsType())).isTrue();
            assertThat(result.productId()).isEqualTo(1L);
            assertThat(result.productName()).isEqualTo("상품");
        }
    }

    // === Phase 2: 스키마 호환성 ===

    @Nested
    @DisplayName("스키마 호환성 검증")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SchemaCompatibilityTest {

        @Test
        @DisplayName("Producer가 추가 필드를 포함한 이벤트를 발행하면 기존 Consumer가 알 수 없는 필드를 무시하고 정상 역직렬화한다")
        void additionalFieldsIgnored_backwardCompatibility() throws Exception {
            // given — Producer가 V2 이벤트(추가 필드 포함)를 JSON으로 직렬화하여 발행
            String topic = "schema-backward-" + UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> v2Payload = new LinkedHashMap<>();
            v2Payload.put("productId", 999L);
            v2Payload.put("pricePolicyId", 888L);
            v2Payload.put("sellerId", 777L);
            v2Payload.put("productName", "V2 상품");
            v2Payload.put("goodsType", "1");
            v2Payload.put("newFieldInV2", "이 필드는 V1에 없다");
            v2Payload.put("anotherNewField", 42);

            Map<String, Object> v2Envelope = new LinkedHashMap<>();
            v2Envelope.put("eventId", UUID.randomUUID().toString());
            v2Envelope.put("eventType", "product.product.registered");
            v2Envelope.put("source", "product-service");
            v2Envelope.put("timestamp", SCHEMA_TEST_TIMESTAMP);
            v2Envelope.put("payload", v2Payload);

            String json = OBJECT_MAPPER.writeValueAsString(v2Envelope);

            // when — raw JSON을 StringSerializer로 발행, JsonDeserializer로 수신
            publishRawJson(topic, json);
            EventEnvelope<?> deserialized = consumeEnvelope(topic, "schema-backward-group");

            // then — 알 수 없는 필드는 무시되고 기존 필드는 정상 역직렬화
            ProductRegisteredEvent result = deserialized.getPayloadAs(ProductRegisteredEvent.class, OBJECT_MAPPER);
            assertThat(result.productId()).isEqualTo(999L);
            assertThat(result.pricePolicyId()).isEqualTo(888L);
            assertThat(result.sellerId()).isEqualTo(777L);
            assertThat(result.productName()).isEqualTo("V2 상품");
        }

        @Test
        @DisplayName("Producer가 필드를 제거한 이벤트를 발행하면 Consumer가 누락 필드를 null로 처리하여 정상 역직렬화한다")
        void missingFields_treatedAsNull_forwardCompatibility() throws Exception {
            // given — V0 이벤트(일부 필드 제거)
            String topic = "schema-forward-" + UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> v0Payload = new LinkedHashMap<>();
            v0Payload.put("productId", 111L);
            // pricePolicyId, sellerId, godType 제거

            Map<String, Object> v0Envelope = new LinkedHashMap<>();
            v0Envelope.put("eventId", UUID.randomUUID().toString());
            v0Envelope.put("eventType", "product.product.registered");
            v0Envelope.put("source", "product-service");
            v0Envelope.put("timestamp", SCHEMA_TEST_TIMESTAMP);
            v0Envelope.put("payload", v0Payload);

            String json = OBJECT_MAPPER.writeValueAsString(v0Envelope);

            // when
            publishRawJson(topic, json);
            EventEnvelope<?> deserialized = consumeEnvelope(topic, "schema-forward-group");

            // then — 누락 필드는 null로 처리
            ProductRegisteredEvent result = deserialized.getPayloadAs(ProductRegisteredEvent.class, OBJECT_MAPPER);
            assertThat(result.productId()).isEqualTo(111L);
            assertThat(FormatValidator.hasNoValue(result.pricePolicyId())).isTrue();
            assertThat(FormatValidator.hasNoValue(result.sellerId())).isTrue();
            assertThat(FormatValidator.hasNoValue(result.productName())).isTrue();
            assertThat(FormatValidator.hasNoValue(result.goodsType())).isTrue();
        }

        @Test
        @DisplayName("Producer V2와 Consumer V1 간 버전 불일치 시 추가 필드가 무시되어 정상 역직렬화한다")
        void producerV2_consumerV1_additionalFieldsIgnored() throws Exception {
            // given — PaymentApprovedEvent에 V2 추가 필드
            String topic = "schema-v2v1-" + UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> v2Payload = new LinkedHashMap<>();
            v2Payload.put("orderId", 10L);
            v2Payload.put("orderKey", "order-v2-key");
            v2Payload.put("paymentAmount", 30000L);
            v2Payload.put("paymentMethod", "CARD");
            v2Payload.put("pgProvider", "KCP");

            Map<String, Object> v2Envelope = new LinkedHashMap<>();
            v2Envelope.put("eventId", UUID.randomUUID().toString());
            v2Envelope.put("eventType", "commerce.payment.approved");
            v2Envelope.put("source", "commerce-service");
            v2Envelope.put("timestamp", SCHEMA_TEST_TIMESTAMP);
            v2Envelope.put("payload", v2Payload);

            String json = OBJECT_MAPPER.writeValueAsString(v2Envelope);

            // when
            publishRawJson(topic, json);
            EventEnvelope<?> deserialized = consumeEnvelope(topic, "version-mismatch-v2-group");

            // then
            PaymentApprovedEvent result = deserialized.getPayloadAs(PaymentApprovedEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(10L);
            assertThat(result.orderKey()).isEqualTo("order-v2-key");
            assertThat(result.paymentAmount()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("Producer V1과 Consumer V2 간 버전 불일치 시 누락 필드가 null로 처리되어 정상 역직렬화한다")
        void producerV1_consumerV2_missingFieldsNull() throws Exception {
            // given — 최소 필드만 포함한 V1 이벤트
            String topic = "schema-v1v2-" + UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> v1Payload = new LinkedHashMap<>();
            v1Payload.put("orderId", 20L);

            Map<String, Object> v1Envelope = new LinkedHashMap<>();
            v1Envelope.put("eventId", UUID.randomUUID().toString());
            v1Envelope.put("eventType", "commerce.payment.approved");
            v1Envelope.put("source", "commerce-service");
            v1Envelope.put("timestamp", SCHEMA_TEST_TIMESTAMP);
            v1Envelope.put("payload", v1Payload);

            String json = OBJECT_MAPPER.writeValueAsString(v1Envelope);

            // when
            publishRawJson(topic, json);
            EventEnvelope<?> deserialized = consumeEnvelope(topic, "version-mismatch-v1-group");

            // then
            PaymentApprovedEvent result = deserialized.getPayloadAs(PaymentApprovedEvent.class, OBJECT_MAPPER);
            assertThat(result.orderId()).isEqualTo(20L);
            assertThat(FormatValidator.hasNoValue(result.orderKey())).isTrue();
            assertThat(FormatValidator.hasNoValue(result.paymentAmount())).isTrue();
        }

        @Test
        @DisplayName("필드 타입이 호환되지 않게 변경되면 역직렬화가 실패한다")
        void incompatibleFieldTypeChange_deserializationFails() throws Exception {
            // given — orderId를 String으로 변경 (Long → String 비호환)
            String topic = "schema-incompatible-" + UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> incompatiblePayload = new LinkedHashMap<>();
            incompatiblePayload.put("orderId", "not-a-number");
            incompatiblePayload.put("orderKey", "key");
            incompatiblePayload.put("paymentAmount", 1000L);

            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", UUID.randomUUID().toString());
            envelope.put("eventType", "commerce.payment.approved");
            envelope.put("source", "commerce-service");
            envelope.put("timestamp", SCHEMA_TEST_TIMESTAMP);
            envelope.put("payload", incompatiblePayload);

            String json = OBJECT_MAPPER.writeValueAsString(envelope);

            // when — EventEnvelope 자체는 역직렬화 성공 (payload는 LinkedHashMap)
            publishRawJson(topic, json);
            EventEnvelope<?> deserialized = consumeEnvelope(topic, "incompatible-type-group");

            // then — getPayloadAs에서 타입 변환 실패
            assertThatThrownBy(() ->
                    deserialized.getPayloadAs(PaymentApprovedEvent.class, OBJECT_MAPPER))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // === Phase 3: DLT 전송 + trusted.packages 보안 ===

    @Nested
    @DisplayName("역직렬화 실패 및 보안 검증")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DeserializationFailureAndSecurityTest {

        @Test
        @DisplayName("유효하지 않은 JSON 메시지는 JsonDeserializer에서 역직렬화 실패 예외가 발생한다")
        void invalidJson_deserializationFails() {
            // given
            String invalidJson = "{invalid-json-that-cannot-be-parsed";

            // when & then — JsonDeserializer로 직접 역직렬화 시 예외 발생
            JsonDeserializer<EventEnvelope> deserializer = createEventEnvelopeDeserializer();
            assertThatThrownBy(() ->
                    deserializer.deserialize(DLT_TOPIC, invalidJson.getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(SerializationException.class);
            deserializer.close();
        }

        @Test
        @DisplayName("USE_TYPE_INFO_HEADERS가 false이면 타입 헤더와 무관하게 VALUE_DEFAULT_TYPE으로 역직렬화된다")
        void useTypeInfoHeadersFalse_ignoresTypeHeaders() throws Exception {
            // given — 타입 헤더에 임의 클래스를 설정한 메시지
            PaymentApprovedEvent event = new PaymentApprovedEvent(1L, "key", 1000L);
            EventEnvelope<PaymentApprovedEvent> envelope = EventEnvelope.of(
                    "commerce.payment.approved", "commerce-service", event, FIXED_CLOCK);

            String json = OBJECT_MAPPER.writeValueAsString(envelope);

            // when — 타입 헤더에 악의적 클래스명을 설정하여 발행
            ProducerRecord<String, String> record = new ProducerRecord<>(SECURITY_TOPIC, "key", json);
            record.headers().add(new RecordHeader("__TypeId__",
                    "java.lang.Runtime".getBytes(StandardCharsets.UTF_8)));
            publishRawRecord(record);

            // then — USE_TYPE_INFO_HEADERS=false이므로 타입 헤더 무시, EventEnvelope로 정상 역직렬화
            EventEnvelope<?> deserialized = consumeEnvelope(SECURITY_TOPIC, "type-header-ignore-group");
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.eventType()).isEqualTo("commerce.payment.approved");
        }

        @Test
        @DisplayName("trusted.packages 외부 패키지의 타입 헤더가 포함된 메시지는 타입 헤더 활성화 시 역직렬화가 거부된다")
        void untrustedPackage_withTypeHeaders_rejected() {
            // given — USE_TYPE_INFO_HEADERS=true + trusted.packages 제한 설정의 JsonDeserializer
            Map<String, Object> props = new HashMap<>();
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.personal.marketnote.common.kafka.event");
            props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

            JsonDeserializer<Object> deserializer = new JsonDeserializer<>();
            deserializer.configure(props, false);

            // when — 신뢰하지 않는 패키지의 타입 헤더를 가진 메시지
            String validJson = "{\"key\":\"value\"}";
            byte[] data = validJson.getBytes(StandardCharsets.UTF_8);

            org.apache.kafka.common.header.Headers headers = new org.apache.kafka.common.header.internals.RecordHeaders();
            headers.add(new RecordHeader("__TypeId__",
                    "java.lang.ProcessBuilder".getBytes(StandardCharsets.UTF_8)));

            // then — 신뢰하지 않는 패키지이므로 역직렬화 거부
            assertThatThrownBy(() -> deserializer.deserialize(SECURITY_TOPIC, headers, data))
                    .isInstanceOf(SerializationException.class);
            deserializer.close();
        }
    }

    // === 유틸리티 메서드 ===

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private EventEnvelope<?> publishAndConsume(String topic, EventEnvelope<?> envelope)
            throws ExecutionException, InterruptedException, TimeoutException {
        String uniqueTopic = topic + "-" + UUID.randomUUID().toString().substring(0, 8);
        String groupId = "roundtrip-" + UUID.randomUUID().toString().substring(0, 8);

        try (KafkaProducer<String, Object> producer = createJsonProducer()) {
            Future<RecordMetadata> future = producer.send(new ProducerRecord<>(uniqueTopic, envelope.eventId(), envelope));
            future.get(10, TimeUnit.SECONDS);
        }

        return consumeEnvelope(uniqueTopic, groupId);
    }

    private void publishRawJson(String topic, String json)
            throws ExecutionException, InterruptedException, TimeoutException {
        try (KafkaProducer<String, String> producer = createStringProducer()) {
            Future<RecordMetadata> future = producer.send(new ProducerRecord<>(topic, "key", json));
            future.get(10, TimeUnit.SECONDS);
        }
    }

    private void publishRawRecord(ProducerRecord<String, String> record)
            throws ExecutionException, InterruptedException, TimeoutException {
        try (KafkaProducer<String, String> producer = createStringProducer()) {
            Future<RecordMetadata> future = producer.send(record);
            future.get(10, TimeUnit.SECONDS);
        }
    }

    private EventEnvelope<?> consumeEnvelope(String topic, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.personal.marketnote.common.kafka.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EventEnvelope.class.getName());

        try (KafkaConsumer<String, EventEnvelope<?>> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            long deadline = System.currentTimeMillis() + Duration.ofSeconds(30).toMillis();
            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, EventEnvelope<?>> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, EventEnvelope<?>> record : records) {
                    return record.value();
                }
            }
        }
        return fail("토픽 " + topic + "에서 메시지를 수신하지 못했습니다 (30초 타임아웃)");
    }

    private KafkaProducer<String, Object> createJsonProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new KafkaProducer<>(props);
    }

    private KafkaProducer<String, String> createStringProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    private JsonDeserializer<EventEnvelope> createEventEnvelopeDeserializer() {
        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.personal.marketnote.common.kafka.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EventEnvelope.class.getName());

        JsonDeserializer<EventEnvelope> deserializer = new JsonDeserializer<>(EventEnvelope.class);
        deserializer.configure(props, false);
        return deserializer;
    }

    private void waitForClusterReady() throws InterruptedException {
        for (int attempt = 0; attempt < READY_CHECK_MAX_ATTEMPTS; attempt++) {
            try (KafkaProducer<String, String> producer = createStringProducer()) {
                Future<RecordMetadata> future = producer.send(
                        new ProducerRecord<>("cluster-ready-check", "readiness-" + attempt, "check"));
                future.get(5, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }
        throw new IllegalStateException(
                "Kafka 클러스터가 " + READY_CHECK_MAX_ATTEMPTS + "회 시도 후에도 준비되지 않음");
    }

    // === 샘플 이벤트 빌더 ===

    private ProductUpdatedEvent buildSampleProductUpdatedEvent() {
        return new ProductUpdatedEvent(
                1L, "업데이트 상품", "1", "N", "OPT1", "OPT2",
                "Y", "커스텀상품명", "SUP001", "CATE01",
                "SS2026", "U", "2026", "50000", "30000",
                "45000", "NORMAL", "PICK01", "8801234567890",
                "1.5", "국내", "Y", "365", "7",
                "3", "BOX", "N", "UPRIGHT",
                "종이", "Y", "100", "N",
                "1", "https://img.test/1.jpg", "https://external.test/1.jpg"
        );
    }

    private PaymentCancelledEvent buildSamplePaymentCancelledEvent() {
        List<PaymentCancelledEvent.OrderProductItem> orderProducts = List.of(
                new PaymentCancelledEvent.OrderProductItem(100L, 10L, 2, 15000L),
                new PaymentCancelledEvent.OrderProductItem(200L, 20L, 1, 20000L)
        );
        List<PaymentCancelledEvent.OrderProductItem> cancelProducts = List.of(
                new PaymentCancelledEvent.OrderProductItem(100L, 10L, 1, 15000L)
        );
        return new PaymentCancelledEvent(
                1L, "cancel-order-key", 10L, 30000L, 50000L, 5000L,
                true, 0L, "cancel-uuid-123",
                orderProducts, cancelProducts, 1500L
        );
    }

    private OrderPaymentCompletedEvent buildSampleOrderPaymentCompletedEvent() {
        List<OrderPaymentCompletedEvent.OrderProductItem> orderProducts = List.of(
                new OrderPaymentCompletedEvent.OrderProductItem(100L, 10L, 2, 25000L),
                new OrderPaymentCompletedEvent.OrderProductItem(200L, null, 1, 20000L)
        );
        return new OrderPaymentCompletedEvent(1L, 10L, 50000L, 5000L, orderProducts, 500L);
    }
}
