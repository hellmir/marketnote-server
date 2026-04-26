package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledSettlementConsumer {
    private static final String IDEMPOTENCY_KEY_PREFIX = "ORDER_CANCELLATION_ALLOCATION:";

    private final ObjectMapper objectMapper;
    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final SavePaymentAllocationPort savePaymentAllocationPort;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_CANCELLED,
            groupId = "commerce-order-cancelled-settlement"
    )
    public void handleOrderCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_CANCELLED)) {
            acknowledgment.acknowledge();
            return;
        }

        OrderCancelledEvent payload = envelope.getPayloadAs(OrderCancelledEvent.class, objectMapper);

        log.info("주문 취소 이벤트 수신 (정산 역배분). eventId={}, orderId={}, isFullCancel={}",
                envelope.eventId(), payload.orderId(), payload.isFullCancel());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            createCancellationAllocations(payload.orderId());
            log.info("주문 취소 역배분 생성 완료. orderId={}", payload.orderId());
        } catch (DataIntegrityViolationException e) {
            log.info("이미 처리된 주문 취소 역배분 이벤트 (멱등 처리). eventId={}, orderId={}",
                    envelope.eventId(), payload.orderId());
        }

        acknowledgment.acknowledge();
    }

    private void createCancellationAllocations(Long orderId) {
        List<PaymentAllocation> originalAllocations = findPaymentAllocationPort.findByOrderId(orderId);

        List<PaymentAllocation> cancellationAllocations = originalAllocations.stream()
                .filter(allocation -> allocation.getTransactionType().isPositiveContribution())
                .map(allocation -> PaymentAllocation.from(
                        PaymentAllocationCreateState.builder()
                                .orderId(orderId)
                                .sellerId(allocation.getSellerId())
                                .allocatedAmount(allocation.getAllocatedAmount())
                                .shippingFee(allocation.getShippingFee())
                                .transactionType(PaymentAllocationTransactionType.CANCELLATION)
                                .targetType(PaymentAllocationTargetType.ORDER)
                                .idempotencyKey(IDEMPOTENCY_KEY_PREFIX + orderId + ":" + allocation.getSellerId())
                                .build()
                ))
                .toList();

        if (!cancellationAllocations.isEmpty()) {
            savePaymentAllocationPort.saveAll(cancellationAllocations);
        }
    }
}
