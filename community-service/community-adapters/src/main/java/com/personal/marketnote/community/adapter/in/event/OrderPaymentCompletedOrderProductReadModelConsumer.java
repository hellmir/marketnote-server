package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.out.order.SaveOrderProductReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentCompletedOrderProductReadModelConsumer {
    private final ObjectMapper objectMapper;
    private final SaveOrderProductReadModelPort saveOrderProductReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "community-order-product-read-model"
    )
    public void handleOrderPaymentCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_PAYMENT_COMPLETED)) {
            acknowledgment.acknowledge();
            return;
        }

        OrderPaymentCompletedEvent payload = envelope.getPayloadAs(OrderPaymentCompletedEvent.class, objectMapper);

        if (FormatValidator.hasNoValue(payload)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()),
                EventPayloadValidator.id("buyerId", payload.buyerId())
        )) {
            acknowledgment.acknowledge();
            return;
        }

        List<OrderProductItem> orderProducts = payload.orderProducts();
        if (FormatValidator.hasNoValue(orderProducts)) {
            acknowledgment.acknowledge();
            return;
        }

        for (OrderProductItem item : orderProducts) {
            upsertItem(envelope.eventId(), payload.orderId(), payload.buyerId(), item);
        }

        log.info("주문 상품 Read Model 저장 완료. eventId={}, orderId={}, itemCount={}",
                envelope.eventId(), payload.orderId(), orderProducts.size()
        );

        acknowledgment.acknowledge();
    }

    private void upsertItem(String eventId, Long orderId, Long buyerId, OrderProductItem item) {
        if (FormatValidator.hasNoValue(item)) {
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(eventId,
                EventPayloadValidator.id("pricePolicyId", item.pricePolicyId())
        )) {
            return;
        }

        if (FormatValidator.hasNoValue(item.unitAmount()) || item.unitAmount() <= 0) {
            log.warn("주문 상품 단가가 유효하지 않아 스킵. eventId={}, orderId={}, pricePolicyId={}, unitAmount={}",
                    eventId, orderId, item.pricePolicyId(), item.unitAmount()
            );
            return;
        }

        saveOrderProductReadModelPort.upsert(orderId, item.pricePolicyId(), buyerId, item.unitAmount());
    }
}
