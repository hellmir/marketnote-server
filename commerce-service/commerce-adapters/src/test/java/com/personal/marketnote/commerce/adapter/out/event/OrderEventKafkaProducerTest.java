package com.personal.marketnote.commerce.adapter.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventKafkaProducer 테스트")
class OrderEventKafkaProducerTest {
    @InjectMocks
    private OrderEventKafkaProducer orderEventKafkaProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

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
    @DisplayName("주문 결제 완료 이벤트 발행 시 올바른 토픽과 파티션 키로 전송된다")
    void publishOrderPaymentCompletedEvent_sendsToCorrectTopicWithOrderIdKey() {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        List<OrderProduct> orderProducts = createOrderProducts();

        // when
        orderEventKafkaProducer.publishOrderPaymentCompletedEvent(
                1L, 50L, 80000L, 5000L, orderProducts
        );

        // then
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.ORDER_PAYMENT_COMPLETED),
                eq("1"),
                any(EventEnvelope.class)
        );
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishOrderPaymentCompletedEvent_envelopeContainsCorrectPayload() {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        List<OrderProduct> orderProducts = createOrderProducts();

        // when
        orderEventKafkaProducer.publishOrderPaymentCompletedEvent(
                10L, 50L, 80000L, 5000L, orderProducts
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.ORDER_PAYMENT_COMPLETED),
                eq("10"),
                envelopeCaptor.capture()
        );

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
    }
}
