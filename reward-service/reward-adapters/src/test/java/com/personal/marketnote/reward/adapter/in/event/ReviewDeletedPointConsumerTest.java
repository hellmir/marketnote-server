package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewDeletedEvent;
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
@DisplayName("ReviewDeletedPointConsumer 테스트")
class ReviewDeletedPointConsumerTest {
    @InjectMocks
    private ReviewDeletedPointConsumer consumer;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long reviewerId, Long reviewId) {
        ReviewDeletedEvent event = new ReviewDeletedEvent(
                reviewId, 50L, 9, 4.2f, reviewerId
        );
        EventEnvelope<ReviewDeletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", KafkaTopicConstants.REVIEW_DELETED, "community-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        return new ConsumerRecord<>(KafkaTopicConstants.REVIEW_DELETED, 0, 0L, "50", envelope);
    }

    @Test
    @DisplayName("리뷰 삭제 이벤트 수신 시 500포인트를 차감하고 acknowledge한다")
    void handleReviewDeletedEvent_validEvent_deductsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 200L);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(captor.capture());

        ModifyUserPointCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(100L);
        assertThat(command.changeType()).isEqualTo(UserPointChangeType.DEDUCTION);
        assertThat(command.amount()).isEqualTo(500L);
        assertThat(command.sourceType()).isEqualTo(UserPointSourceType.REVIEW);
        assertThat(command.sourceId()).isEqualTo(200L);
        assertThat(command.reason()).isEqualTo(AccrualPointAmountConstant.REVIEW_POINT_DEDUCTION_REASON);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 멱등 처리하고 acknowledge한다")
    void handleReviewDeletedEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 200L);
        doThrow(new DuplicateUserPointHistoryException(100L, UserPointSourceType.REVIEW, 200L, "리뷰 삭제 차감"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 차감 없이 acknowledge한다")
    void handleReviewDeletedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_DELETED, 0, 0L, "50", null
        );

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 차감 없이 acknowledge한다")
    void handleReviewDeletedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        ReviewDeletedEvent event = new ReviewDeletedEvent(200L, 50L, 9, 4.2f, 100L);
        EventEnvelope<ReviewDeletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "community-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_DELETED, 0, 0L, "50", envelope
        );

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewerId가 null이면 차감 없이 acknowledge한다")
    void handleReviewDeletedEvent_nullReviewerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 200L);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }
}
