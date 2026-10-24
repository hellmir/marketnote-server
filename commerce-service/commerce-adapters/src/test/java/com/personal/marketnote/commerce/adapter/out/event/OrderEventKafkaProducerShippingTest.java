package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.domain.order.ShippingAddressCreateState;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventKafkaProducer 배송지/productId 보강 테스트")
class OrderEventKafkaProducerShippingTest {

    @InjectMocks
    private OrderEventKafkaProducer orderEventKafkaProducer;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    private void setUpClock() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-11T10:00:00Z"), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("OrderEventKafkaProducer가 배송지 정보와 productId를 포함한 이벤트를 Outbox에 저장한다")
    @SuppressWarnings("unchecked")
    void shouldIncludeShippingAddressAndProductIdInPayload() throws Exception {
        // given
        setUpClock();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressCreateState.builder().recipientName("홍길동").recipientPhoneNumber("010-1234-5678").zipCode("06234").address("서울특별시 강남구 테헤란로 123").addressDetail("4층 401호").deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR).deliveryRequestMessage("문 앞에 놓아주세요").build());

        OrderProduct product = OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .productId(42L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .sharerKey(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .quantity(2)
                .unitAmount(30000L)
                .orderStatus(OrderStatus.PAID)
                .accumulatedPoint(500L)
                .build());

        // when
        orderEventKafkaProducer.publishOrderPaymentCompletedEvent(
                1L, 50L, 60000L, 5000L, shippingAddress, List.of(product), 1000L
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        OrderPaymentCompletedEvent payload = (OrderPaymentCompletedEvent) envelopeCaptor.getValue().payload();

        assertThat(payload.recipientName()).isEqualTo("홍길동");
        assertThat(payload.recipientPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(payload.zipCode()).isEqualTo("06234");
        assertThat(payload.address()).isEqualTo("서울특별시 강남구 테헤란로 123");
        assertThat(payload.addressDetail()).isEqualTo("4층 401호");
        assertThat(payload.deliveryRequestMessage()).isEqualTo("문 앞에 놓아주세요");

        OrderPaymentCompletedEvent.OrderProductItem item = payload.orderProducts().get(0);
        assertThat(item.productId()).isEqualTo(42L);
        assertThat(item.pricePolicyId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("ShippingAddress가 null이면 배송지 필드가 모두 null로 저장된다")
    @SuppressWarnings("unchecked")
    void shouldHandleNullShippingAddress() throws Exception {
        // given
        setUpClock();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        OrderProduct product = OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .quantity(1)
                .unitAmount(50000L)
                .orderStatus(OrderStatus.PAID)
                .accumulatedPoint(0L)
                .build());

        // when
        orderEventKafkaProducer.publishOrderPaymentCompletedEvent(
                1L, 50L, 50000L, 0L, null, List.of(product), 0L
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        OrderPaymentCompletedEvent payload = (OrderPaymentCompletedEvent) envelopeCaptor.getValue().payload();

        assertThat(payload.recipientName()).isNull();
        assertThat(payload.recipientPhoneNumber()).isNull();
        assertThat(payload.zipCode()).isNull();
        assertThat(payload.address()).isNull();
        assertThat(payload.addressDetail()).isNull();
        assertThat(payload.deliveryRequestMessage()).isNull();
    }

    @Test
    @DisplayName("기존 Consumer가 신규 필드 추가 후에도 정상 역직렬화된다")
    void shouldDeserializeEventWithNewFieldsUsingJackson() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        String jsonWithNewFields = """
                {
                    "orderId": 1,
                    "buyerId": 10,
                    "totalAmount": 50000,
                    "pointAmount": 5000,
                    "recipientName": "홍길동",
                    "recipientPhoneNumber": "010-1234-5678",
                    "zipCode": "06234",
                    "address": "서울시 강남구",
                    "addressDetail": "101호",
                    "deliveryRequestMessage": "문 앞에 놓아주세요",
                    "orderProducts": [
                        {"pricePolicyId": 100, "productId": 42, "sharerKey": null, "quantity": 1, "unitAmount": 50000}
                    ],
                    "totalAccumulatedPoint": 500
                }
                """;

        // when
        OrderPaymentCompletedEvent event = mapper.readValue(jsonWithNewFields, OrderPaymentCompletedEvent.class);

        // then
        assertThat(event.recipientName()).isEqualTo("홍길동");
        assertThat(event.orderProducts().get(0).productId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("신규 필드 없는 기존 메시지도 정상 역직렬화된다 (하위 호환)")
    void shouldDeserializeEventWithoutNewFieldsForBackwardCompatibility() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        String jsonWithoutNewFields = """
                {
                    "orderId": 1,
                    "buyerId": 10,
                    "totalAmount": 50000,
                    "pointAmount": 5000,
                    "orderProducts": [
                        {"pricePolicyId": 100, "sharerKey": null, "quantity": 1, "unitAmount": 50000}
                    ],
                    "totalAccumulatedPoint": 500
                }
                """;

        // when
        OrderPaymentCompletedEvent event = mapper.readValue(jsonWithoutNewFields, OrderPaymentCompletedEvent.class);

        // then
        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.recipientName()).isNull();
        assertThat(event.recipientPhoneNumber()).isNull();
        assertThat(event.orderProducts().get(0).productId()).isNull();
    }
}
