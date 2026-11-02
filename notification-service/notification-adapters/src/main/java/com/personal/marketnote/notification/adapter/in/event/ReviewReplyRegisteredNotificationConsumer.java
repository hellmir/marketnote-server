package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ReviewReplyRegisteredEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewReplyRegisteredNotificationConsumer {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.REVIEW_REPLY_REGISTERED,
            groupId = "notification-review-reply"
    )
    public void handleReviewReplyRegisteredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.REVIEW_REPLY_REGISTERED)) {
            acknowledgment.acknowledge();
            return;
        }

        ReviewReplyRegisteredEvent payload = envelope.getPayloadAs(ReviewReplyRegisteredEvent.class, objectMapper);

        log.info("리뷰 답글 등록 이벤트 수신 (알림 발송). eventId={}, userId={}, reviewId={}",
                envelope.eventId(), payload.userId(), payload.reviewId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("userId", payload.userId()),
                EventPayloadValidator.id("reviewId", payload.reviewId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            String productName = FormatValidator.hasValue(payload.productName()) ? payload.productName() : "";
            SendNotificationCommand command = new SendNotificationCommand(
                    payload.userId(),
                    "REVIEW_REPLY",
                    Map.of("product_name", productName),
                    "PUSH_ONLY",
                    null
            );
            sendNotificationUseCase.sendNotification(command);

            log.info("리뷰 답글 알림 발송 완료. userId={}, reviewId={}", payload.userId(), payload.reviewId());
        } catch (Exception e) {
            log.error("리뷰 답글 알림 발송 실패. eventId={}, userId={}, reviewId={}, error={}",
                    envelope.eventId(), payload.userId(), payload.reviewId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
