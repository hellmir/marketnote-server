package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseConfirmationRequestNotificationConsumer {

    private static final String DELIVERED_STATUS = "DELIVERED";

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.SHIPPING_STATUS_CHANGED,
            groupId = "notification-purchase-confirmation"
    )
    public void handleShippingStatusChangedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.SHIPPING_STATUS_CHANGED)) {
            acknowledgment.acknowledge();
            return;
        }

        ShippingStatusChangedEvent payload = envelope.getPayloadAs(ShippingStatusChangedEvent.class, objectMapper);

        if (!DELIVERED_STATUS.equals(payload.shippingStatus())) {
            acknowledgment.acknowledge();
            return;
        }

        log.info("배송 완료 이벤트 수신 (구매 확정 요청 알림 예약). eventId={}, orderId={}, buyerId={}",
                envelope.eventId(), payload.orderId(), payload.buyerId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()),
                EventPayloadValidator.id("buyerId", payload.buyerId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.occurredAt())) {
            log.warn("occurredAt이 null입니다. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            LocalDateTime scheduledAt = payload.occurredAt().plusDays(1);

            SendNotificationCommand command = new SendNotificationCommand(
                    payload.buyerId(),
                    "PURCHASE_CONFIRMATION_REQUEST",
                    Map.of("order_id", String.valueOf(payload.orderId())),
                    "PUSH_AND_IN_APP",
                    scheduledAt
            );
            sendNotificationUseCase.sendNotification(command);

            log.info("구매 확정 요청 알림 예약 완료. orderId={}, buyerId={}, scheduledAt={}",
                    payload.orderId(), payload.buyerId(), scheduledAt);
        } catch (Exception e) {
            log.error("구매 확정 요청 알림 발송 실패. eventId={}, orderId={}, buyerId={}, error={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
