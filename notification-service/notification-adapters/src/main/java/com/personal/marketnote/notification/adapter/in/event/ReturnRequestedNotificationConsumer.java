package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ReturnRequestedEvent;
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
public class ReturnRequestedNotificationConsumer {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.RETURN_REQUESTED,
            groupId = "notification-order"
    )
    public void handleReturnRequestedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.RETURN_REQUESTED)) {
            acknowledgment.acknowledge();
            return;
        }

        ReturnRequestedEvent payload = envelope.getPayloadAs(ReturnRequestedEvent.class, objectMapper);

        log.info("반품 신청 이벤트 수신 (알림 발송). eventId={}, orderId={}, buyerId={}",
                envelope.eventId(), payload.orderId(), payload.buyerId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()),
                EventPayloadValidator.id("buyerId", payload.buyerId()))) {
            acknowledgment.acknowledge();
            return;
        }

        SendNotificationCommand command = new SendNotificationCommand(
                payload.buyerId(),
                "RETURN_REQUESTED",
                Map.of("order_id", String.valueOf(payload.orderId())),
                "PUSH_AND_IN_APP",
                null
        );
        sendNotificationUseCase.sendNotification(command);

        log.info("반품 신청 알림 발송 완료. orderId={}, buyerId={}", payload.orderId(), payload.buyerId());

        acknowledgment.acknowledge();
    }
}
