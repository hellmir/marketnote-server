package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.fulfillment.exception.ShippingTrackerAlreadyExistsException;
import com.personal.marketnote.fulfillment.port.in.command.CreateShippingTrackerCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.CreateShippingTrackerUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedOutboundConsumer 테스트")
class OrderPaymentCompletedOutboundConsumerTest {

    @InjectMocks
    private OrderPaymentCompletedOutboundConsumer consumer;

    @Mock
    private CreateShippingTrackerUseCase createShippingTrackerUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long orderId, Long buyerId, List<OrderPaymentCompletedEvent.OrderProductItem> orderProducts
    ) {
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                orderId, buyerId, 50000L, 1000L, orderProducts, 500L
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
                "commerce-service",
                LocalDateTime.of(2026, 4, 14, 10, 0),
                event
        );
        return new ConsumerRecord<>(
                KafkaTopicConstants.ORDER_PAYMENT_COMPLETED, 0, 0L, "key-1", envelope
        );
    }

    private List<OrderPaymentCompletedEvent.OrderProductItem> buildOrderProducts() {
        return List.of(
                new OrderPaymentCompletedEvent.OrderProductItem(1L, UUID.randomUUID(), 2, 10000L),
                new OrderPaymentCompletedEvent.OrderProductItem(2L, UUID.randomUUID(), 1, 30000L)
        );
    }

    @Nested
    @DisplayName("성공")
    class Success {

        @Test
        @DisplayName("정상 이벤트 수신 시 배송 추적을 생성하고 acknowledge한다")
        void shouldCreateShippingTrackerAndAcknowledge() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 1L, buildOrderProducts());

            // when
            consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

            // then
            ArgumentCaptor<CreateShippingTrackerCommand> captor = ArgumentCaptor.forClass(CreateShippingTrackerCommand.class);
            verify(createShippingTrackerUseCase).createShippingTracker(captor.capture());

            CreateShippingTrackerCommand command = captor.getValue();
            assertThat(command.orderId()).isEqualTo(100L);
            assertThat(command.buyerId()).isEqualTo(1L);
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("실패")
    class Failure {

        @Test
        @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
        void shouldAcknowledgeWhenEnvelopeIsNull() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.ORDER_PAYMENT_COMPLETED, 0, 0L, "key-1", null
            );

            // when
            consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

            // then
            verifyNoInteractions(createShippingTrackerUseCase);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
        void shouldAcknowledgeWhenEventTypeMismatch() {
            // given
            OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                    1L, 1L, 50000L, 1000L, buildOrderProducts(), 500L
            );
            EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                    "test-event-id", "wrong.event.type", "commerce-service",
                    LocalDateTime.of(2026, 4, 14, 10, 0), event
            );
            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.ORDER_PAYMENT_COMPLETED, 0, 0L, "key-1", envelope
            );

            // when
            consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

            // then
            verifyNoInteractions(createShippingTrackerUseCase);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
        void shouldAcknowledgeWhenOrderIdIsNull() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 1L, buildOrderProducts());

            // when
            consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

            // then
            verifyNoInteractions(createShippingTrackerUseCase);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("orderProducts가 빈 목록이면 UseCase를 호출하지 않고 acknowledge한다")
        void shouldAcknowledgeWhenOrderProductsEmpty() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 1L, List.of());

            // when
            consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

            // then
            verifyNoInteractions(createShippingTrackerUseCase);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("ShippingTrackerAlreadyExistsException 발생 시 acknowledge한다")
        void shouldAcknowledgeWhenShippingTrackerAlreadyExists() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 1L, buildOrderProducts());
            doThrow(new ShippingTrackerAlreadyExistsException(1L))
                    .when(createShippingTrackerUseCase).createShippingTracker(any(CreateShippingTrackerCommand.class));

            // when
            consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

            // then
            verify(createShippingTrackerUseCase).createShippingTracker(any(CreateShippingTrackerCommand.class));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("기타 예외 발생 시 예외를 전파하고 acknowledge하지 않는다")
        void shouldPropagateExceptionAndNotAcknowledge() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 1L, buildOrderProducts());
            doThrow(new RuntimeException("네트워크 오류"))
                    .when(createShippingTrackerUseCase).createShippingTracker(any(CreateShippingTrackerCommand.class));

            // when & then
            assertThatThrownBy(() -> consumer.handleOrderPaymentCompletedEvent(record, acknowledgment))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("네트워크 오류");

            verify(createShippingTrackerUseCase).createShippingTracker(any(CreateShippingTrackerCommand.class));
            verify(acknowledgment, never()).acknowledge();
        }
    }
}
