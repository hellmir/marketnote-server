package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.EventRegisteredEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.port.in.command.SendBatchNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendBatchNotificationUseCase;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventRegisteredNotificationConsumer {

    private static final int MAX_BATCH_SIZE = 10_000;

    private final SendBatchNotificationUseCase sendBatchNotificationUseCase;
    private final FindNotificationPreferencePort findNotificationPreferencePort;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.EVENT_REGISTERED,
            groupId = "notification-event-registered"
    )
    public void handleEventRegisteredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.EVENT_REGISTERED)) {
            acknowledgment.acknowledge();
            return;
        }

        EventRegisteredEvent payload = envelope.getPayloadAs(EventRegisteredEvent.class, objectMapper);

        log.info("이벤트 등록 이벤트 수신 (알림 발송). eventId={}, postId={}, title={}",
                envelope.eventId(), payload.postId(), payload.title());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("postId", payload.postId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.title())) {
            log.warn("이벤트 제목 누락. eventId={}, postId={}", envelope.eventId(), payload.postId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            List<Long> allUserIds = findNotificationPreferencePort.findAllDistinctUserIds();

            Map<String, String> variables = Map.of(
                    "post_id", String.valueOf(payload.postId()),
                    "event_title", payload.title()
            );

            for (int i = 0; i < allUserIds.size(); i += MAX_BATCH_SIZE) {
                List<Long> batchUserIds = allUserIds.subList(i, Math.min(i + MAX_BATCH_SIZE, allUserIds.size()));

                SendBatchNotificationCommand command = new SendBatchNotificationCommand(
                        batchUserIds,
                        "EVENT_REGISTERED",
                        variables,
                        "PUSH_AND_IN_APP",
                        null
                );
                sendBatchNotificationUseCase.sendBatchNotification(command);

                log.info("이벤트 알림 배치 발송 완료. postId={}, batchIndex={}, batchSize={}",
                        payload.postId(), i / MAX_BATCH_SIZE, batchUserIds.size());
            }

            log.info("이벤트 알림 전체 발송 완료. postId={}, totalUsers={}", payload.postId(), allUserIds.size());
        } catch (Exception e) {
            log.error("이벤트 알림 발송 실패. eventId={}, postId={}, error={}",
                    envelope.eventId(), payload.postId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
