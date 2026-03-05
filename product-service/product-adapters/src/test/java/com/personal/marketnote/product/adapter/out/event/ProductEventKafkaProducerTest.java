package com.personal.marketnote.product.adapter.out.event;

import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEventKafkaProducerTest {
    @InjectMocks
    private ProductEventKafkaProducer productEventKafkaProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private Clock clock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("상품 등록 이벤트 발행 시 올바른 토픽과 파티션 키로 전송된다")
    void publishProductRegisteredEvent_sendsToCorrectTopicWithProductIdKey() {
        // given
        setUpClock("2026-02-27T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        productEventKafkaProducer.publishProductRegisteredEvent(1L, 2L, 3L, "테스트 상품", "1");

        // then
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.PRODUCT_REGISTERED),
                eq("1"),
                any(EventEnvelope.class)
        );
    }

    @Test
    @DisplayName("상품 등록 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishProductRegisteredEvent_envelopeContainsCorrectPayload() {
        // given
        setUpClock("2026-02-27T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        productEventKafkaProducer.publishProductRegisteredEvent(10L, 20L, 30L, "상품명", "2");

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.PRODUCT_REGISTERED),
                eq("10"),
                envelopeCaptor.capture()
        );

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
        assertThat(payload.godType()).isEqualTo("2");
    }
}
