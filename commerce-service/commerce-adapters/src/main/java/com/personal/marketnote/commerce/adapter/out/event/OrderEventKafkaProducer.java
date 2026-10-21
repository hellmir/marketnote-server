package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.event.exception.OrderEventSerializationException;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.*;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.common.domain.deliveryrequestmessage.DeliveryRequestMessage;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.common.utility.FormatValidator;
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
    public void publishOrderPaymentCompletedEvent(
            Long orderId, Long buyerId, Long totalAmount,
            Long pointAmount, ShippingAddress shippingAddress,
            List<OrderProduct> orderProducts, Long totalAccumulatedPoint
    ) {
        List<OrderProductItem> items = orderProducts.stream()
                .map(op -> new OrderProductItem(
                        op.getPricePolicyId(),
                        op.getProductId(),
                        op.getProductKey(),
                        op.getSharerKey(),
                        op.getQuantity().getValue(),
                        op.getUnitAmount().getValue()
                ))
                .toList();

        OrderPaymentCompletedEvent payload = new OrderPaymentCompletedEvent(
                orderId, buyerId, totalAmount, pointAmount,
                resolveRecipientName(shippingAddress),
                resolveRecipientPhoneNumber(shippingAddress),
                FormatValidator.hasValue(shippingAddress) ? shippingAddress.getZipCode() : null,
                FormatValidator.hasValue(shippingAddress) ? shippingAddress.getAddress() : null,
                FormatValidator.hasValue(shippingAddress) ? shippingAddress.getAddressDetail() : null,
                resolveDeliveryRequestMessage(shippingAddress),
                items, totalAccumulatedPoint
        );
        String topic = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED;
        EventEnvelope<OrderPaymentCompletedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishOrderPurchaseConfirmedEvent(
            Long orderId, Long buyerId, List<UUID> sharerKeys, boolean isAutoConfirmed,
            List<OrderProduct> orderProducts
    ) {
        List<OrderPurchaseConfirmedEvent.OrderProductItem> items = FormatValidator.hasValue(orderProducts)
                ? orderProducts.stream()
                        .map(op -> new OrderPurchaseConfirmedEvent.OrderProductItem(
                                op.getPricePolicyId(),
                                op.getProductId(),
                                op.getQuantity().getValue()
                        ))
                        .toList()
                : List.of();
        OrderPurchaseConfirmedEvent payload = new OrderPurchaseConfirmedEvent(
                orderId, buyerId, sharerKeys, isAutoConfirmed, items
        );
        String topic = KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED;
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishOrderCancelledEvent(
            Long orderId, String orderKey, Long buyerId,
            Long cancelAmount, Long paymentAmount, Long pointAmount,
            Long shippingFee, boolean isFullCancel, Long alreadyRefunded,
            List<OrderProduct> orderProducts, List<OrderProduct> cancelProducts,
            String cancelId
    ) {
        List<OrderCancelledEvent.OrderProductItem> items = orderProducts.stream()
                .map(op -> new OrderCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity().getValue(),
                        op.getUnitAmount().getValue()
                ))
                .toList();

        List<OrderCancelledEvent.OrderProductItem> cancelItems = cancelProducts.stream()
                .map(op -> new OrderCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity().getValue(),
                        op.getUnitAmount().getValue()
                ))
                .toList();

        OrderCancelledEvent payload = new OrderCancelledEvent(
                orderId, orderKey, buyerId, cancelAmount, paymentAmount,
                pointAmount, shippingFee, isFullCancel, alreadyRefunded,
                items, cancelItems, cancelId
        );
        String topic = KafkaTopicConstants.ORDER_CANCELLED;
        EventEnvelope<OrderCancelledEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishOrderReturnedEvent(
            Long orderId, String orderKey, Long buyerId,
            Long returnAmount, Long paymentAmount, Long pointAmount,
            Long shippingFee, boolean isFullReturn,
            Long returnShippingFee, List<OrderProduct> returnProducts,
            String returnId
    ) {
        List<OrderReturnedEvent.OrderProductItem> items = returnProducts.stream()
                .map(op -> new OrderReturnedEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity().getValue(),
                        op.getUnitAmount().getValue()
                ))
                .toList();

        OrderReturnedEvent payload = new OrderReturnedEvent(
                orderId, orderKey, buyerId, returnAmount, paymentAmount,
                pointAmount, shippingFee, isFullReturn, returnShippingFee, items, returnId
        );
        String topic = KafkaTopicConstants.ORDER_RETURNED;
        EventEnvelope<OrderReturnedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishReturnRequestedEvent(Long orderId, Long buyerId) {
        ReturnRequestedEvent payload = new ReturnRequestedEvent(orderId, buyerId);
        String topic = KafkaTopicConstants.RETURN_REQUESTED;
        EventEnvelope<ReturnRequestedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishReturnRejectedEvent(Long orderId, Long buyerId) {
        ReturnRejectedEvent payload = new ReturnRejectedEvent(orderId, buyerId);
        String topic = KafkaTopicConstants.RETURN_REJECTED;
        EventEnvelope<ReturnRejectedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishOrderCancelFailedEvent(Long orderId, Long buyerId) {
        OrderCancelFailedEvent payload = new OrderCancelFailedEvent(orderId, buyerId);
        String topic = KafkaTopicConstants.ORDER_CANCEL_FAILED;
        EventEnvelope<OrderCancelFailedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    private String resolveRecipientName(ShippingAddress shippingAddress) {
        if (FormatValidator.hasNoValue(shippingAddress) || FormatValidator.hasNoValue(shippingAddress.getRecipientName())) {
            return null;
        }
        return shippingAddress.getRecipientName().getValue();
    }

    private String resolveRecipientPhoneNumber(ShippingAddress shippingAddress) {
        if (FormatValidator.hasNoValue(shippingAddress) || FormatValidator.hasNoValue(shippingAddress.getRecipientPhoneNumber())) {
            return null;
        }
        return shippingAddress.getRecipientPhoneNumber().getValue();
    }

    private String resolveDeliveryRequestMessage(ShippingAddress shippingAddress) {
        if (FormatValidator.hasNoValue(shippingAddress)) {
            return null;
        }
        DeliveryRequestMessage message = shippingAddress.getDeliveryRequestMessage();
        if (FormatValidator.hasNoValue(message)) {
            return null;
        }
        return message.getValue();
    }

    private <T> void saveToOutbox(EventEnvelope<T> envelope, String topic, String partitionKey) {
        String payloadJson = serializePayload(envelope, topic);
        OutboxEvent outboxEvent = OutboxEvent.of(
                envelope.eventId(), topic, partitionKey,
                envelope.eventType(), SOURCE, payloadJson, clock
        );
        saveOutboxEventPort.save(outboxEvent);
        log.info("Outbox 이벤트 저장. topic={}, partitionKey={}, eventId={}", topic, partitionKey, envelope.eventId());
    }

    private <T> String serializePayload(EventEnvelope<T> envelope, String topic) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new OrderEventSerializationException(topic, e);
        }
    }
}
