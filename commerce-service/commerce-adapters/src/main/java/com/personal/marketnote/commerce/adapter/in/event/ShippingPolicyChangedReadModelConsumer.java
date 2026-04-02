package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangedEvent;
import com.personal.marketnote.commerce.port.out.shipping.SaveShippingPolicyReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingPolicyChangedReadModelConsumer {

    private final ObjectMapper objectMapper;
    private final SaveShippingPolicyReadModelPort saveShippingPolicyReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.SHIPPING_POLICY_CHANGED,
            groupId = "commerce-shipping-policy-read-model"
    )
    public void handleShippingPolicyChangedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.SHIPPING_POLICY_CHANGED)) {
            acknowledgment.acknowledge();
            return;
        }

        ShippingPolicyChangedEvent payload = envelope.getPayloadAs(ShippingPolicyChangedEvent.class, objectMapper);

        log.info("배송비 정책 변경 이벤트 수신. eventId={}, sellerId={}, action={}",
                envelope.eventId(), payload.sellerId(), payload.action());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("sellerId", payload.sellerId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (payload.action().isDeleted()) {
            saveShippingPolicyReadModelPort.deactivateBySellerId(payload.sellerId());
            log.info("배송비 정책 Read Model 비활성화 완료. sellerId={}", payload.sellerId());
        }

        if (payload.action().isCreated() || payload.action().isUpdated()) {
            saveShippingPolicyReadModelPort.upsert(
                    payload.sellerId(), payload.shippingFee(), payload.freeShippingThreshold()
            );
            log.info("배송비 정책 Read Model 저장 완료. sellerId={}, shippingFee={}, freeShippingThreshold={}",
                    payload.sellerId(), payload.shippingFee(), payload.freeShippingThreshold());
        }

        acknowledgment.acknowledge();
    }
}
