package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCodes;
import com.personal.marketnote.notification.port.in.command.AligoAlimTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendAligoAlimTalkUseCase;
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
public class ShippingCompletedAlimTalkConsumer {

    private static final String DELIVERED_STATUS = "DELIVERED";

    private final SendAligoAlimTalkUseCase sendAligoAlimTalkUseCase;
    private final ObjectMapper objectMapper;
    private final AligoProperties aligoProperties;

    @KafkaListener(
            topics = KafkaTopicConstants.SHIPPING_STATUS_CHANGED,
            groupId = "notification-alimtalk-shipping"
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

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()),
                EventPayloadValidator.id("buyerId", payload.buyerId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.buyerPhoneNumber())) {
            log.warn("배송 완료 알림톡 발송 생략: 구매자 전화번호 없음. eventId={}, orderId={}",
                    envelope.eventId(), payload.orderId());
            acknowledgment.acknowledge();
            return;
        }

        log.info("배송 완료 이벤트 수신 (알림톡 발송). eventId={}, orderId={}, buyerId={}",
                envelope.eventId(), payload.orderId(), payload.buyerId());

        try {
            SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                    AligoTemplateCodes.SHIPPING_COMPLETED,
                    aligoProperties.getSender(),
                    Map.of("order_id", String.valueOf(payload.orderId())),
                    List.of(new AligoAlimTalkRecipientCommand(
                            payload.buyerPhoneNumber(), payload.buyerName()
                    )),
                    null
            );
            sendAligoAlimTalkUseCase.sendAlimTalk(command);

            log.info("배송 완료 알림톡 발송 완료. orderId={}, buyerId={}",
                    payload.orderId(), payload.buyerId());
        } catch (Exception e) {
            log.error("배송 완료 알림톡 발송 실패. eventId={}, orderId={}, buyerId={}, error={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
