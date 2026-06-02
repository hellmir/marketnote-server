package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class OrderEventKafkaProducer implements PublishOrderEventPort {
    private static final String SOURCE = "commerce-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishOrderPaymentCompletedEvent(Long orderId, Long buyerId, Long totalAmount,
                                                  Long pointAmount, List<OrderProduct> orderProducts,
                                                  Long totalAccumulatedPoint) {
        List<OrderProductItem> items = orderProducts.stream()
                .map(op -> new OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList();

        OrderPaymentCompletedEvent payload = new OrderPaymentCompletedEvent(
                orderId, buyerId, totalAmount, pointAmount, items, totalAccumulatedPoint
        );
        String topic = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED;
        EventEnvelope<OrderPaymentCompletedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishOrderPurchaseConfirmedEvent(Long orderId, Long buyerId, List<UUID> sharerKeys) {
        OrderPurchaseConfirmedEvent payload = new OrderPurchaseConfirmedEvent(orderId, buyerId, sharerKeys);
        String topic = KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED;
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishOrderCancelledEvent(Long orderId, String orderKey, Long buyerId,
                                           Long cancelAmount, Long paymentAmount, Long pointAmount,
                                           Long shippingFee, boolean isFullCancel, Long alreadyRefunded,
                                           List<OrderProduct> orderProducts, List<OrderProduct> cancelProducts) {
        List<OrderCancelledEvent.OrderProductItem> items = orderProducts.stream()
                .map(op -> new OrderCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList();

        List<OrderCancelledEvent.OrderProductItem> cancelItems = cancelProducts.stream()
                .map(op -> new OrderCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList();

        OrderCancelledEvent payload = new OrderCancelledEvent(
                orderId, orderKey, buyerId, cancelAmount, paymentAmount,
                pointAmount, shippingFee, isFullCancel, alreadyRefunded,
                items, cancelItems
        );
        String topic = KafkaTopicConstants.ORDER_CANCELLED;
        EventEnvelope<OrderCancelledEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishOrderReturnedEvent(Long orderId, String orderKey, Long buyerId,
                                          Long returnAmount, Long paymentAmount, Long pointAmount,
                                          Long shippingFee, boolean isFullReturn,
                                          Long returnShippingFee, List<OrderProduct> returnProducts) {
        List<OrderReturnedEvent.OrderProductItem> items = returnProducts.stream()
                .map(op -> new OrderReturnedEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList();

        OrderReturnedEvent payload = new OrderReturnedEvent(
                orderId, orderKey, buyerId, returnAmount, paymentAmount,
                pointAmount, shippingFee, isFullReturn, returnShippingFee, items
        );
        String topic = KafkaTopicConstants.ORDER_RETURNED;
        EventEnvelope<OrderReturnedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    private <T> void saveToOutbox(EventEnvelope<T> envelope, String topic, String partitionKey) {
        try {
            String payloadJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    envelope.eventId(), topic, partitionKey,
                    envelope.eventType(), SOURCE, payloadJson, clock
            );
            saveOutboxEventPort.save(outboxEvent);
            log.info("Outbox 이벤트 저장. topic={}, partitionKey={}, eventId={}",
                    topic, partitionKey, envelope.eventId());
        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패. topic={}, partitionKey={}, error={}",
                    topic, partitionKey, e.getMessage(), e);
        }
    }
}
