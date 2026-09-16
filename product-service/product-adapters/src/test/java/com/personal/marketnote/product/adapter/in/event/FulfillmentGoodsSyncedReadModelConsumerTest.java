package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;
import com.personal.marketnote.product.port.out.fulfillment.SaveFulfillmentGoodsReadModelPort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FulfillmentGoodsSyncedReadModelConsumerTest {

    @InjectMocks
    private FulfillmentGoodsSyncedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveFulfillmentGoodsReadModelPort saveFulfillmentGoodsReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.FULFILLMENT_GOODS_SYNCED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<FulfillmentGoodsSyncedEvent> createEnvelope(FulfillmentGoodsSyncedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.FULFILLMENT_GOODS_SYNCED,
                "fulfillment-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    private FulfillmentGoodsSyncedEvent createValidEvent(String customerGoodsCode) {
        return new FulfillmentGoodsSyncedEvent(
                customerGoodsCode, "GD001", "테스트 상품", "SINGLE", "단품",
                "Y", null, null, "CUST001", "테스트 고객",
                null, null, null, null, null,
                null, "10000", "8000", "12000", null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, "8801234567890", null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, "Y", null, null, "Y",
                null, List.of()
        );
    }

    @Test
    @DisplayName("envelope가 null이면 ack만 호출하고 포트는 호출하지 않는다")
    void nullEnvelope_acknowledgesWithoutUpsert() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.FULFILLMENT_GOODS_SYNCED, 0, 0, "1", null
        );

        // when
        consumer.handleFulfillmentGoodsSyncedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveFulfillmentGoodsReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 일치하지 않으면 ack만 호출하고 포트는 호출하지 않는다")
    void eventTypeMismatch_acknowledgesWithoutUpsert() {
        // given
        FulfillmentGoodsSyncedEvent payload = createValidEvent("CGC001");
        EventEnvelope<FulfillmentGoodsSyncedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "fulfillment-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleFulfillmentGoodsSyncedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveFulfillmentGoodsReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("customerGoodsCode가 null이면 ack만 호출하고 포트는 호출하지 않는다")
    void nullCustomerGoodsCode_acknowledgesWithoutUpsert() {
        // given
        FulfillmentGoodsSyncedEvent payload = createValidEvent(null);
        EventEnvelope<FulfillmentGoodsSyncedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleFulfillmentGoodsSyncedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveFulfillmentGoodsReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("유효한 이벤트이면 upsert 포트를 호출하고 ack한다")
    void validEvent_upsertsAndAcknowledges() {
        // given
        FulfillmentGoodsSyncedEvent payload = createValidEvent("CGC001");
        EventEnvelope<FulfillmentGoodsSyncedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleFulfillmentGoodsSyncedEvent(record, acknowledgment);

        // then
        verify(saveFulfillmentGoodsReadModelPort).upsert(payload);
        verify(acknowledgment).acknowledge();
    }
}
