package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.InquiryAnsweredEvent;
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
public class ProductInquiryAnsweredNotificationConsumer {

    private static final String PRODUCT_INQUERY_BOARD = "PRODUCT_INQUERY";

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.INQUIRY_ANSWERED,
            groupId = "notification-product-inquiry-answered"
    )
    public void handleInquiryAnsweredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.INQUIRY_ANSWERED)) {
            acknowledgment.acknowledge();
            return;
        }

        InquiryAnsweredEvent payload = envelope.getPayloadAs(InquiryAnsweredEvent.class, objectMapper);

        if (!PRODUCT_INQUERY_BOARD.equals(payload.board())) {
            acknowledgment.acknowledge();
            return;
        }

        log.info("상품 문의 답변 이벤트 수신 (알림 발송). eventId={}, userId={}, postId={}",
                envelope.eventId(), payload.userId(), payload.postId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("userId", payload.userId()),
                EventPayloadValidator.id("postId", payload.postId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.title())) {
            log.warn("문의 제목 누락. eventId={}, postId={}", envelope.eventId(), payload.postId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            SendNotificationCommand command = new SendNotificationCommand(
                    payload.userId(),
                    "PRODUCT_INQUIRY_REPLY",
                    Map.of(
                            "post_id", String.valueOf(payload.postId()),
                            "inquiry_title", payload.title()
                    ),
                    "PUSH_ONLY",
                    null
            );
            sendNotificationUseCase.sendNotification(command);

            log.info("상품 문의 답변 알림 발송 완료. userId={}, postId={}", payload.userId(), payload.postId());
        } catch (Exception e) {
            log.error("상품 문의 답변 알림 발송 실패. eventId={}, userId={}, postId={}, error={}",
                    envelope.eventId(), payload.userId(), payload.postId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
