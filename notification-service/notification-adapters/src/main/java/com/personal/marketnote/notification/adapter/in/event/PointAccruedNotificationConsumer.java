package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.PointAccruedEvent;
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
public class PointAccruedNotificationConsumer {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.POINT_ACCRUED,
            groupId = "notification-point-accrued"
    )
    public void handlePointAccruedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.POINT_ACCRUED)) {
            acknowledgment.acknowledge();
            return;
        }

        PointAccruedEvent payload = envelope.getPayloadAs(PointAccruedEvent.class, objectMapper);

        log.info("포인트 적립 이벤트 수신 (알림 발송). eventId={}, userId={}, amount={}",
                envelope.eventId(), payload.userId(), payload.amount());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("userId", payload.userId()),
                EventPayloadValidator.id("amount", payload.amount()))) {
            acknowledgment.acknowledge();
            return;
        }

        SendNotificationCommand command = new SendNotificationCommand(
                payload.userId(),
                "POINT_ACCRUAL",
                Map.of("amount", String.valueOf(payload.amount())),
                "PUSH_ONLY",
                null
        );
        sendNotificationUseCase.sendNotification(command);

        log.info("포인트 적립 알림 발송 완료. userId={}, amount={}", payload.userId(), payload.amount());

        acknowledgment.acknowledge();
    }
}
