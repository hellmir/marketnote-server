package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventKafkaProducerPublishOrderReturnedEventTest {
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-09T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private OrderEventKafkaProducer orderEventKafkaProducer;

    @Test
    @DisplayName("반품 완료 이벤트를 Outbox에 저장한다")
    void publishOrderReturnedEvent_savesToOutbox() {
        // given
        Long orderId = 1L;
        String orderKey = "ORDER-KEY-001";
        Long buyerId = 100L;
        Long returnAmount = 50000L;
        Long paymentAmount = 45000L;
        Long pointAmount = 5000L;
        Long shippingFee = 3000L;
        boolean isFullReturn = true;

        UUID sharerKey = UUID.randomUUID();
        OrderProduct returnProduct = OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(orderId)
                .sellerId(10L)
                .pricePolicyId(200L)
                .sharerKey(sharerKey)
                .quantity(2)
                .unitAmount(25000L)
                .orderStatus(OrderStatus.RETURNED)
                .build());

        // when
        orderEventKafkaProducer.publishOrderReturnedEvent(
                orderId, orderKey, buyerId, returnAmount, paymentAmount,
                pointAmount, shippingFee, isFullReturn, List.of(returnProduct)
        );

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        OutboxEvent savedEvent = captor.getValue();
        assertThat(savedEvent.getTopic()).isEqualTo(KafkaTopicConstants.ORDER_RETURNED);
        assertThat(savedEvent.getPartitionKey()).isEqualTo(orderId.toString());
        assertThat(savedEvent.getPayload()).contains("\"orderId\":1");
        assertThat(savedEvent.getPayload()).contains("\"orderKey\":\"ORDER-KEY-001\"");
        assertThat(savedEvent.getPayload()).contains("\"returnAmount\":50000");
        assertThat(savedEvent.getPayload()).contains("\"isFullReturn\":true");
    }

    @Test
    @DisplayName("반품 완료 이벤트 페이로드에 반품 상품 목록이 포함된다")
    void publishOrderReturnedEvent_includesReturnProducts() {
        // given
        Long orderId = 2L;
        UUID sharerKey1 = UUID.randomUUID();
        UUID sharerKey2 = UUID.randomUUID();

        OrderProduct product1 = OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(orderId).sellerId(10L).pricePolicyId(100L)
                .sharerKey(sharerKey1).quantity(1).unitAmount(30000L)
                .orderStatus(OrderStatus.RETURNED).build());
        OrderProduct product2 = OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(orderId).sellerId(10L).pricePolicyId(200L)
                .sharerKey(sharerKey2).quantity(3).unitAmount(10000L)
                .orderStatus(OrderStatus.RETURNED).build());

        // when
        orderEventKafkaProducer.publishOrderReturnedEvent(
                orderId, "ORDER-KEY-002", 100L, 60000L, 55000L,
                5000L, 0L, true, List.of(product1, product2)
        );

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        String payload = captor.getValue().getPayload();
        assertThat(payload).contains("\"pricePolicyId\":100");
        assertThat(payload).contains("\"pricePolicyId\":200");
        assertThat(payload).contains("\"quantity\":1");
        assertThat(payload).contains("\"quantity\":3");
    }
}
