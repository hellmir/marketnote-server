package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
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
public class PurchaseConfirmationCompletedInAppNotificationConsumer {

    private static final String TEMPLATE_CODE_AUTO = "PURCHASE_CONFIRMATION_COMPLETED_AUTO";
    private static final String TEMPLATE_CODE_MANUAL = "PURCHASE_CONFIRMATION_COMPLETED_MANUAL";

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED,
            groupId = "notification-purchase-confirmation-inapp"
    )
    public void handleOrderPurchaseConfirmedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED)) {
            acknowledgment.acknowledge();
            return;
        }

        OrderPurchaseConfirmedEvent payload = envelope.getPayloadAs(OrderPurchaseConfirmedEvent.class, objectMapper);

        String templateCode = resolveTemplateCode(payload.isAutoConfirmed());

        log.info("구매 확정 이벤트 수신 (인앱 알림 생성). eventId={}, orderId={}, buyerId={}, isAutoConfirmed={}",
                envelope.eventId(), payload.orderId(), payload.buyerId(), payload.isAutoConfirmed());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()),
                EventPayloadValidator.id("buyerId", payload.buyerId()))) {
            acknowledgment.acknowledge();
            return;
        }

        SendNotificationCommand command = new SendNotificationCommand(
                payload.buyerId(),
                templateCode,
                Map.of("order_id", String.valueOf(payload.orderId())),
                "IN_APP_ONLY",
                null
        );
        sendNotificationUseCase.sendNotification(command);

        log.info("구매 확정 완료 인앱 알림 생성 완료. orderId={}, buyerId={}, templateCode={}",
                payload.orderId(), payload.buyerId(), templateCode);

        acknowledgment.acknowledge();
    }

    private String resolveTemplateCode(boolean isAutoConfirmed) {
        if (isAutoConfirmed) {
            return TEMPLATE_CODE_AUTO;
        }
        return TEMPLATE_CODE_MANUAL;
    }
}
