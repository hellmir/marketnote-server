package com.personal.marketnote.product.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEventKafkaProducerTest {
    @InjectMocks
    private ProductEventKafkaProducer productEventKafkaProducer;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("상품 등록 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishProductRegisteredEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-02-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        productEventKafkaProducer.publishProductRegisteredEvent(1L, 2L, 3L, "테스트 상품", "1");

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.PRODUCT_REGISTERED);
        assertThat(captured.getPartitionKey()).isEqualTo("1");
        assertThat(captured.getSource()).isEqualTo("product-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("상품 등록 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishProductRegisteredEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-02-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        productEventKafkaProducer.publishProductRegisteredEvent(10L, 20L, 30L, "상품명", "2");

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.PRODUCT_REGISTERED);
        assertThat(capturedEnvelope.source()).isEqualTo("product-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        ProductRegisteredEvent payload = (ProductRegisteredEvent) capturedEnvelope.payload();
        assertThat(payload.productId()).isEqualTo(10L);
        assertThat(payload.pricePolicyId()).isEqualTo(20L);
        assertThat(payload.sellerId()).isEqualTo(30L);
        assertThat(payload.productName()).isEqualTo("상품명");
        assertThat(payload.goodsType()).isEqualTo("2");
    }

    @Test
    @DisplayName("가격 정책 생성 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishPricePolicyCreatedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-02-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        productEventKafkaProducer.publishPricePolicyCreatedEvent(1L, 2L);

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.PRICE_POLICY_CREATED);
        assertThat(captured.getPartitionKey()).isEqualTo("1");
        assertThat(captured.getSource()).isEqualTo("product-service");
    }

    @Test
    @DisplayName("가격 정책 생성 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishPricePolicyCreatedEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-02-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        productEventKafkaProducer.publishPricePolicyCreatedEvent(10L, 20L);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.PRICE_POLICY_CREATED);
        assertThat(capturedEnvelope.source()).isEqualTo("product-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        PricePolicyCreatedEvent payload = (PricePolicyCreatedEvent) capturedEnvelope.payload();
        assertThat(payload.productId()).isEqualTo(10L);
        assertThat(payload.pricePolicyId()).isEqualTo(20L);
    }
}
