package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentAccessTokenIssuanceFailedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.CreateShippingTrackerUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedOutboundConsumer Fassto 출고 요청 테스트")
class OrderPaymentCompletedOutboundDeliveryTest {

    @InjectMocks
    private OrderPaymentCompletedOutboundConsumer consumer;

    @Mock
    private CreateShippingTrackerUseCase createShippingTrackerUseCase;

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    @Mock
    private RegisterFulfillmentDeliveryUseCase registerFulfillmentDeliveryUseCase;

    @Mock
    private FulfillmentAuthProperties fasstoAuthProperties;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-10-11T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private void setUpFasstoAuth() {
        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20260416200000");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(accessToken);
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("TEST_CODE");
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecordWithShipping() {
        List<OrderPaymentCompletedEvent.OrderProductItem> products = List.of(
                new OrderPaymentCompletedEvent.OrderProductItem(100L, 42L, UUID.randomUUID(), 2, 30000L),
                new OrderPaymentCompletedEvent.OrderProductItem(200L, 43L, null, 1, 20000L)
        );
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                1L, 10L, 80000L, 5000L,
                "홍길동", "010-1234-5678", "06234",
                "서울특별시 강남구 테헤란로 123", "4층 401호", "문 앞에 놓아주세요",
                products, 500L
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
                "commerce-service", LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        return new ConsumerRecord<>(KafkaTopicConstants.ORDER_PAYMENT_COMPLETED, 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("결제 완료 이벤트 수신 시 Fassto 출고 요청이 호출된다")
    void shouldCallRegisterDeliveryOnPaymentCompleted() {
        // given
        setUpFasstoAuth();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecordWithShipping();

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(registerFulfillmentDeliveryUseCase).registerDeliveryIdempotent(any(RegisterFulfillmentDeliveryCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 배송지 정보가 RegisterFulfillmentDeliveryItemCommand에 올바르게 매핑된다")
    void shouldMapShippingAddressToDeliveryCommand() {
        // given
        setUpFasstoAuth();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecordWithShipping();

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterFulfillmentDeliveryCommand> captor =
                ArgumentCaptor.forClass(RegisterFulfillmentDeliveryCommand.class);
        verify(registerFulfillmentDeliveryUseCase).registerDeliveryIdempotent(captor.capture());

        RegisterFulfillmentDeliveryCommand command = captor.getValue();
        assertThat(command.customerCode()).isEqualTo("TEST_CODE");
        assertThat(command.accessToken()).isEqualTo("test-token");
        assertThat(command.deliveryRequests()).hasSize(1);

        var itemCommand = command.deliveryRequests().get(0);
        assertThat(itemCommand.orderNumber()).isEqualTo("1");
        assertThat(itemCommand.recipientName()).isEqualTo("홍길동");
        assertThat(itemCommand.recipientPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(itemCommand.recipientAddress()).isEqualTo("(06234) 서울특별시 강남구 테헤란로 123 4층 401호");
        assertThat(itemCommand.shippingRequest()).isEqualTo("문 앞에 놓아주세요");
    }

    @Test
    @DisplayName("convertToGoodsCommands가 productId를 String.valueOf(productId)로 변환하여 productCode에 매핑한다")
    void shouldConvertProductIdToProductCode() {
        // given
        setUpFasstoAuth();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecordWithShipping();

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterFulfillmentDeliveryCommand> captor =
                ArgumentCaptor.forClass(RegisterFulfillmentDeliveryCommand.class);
        verify(registerFulfillmentDeliveryUseCase).registerDeliveryIdempotent(captor.capture());

        var products = captor.getValue().deliveryRequests().get(0).products();
        assertThat(products).hasSize(2);
        assertThat(products.get(0).productCode()).isEqualTo("42");
        assertThat(products.get(0).orderQuantity()).isEqualTo(2);
        assertThat(products.get(1).productCode()).isEqualTo("43");
        assertThat(products.get(1).orderQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 출고 등록된 주문은 FulfillmentDeliveryAlreadyRegisteredException catch 후 warn + acknowledge 처리된다")
    void shouldAcknowledgeWhenDeliveryAlreadyRegistered() {
        // given
        setUpFasstoAuth();
        doThrow(new FulfillmentDeliveryAlreadyRegisteredException(1L))
                .when(registerFulfillmentDeliveryUseCase).registerDeliveryIdempotent(any());
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecordWithShipping();

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("토큰 발급 실패 시 FulfillmentAccessTokenIssuanceFailedException이 발생한다")
    void shouldThrowWhenAccessTokenIssuanceFails() {
        // given
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenThrow(new FulfillmentAccessTokenIssuanceFailedException("test-event", 1L));
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecordWithShipping();

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPaymentCompletedEvent(record, acknowledgment))
                .isInstanceOf(FulfillmentAccessTokenIssuanceFailedException.class);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("주문 상품이 없는 이벤트는 출고 요청을 생략한다")
    void shouldSkipDeliveryWhenNoOrderProducts() {
        // given
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                1L, 10L, 0L, 0L,
                "홍길동", "010-1234-5678", "06234", "서울시", "101호", null,
                List.of(), 0L
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
                "commerce-service", LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.ORDER_PAYMENT_COMPLETED, 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(registerFulfillmentDeliveryUseCase, never()).registerDeliveryIdempotent(any());
        verify(acknowledgment).acknowledge();
    }
}
