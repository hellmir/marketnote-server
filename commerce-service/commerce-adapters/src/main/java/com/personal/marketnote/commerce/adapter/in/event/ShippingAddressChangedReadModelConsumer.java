package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.user.SaveShippingAddressReadModelPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingAddressChangedReadModelConsumer {

    private final ObjectMapper objectMapper;
    private final SaveShippingAddressReadModelPort saveShippingAddressReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED,
            groupId = "commerce-shipping-address-read-model"
    )
    public void handleShippingAddressChangedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED)) {
            acknowledgment.acknowledge();
            return;
        }

        ShippingAddressChangedEvent payload = envelope.getPayloadAs(ShippingAddressChangedEvent.class, objectMapper);

        log.info("배송지 변경 이벤트 수신. eventId={}, shippingAddressId={}, userId={}, action={}",
                envelope.eventId(), payload.shippingAddressId(), payload.userId(), payload.action());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("shippingAddressId", payload.shippingAddressId()),
                EventPayloadValidator.id("userId", payload.userId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.action())) {
            log.warn("배송지 변경 이벤트 action이 null. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        if (payload.action().isDeleted()) {
            saveShippingAddressReadModelPort.deactivateByShippingAddressId(payload.shippingAddressId());
            log.info("배송지 Read Model 비활성화 완료. shippingAddressId={}", payload.shippingAddressId());
            acknowledgment.acknowledge();
            return;
        }

        if (payload.action().isCreated() || payload.action().isUpdated()) {
            if (FormatValidator.hasNoValue(payload.recipientName())
                    || FormatValidator.hasNoValue(payload.recipientPhoneNumber())
                    || FormatValidator.hasNoValue(payload.address())
                    || FormatValidator.hasNoValue(payload.addressDetail())) {
                log.warn("배송지 변경 이벤트 필수 필드 누락. eventId={}, shippingAddressId={}",
                        envelope.eventId(), payload.shippingAddressId());
                acknowledgment.acknowledge();
                return;
            }

            saveShippingAddressReadModelPort.upsert(
                    payload.shippingAddressId(), payload.userId(),
                    payload.recipientName(), payload.recipientPhoneNumber(),
                    payload.address(), payload.addressDetail()
            );
            log.info("배송지 Read Model 저장 완료. shippingAddressId={}, userId={}",
                    payload.shippingAddressId(), payload.userId());
        }

        acknowledgment.acknowledge();
    }
}
