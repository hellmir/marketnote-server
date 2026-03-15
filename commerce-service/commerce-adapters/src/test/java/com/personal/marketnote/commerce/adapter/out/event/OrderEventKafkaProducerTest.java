package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventKafkaProducer 테스트")
class OrderEventKafkaProducerTest {
    @InjectMocks
    private OrderEventKafkaProducer orderEventKafkaProducer;

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

    private List<OrderProduct> createOrderProducts() {
        OrderProduct product1 = OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .sharerId(200L)
                .quantity(2)
                .unitAmount(30000L)
                .orderStatus(OrderStatus.PAID)
                .build());
        OrderProduct product2 = OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(10L)
                .pricePolicyId(101L)
                .quantity(1)
                .unitAmount(20000L)
                .orderStatus(OrderStatus.PAID)
                .build());
        return List.of(product1, product2);
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishOrderPaymentCompletedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        List<OrderProduct> orderProducts = createOrderProducts();

        // when
        orderEventKafkaProducer.publishOrderPaymentCompletedEvent(
                1L, 50L, 80000L, 5000L, orderProducts, 1500L
        );

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.ORDER_PAYMENT_COMPLETED);
        assertThat(captured.getPartitionKey()).isEqualTo("1");
        assertThat(captured.getSource()).isEqualTo("commerce-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishOrderPaymentCompletedEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        List<OrderProduct> orderProducts = createOrderProducts();

        // when
        orderEventKafkaProducer.publishOrderPaymentCompletedEvent(
                10L, 50L, 80000L, 5000L, orderProducts, 1500L
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.ORDER_PAYMENT_COMPLETED);
        assertThat(capturedEnvelope.source()).isEqualTo("commerce-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        OrderPaymentCompletedEvent payload = (OrderPaymentCompletedEvent) capturedEnvelope.payload();
        assertThat(payload.orderId()).isEqualTo(10L);
        assertThat(payload.buyerId()).isEqualTo(50L);
        assertThat(payload.totalAmount()).isEqualTo(80000L);
        assertThat(payload.pointAmount()).isEqualTo(5000L);
        assertThat(payload.orderProducts()).hasSize(2);

        OrderPaymentCompletedEvent.OrderProductItem firstItem = payload.orderProducts().get(0);
        assertThat(firstItem.pricePolicyId()).isEqualTo(100L);
        assertThat(firstItem.sharerId()).isEqualTo(200L);
        assertThat(firstItem.quantity()).isEqualTo(2);
        assertThat(firstItem.unitAmount()).isEqualTo(30000L);

        OrderPaymentCompletedEvent.OrderProductItem secondItem = payload.orderProducts().get(1);
        assertThat(secondItem.pricePolicyId()).isEqualTo(101L);
        assertThat(secondItem.sharerId()).isNull();
        assertThat(secondItem.quantity()).isEqualTo(1);
        assertThat(secondItem.unitAmount()).isEqualTo(20000L);

        assertThat(payload.totalAccumulatedPoint()).isEqualTo(1500L);
    }
}
