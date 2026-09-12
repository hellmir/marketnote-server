package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.InquiryAnsweredEvent;
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
class ProductInquiryAnsweredNotificationConsumerTest {

    @InjectMocks
    private ProductInquiryAnsweredNotificationConsumer consumer;

    @Mock
    private SendNotificationUseCase sendNotificationUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long userId, Long postId, String title, String board) {
        InquiryAnsweredEvent event = new InquiryAnsweredEvent(userId, postId, title, board);
        EventEnvelope<InquiryAnsweredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "community.inquiry.answered", "community-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        return new ConsumerRecord<>("community.inquiry.answered", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("상품 문의 답변 이벤트 수신 시 문의 작성자에게 푸시 알림을 발송하고 acknowledge한다")
    void shouldSendNotificationToInquiryAuthor() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(10L, 200L, "상품 사이즈 문의", "PRODUCT_INQUERY");

        // when
        consumer.handleInquiryAnsweredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationUseCase).sendNotification(captor.capture());

        SendNotificationCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(10L);
        assertThat(command.templateCode()).isEqualTo("PRODUCT_INQUIRY_REPLY");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_AND_IN_APP");
        assertThat(command.variables()).containsEntry("post_id", "200");
        assertThat(command.variables()).containsEntry("inquiry_title", "상품 사이즈 문의");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("board가 ONE_ON_ONE_INQUERY이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenBoardIsOneOnOneInquery() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(10L, 200L, "1:1 문의", "ONE_ON_ONE_INQUERY");

        // when
        consumer.handleInquiryAnsweredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.inquiry.answered", 0, 0L, "1", null
        );

        // when
        consumer.handleInquiryAnsweredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenUserIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 200L, "문의", "PRODUCT_INQUERY");

        // when
        consumer.handleInquiryAnsweredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("title이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenTitleIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(10L, 200L, null, "PRODUCT_INQUERY");

        // when
        consumer.handleInquiryAnsweredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("알림 발송 중 예외가 발생해도 acknowledge한다")
    void shouldAcknowledgeEvenWhenSendFails() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(10L, 200L, "문의", "PRODUCT_INQUERY");
        doThrow(new RuntimeException("발송 실패"))
                .when(sendNotificationUseCase).sendNotification(any());

        // when
        consumer.handleInquiryAnsweredEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }
}
