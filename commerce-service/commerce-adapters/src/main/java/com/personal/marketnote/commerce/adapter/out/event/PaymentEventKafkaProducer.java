package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.out.event.PublishPaymentEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentFailedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.List;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class PaymentEventKafkaProducer implements PublishPaymentEventPort {
    private static final String SOURCE = "commerce-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishPaymentApprovedEvent(Long orderId, String orderKey, Long paymentAmount) {
        PaymentApprovedEvent payload = new PaymentApprovedEvent(orderId, orderKey, paymentAmount);
        String topic = KafkaTopicConstants.PAYMENT_APPROVED;
        EventEnvelope<PaymentApprovedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishPaymentFailedEvent(Long orderId, String orderKey, String resultCode, String resultMessage) {
        PaymentFailedEvent payload = new PaymentFailedEvent(orderId, orderKey, resultCode, resultMessage);
        String topic = KafkaTopicConstants.PAYMENT_FAILED;
        EventEnvelope<PaymentFailedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    @Override
    public void publishPaymentCancelledEvent(Long orderId, String orderKey, Long buyerId,
                                             Long cancelAmount, Long paymentAmount, Long pointAmount,
                                             boolean isFullCancel, Long alreadyRefunded,
                                             String cancelId,
                                             List<OrderProduct> orderProducts,
                                             List<OrderProduct> cancelProducts,
                                             Long partialProductPendingDeduction) {
        List<PaymentCancelledEvent.OrderProductItem> items = orderProducts.stream()
                .map(op -> new PaymentCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList();

        List<PaymentCancelledEvent.OrderProductItem> cancelItems = FormatValidator.hasValue(cancelProducts)
                ? cancelProducts.stream()
                .map(op -> new PaymentCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerKey(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList()
                : null;

        PaymentCancelledEvent payload = new PaymentCancelledEvent(
                orderId, orderKey, buyerId, cancelAmount, paymentAmount,
                pointAmount, isFullCancel, alreadyRefunded, cancelId, items, cancelItems,
                partialProductPendingDeduction
        );
        String topic = KafkaTopicConstants.PAYMENT_CANCELLED;
        EventEnvelope<PaymentCancelledEvent> envelope = EventEnvelope.of(
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
