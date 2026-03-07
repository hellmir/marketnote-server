package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.order.UpdateOrderProductReviewStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.UpdateOrderProductUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewRegisteredCommerceConsumerTest {
    @InjectMocks
    private ReviewRegisteredCommerceConsumer reviewRegisteredCommerceConsumer;

    @Mock
    private UpdateOrderProductUseCase updateOrderProductUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long pricePolicyId) {
        ReviewRegisteredEvent event = new ReviewRegisteredEvent(orderId, pricePolicyId);
        EventEnvelope<ReviewRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "community.review.registered", "community-service",
                LocalDateTime.of(2026, 3, 2, 10, 0), event
        );
        return new ConsumerRecord<>("community.review.registered", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 주문상품 리뷰 상태 업데이트 UseCase를 호출하고 acknowledge한다")
    void handleReviewRegisteredEvent_success_updatesReviewStatusAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);

        // when
        reviewRegisteredCommerceConsumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<UpdateOrderProductReviewStatusCommand> captor =
                ArgumentCaptor.forClass(UpdateOrderProductReviewStatusCommand.class);
        verify(updateOrderProductUseCase).updateReviewStatus(captor.capture());

        UpdateOrderProductReviewStatusCommand command = captor.getValue();
        assertThat(command.orderId()).isEqualTo(1L);
        assertThat(command.pricePolicyId()).isEqualTo(2L);
        assertThat(command.isReviewed()).isTrue();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleReviewRegisteredEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 2L);

        // when
        reviewRegisteredCommerceConsumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(updateOrderProductUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleReviewRegisteredEvent_nullPricePolicyId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null);

        // when
        reviewRegisteredCommerceConsumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(updateOrderProductUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleReviewRegisteredEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        doThrow(new RuntimeException("네트워크 오류"))
                .when(updateOrderProductUseCase).updateReviewStatus(any(UpdateOrderProductReviewStatusCommand.class));

        // when & then
        assertThatThrownBy(() -> reviewRegisteredCommerceConsumer.handleReviewRegisteredEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("네트워크 오류");

        verify(updateOrderProductUseCase).updateReviewStatus(any(UpdateOrderProductReviewStatusCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
