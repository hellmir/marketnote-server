package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.out.fulfillment.SaveFulfillmentGoodsReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentGoodsSyncedReadModelConsumer {

    private final ObjectMapper objectMapper;
    private final SaveFulfillmentGoodsReadModelPort saveFulfillmentGoodsReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.FULFILLMENT_GOODS_SYNCED,
            groupId = "product-fulfillment-goods-read-model"
    )
    public void handleFulfillmentGoodsSyncedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.FULFILLMENT_GOODS_SYNCED)) {
            acknowledgment.acknowledge();
            return;
        }

        FulfillmentGoodsSyncedEvent payload = envelope.getPayloadAs(FulfillmentGoodsSyncedEvent.class, objectMapper);

        log.info("풀필먼트 상품 동기화 이벤트 수신. eventId={}, customerGoodsCode={}",
                envelope.eventId(), payload.customerGoodsCode());

        if (FormatValidator.hasNoValue(payload.customerGoodsCode())) {
            log.warn("풀필먼트 상품 동기화 이벤트 customerGoodsCode가 null. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        saveFulfillmentGoodsReadModelPort.upsert(payload);

        log.info("풀필먼트 상품 Read Model 저장 완료. customerGoodsCode={}", payload.customerGoodsCode());

        acknowledgment.acknowledge();
    }
}
