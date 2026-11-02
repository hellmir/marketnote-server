package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewReplyRegisteredEvent;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewReplyRegisteredNotificationConsumerTest {

    @InjectMocks
    private ReviewReplyRegisteredNotificationConsumer consumer;

    @Mock
    private SendNotificationUseCase sendNotificationUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long userId, Long reviewId, String productName) {
        ReviewReplyRegisteredEvent event = new ReviewReplyRegisteredEvent(userId, reviewId, productName);
        EventEnvelope<ReviewReplyRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "community.review.reply-registered", "community-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        return new ConsumerRecord<>("community.review.reply-registered", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("리뷰 답글 등록 이벤트 수신 시 SendNotificationUseCase를 호출하고 acknowledge한다")
    void shouldSendNotificationAndAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, "테스트 상품");

        // when
        consumer.handleReviewReplyRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationUseCase).sendNotification(captor.capture());

        SendNotificationCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(1L);
        assertThat(command.templateCode()).isEqualTo("REVIEW_REPLY");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_ONLY");
        assertThat(command.variables()).containsEntry("product_name", "테스트 상품");
        assertThat(command.scheduledAt()).isNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.review.reply-registered", 0, 0L, "1", null
        );

        // when
        consumer.handleReviewReplyRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        ReviewReplyRegisteredEvent event = new ReviewReplyRegisteredEvent(1L, 100L, "테스트 상품");
        EventEnvelope<ReviewReplyRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "community-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.review.reply-registered", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleReviewReplyRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenUserIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, "테스트 상품");

        // when
        consumer.handleReviewReplyRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenReviewIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, "테스트 상품");

        // when
        consumer.handleReviewReplyRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productName이 null이면 빈 문자열로 대체하여 알림을 발송하고 acknowledge한다")
    void shouldReplaceNullProductNameWithEmptyString() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, null);

        // when
        consumer.handleReviewReplyRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationUseCase).sendNotification(captor.capture());
        assertThat(captor.getValue().variables()).containsEntry("product_name", "");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("sendNotification 호출 중 예외가 발생해도 acknowledge한다")
    void shouldAcknowledgeEvenWhenSendNotificationFails() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, "테스트 상품");
        doThrow(new RuntimeException("발송 실패")).when(sendNotificationUseCase).sendNotification(any());

        // when
        consumer.handleReviewReplyRegisteredEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }
}
