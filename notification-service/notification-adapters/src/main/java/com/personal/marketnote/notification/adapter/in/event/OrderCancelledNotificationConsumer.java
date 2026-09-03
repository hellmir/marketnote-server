package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
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
public class OrderCancelledNotificationConsumer {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_CANCELLED,
            groupId = "notification-order"
    )
    public void handleOrderCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_CANCELLED)) {
            acknowledgment.acknowledge();
            return;
        }

        OrderCancelledEvent payload = envelope.getPayloadAs(OrderCancelledEvent.class, objectMapper);

        log.info("주문 취소 완료 이벤트 수신 (알림 발송). eventId={}, orderId={}, buyerId={}",
                envelope.eventId(), payload.orderId(), payload.buyerId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()),
                EventPayloadValidator.id("buyerId", payload.buyerId()))) {
            acknowledgment.acknowledge();
            return;
        }

        SendNotificationCommand command = new SendNotificationCommand(
                payload.buyerId(),
                "ORDER_CANCELLED",
                Map.of("order_id", String.valueOf(payload.orderId())),
                "PUSH_AND_IN_APP",
                null
        );
        sendNotificationUseCase.sendNotification(command);

        log.info("주문 취소 완료 알림 발송 완료. orderId={}, buyerId={}", payload.orderId(), payload.buyerId());

        acknowledgment.acknowledge();
    }
}
