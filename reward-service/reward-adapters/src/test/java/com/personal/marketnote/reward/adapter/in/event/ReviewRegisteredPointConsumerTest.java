package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.utility.AccrualPointAmountConstant;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
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
@DisplayName("ReviewRegisteredPointConsumer 테스트")
class ReviewRegisteredPointConsumerTest {
    @InjectMocks
    private ReviewRegisteredPointConsumer consumer;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long reviewerId, Long reviewId) {
        ReviewRegisteredEvent event = new ReviewRegisteredEvent(
                1L, 2L, 50L, 10, 4.5f, reviewerId, reviewId
        );
        EventEnvelope<ReviewRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "community.review.registered", "community-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        return new ConsumerRecord<>("community.review.registered", 0, 0L, "1", envelope);
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 500포인트를 적립하고 acknowledge한다")
    void handleReviewRegisteredEvent_validEvent_accrulesAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 200L);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(captor.capture());

        ModifyUserPointCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(100L);
        assertThat(command.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(command.amount()).isEqualTo(500L);
        assertThat(command.sourceType()).isEqualTo(UserPointSourceType.REVIEW);
        assertThat(command.sourceId()).isEqualTo(200L);
        assertThat(command.reason()).isEqualTo(AccrualPointAmountConstant.REVIEW_POINT_REASON);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewerId가 null이면 적립 없이 acknowledge한다")
    void handleReviewRegisteredEvent_nullReviewerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 200L);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewId가 null이면 적립 없이 acknowledge한다")
    void handleReviewRegisteredEvent_nullReviewId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, null);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 멱등 처리하고 acknowledge한다")
    void handleReviewRegisteredEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 200L);
        doThrow(new DuplicateUserPointHistoryException(100L, UserPointSourceType.REVIEW, 200L, "리뷰 등록 적립"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 적립 없이 acknowledge한다")
    void handleReviewRegisteredEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.review.registered", 0, 0L, "1", null
        );

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 적립 없이 acknowledge한다")
    void handleReviewRegisteredEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        ReviewRegisteredEvent event = new ReviewRegisteredEvent(
                1L, 2L, 50L, 10, 4.5f, 100L, 200L
        );
        EventEnvelope<ReviewRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "community-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.review.registered", 0, 0L, "1", envelope
        );

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleReviewRegisteredEvent_unexpectedException_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 200L);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleReviewRegisteredEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("reviewerId가 0이면 적립 없이 acknowledge한다")
    void handleReviewRegisteredEvent_zeroReviewerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 200L);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewId가 0이면 적립 없이 acknowledge한다")
    void handleReviewRegisteredEvent_zeroReviewId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 0L);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }
}
