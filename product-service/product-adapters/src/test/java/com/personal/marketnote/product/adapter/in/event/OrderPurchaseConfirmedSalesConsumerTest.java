package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.product.port.in.usecase.sales.SyncConfirmedOrderSalesUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderPurchaseConfirmedSalesConsumerTest {

    @InjectMocks
    private OrderPurchaseConfirmedSalesConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SyncConfirmedOrderSalesUseCase syncConfirmedOrderSalesUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    private static final LocalDateTime FIXED_TIME =
            LocalDateTime.now(Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneId.of("Asia/Seoul")));

    @Test
    @DisplayName("envelope 이 null 이면 UseCase 를 호출하지 않고 ack 한다")
    void skipsOnNullEnvelope() {
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED, 0, 0, "1", null);

        consumer.handleOrderPurchaseConfirmed(record, acknowledgment);

        verifyNoInteractions(syncConfirmedOrderSalesUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 일치하지 않으면 UseCase 를 호출하지 않고 ack 한다")
    void skipsOnEventTypeMismatch() {
        EventEnvelope<Object> envelope = new EventEnvelope<>(
                "evt-1", "wrong.event.type", "commerce-service", FIXED_TIME, null
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED, 0, 0, "1", envelope);

        consumer.handleOrderPurchaseConfirmed(record, acknowledgment);

        verifyNoInteractions(syncConfirmedOrderSalesUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("payload 가 유효하면 UseCase.syncSales 를 호출하고 ack 한다")
    void invokesSyncSalesOnValidPayload() {
        OrderPurchaseConfirmedEvent payload = new OrderPurchaseConfirmedEvent(
                100L, 10L, List.of(), false,
                List.of(new OrderPurchaseConfirmedEvent.OrderProductItem(1L, 11L, 3))
        );
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = new EventEnvelope<>(
                "evt-1", KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED, "commerce-service", FIXED_TIME, payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED, 0, 0, "1", envelope);

        consumer.handleOrderPurchaseConfirmed(record, acknowledgment);

        verify(syncConfirmedOrderSalesUseCase).syncSales(any(OrderPurchaseConfirmedEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId 가 null 이면 UseCase 를 호출하지 않고 ack 한다")
    void skipsOnInvalidOrderId() {
        OrderPurchaseConfirmedEvent payload = new OrderPurchaseConfirmedEvent(
                null, 10L, List.of(), false, List.of()
        );
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = new EventEnvelope<>(
                "evt-1", KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED, "commerce-service", FIXED_TIME, payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED, 0, 0, "1", envelope);

        consumer.handleOrderPurchaseConfirmed(record, acknowledgment);

        verifyNoInteractions(syncConfirmedOrderSalesUseCase);
        verify(acknowledgment).acknowledge();
    }
}
