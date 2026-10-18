package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.usecase.sales.SyncConfirmedOrderSalesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPurchaseConfirmedSalesConsumer {

    private static final String CONSUMER_GROUP_ID = "product-sales-sync";

    private final ObjectMapper objectMapper;
    private final SyncConfirmedOrderSalesUseCase syncConfirmedOrderSalesUseCase;

    @KafkaListener(topics = KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED, groupId = CONSUMER_GROUP_ID)
    public void handleOrderPurchaseConfirmed(
            ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack
    ) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED)) {
            ack.acknowledge();
            return;
        }

        OrderPurchaseConfirmedEvent payload = envelope.getPayloadAs(OrderPurchaseConfirmedEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)) {
            ack.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            ack.acknowledge();
            return;
        }

        syncConfirmedOrderSalesUseCase.syncSales(payload);
        ack.acknowledge();
    }
}
