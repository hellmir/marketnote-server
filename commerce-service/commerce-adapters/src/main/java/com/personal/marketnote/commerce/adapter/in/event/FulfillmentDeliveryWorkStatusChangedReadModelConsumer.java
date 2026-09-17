package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.fulfillment.SaveFulfillmentWorkStatusReadModelPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.FulfillmentDeliveryWorkStatusChangedEvent;
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
public class FulfillmentDeliveryWorkStatusChangedReadModelConsumer {

    private final ObjectMapper objectMapper;
    private final SaveFulfillmentWorkStatusReadModelPort saveFulfillmentWorkStatusReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.FULFILLMENT_DELIVERY_WORK_STATUS_CHANGED,
            groupId = "commerce-fulfillment-work-status-read-model"
    )
    public void handleFulfillmentDeliveryWorkStatusChangedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.FULFILLMENT_DELIVERY_WORK_STATUS_CHANGED)) {
            acknowledgment.acknowledge();
            return;
        }

        FulfillmentDeliveryWorkStatusChangedEvent payload =
                envelope.getPayloadAs(FulfillmentDeliveryWorkStatusChangedEvent.class, objectMapper);

        log.info("풀필먼트 배송 작업 상태 변경 이벤트 수신. eventId={}, orderId={}, workStatus={}",
                envelope.eventId(), payload.orderId(), payload.workStatus());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.workStatus())) {
            log.warn("풀필먼트 배송 작업 상태 변경 이벤트 workStatus가 null. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        saveFulfillmentWorkStatusReadModelPort.upsert(payload.orderId(), payload.workStatus());

        log.info("풀필먼트 작업 상태 Read Model 저장 완료. orderId={}, workStatus={}",
                payload.orderId(), payload.workStatus());

        acknowledgment.acknowledge();
    }
}
