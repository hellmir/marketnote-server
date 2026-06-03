package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationCreateState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
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
public class OrderReturnedSettlementConsumer {
    private static final String IDEMPOTENCY_KEY_PREFIX = "ORDER_RETURN_ALLOCATION:";

    private final ObjectMapper objectMapper;
    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final SavePaymentAllocationPort savePaymentAllocationPort;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_RETURNED,
            groupId = "commerce-order-returned-settlement"
    )
    public void handleOrderReturnedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_RETURNED)) {
            acknowledgment.acknowledge();
            return;
        }

        OrderReturnedEvent payload = envelope.getPayloadAs(OrderReturnedEvent.class, objectMapper);

        log.info("반품 완료 이벤트 수신 (정산 역배분). eventId={}, orderId={}, isFullReturn={}",
                envelope.eventId(), payload.orderId(), payload.isFullReturn());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            createReturnAllocations(payload.orderId());
            log.info("반품 완료 역배분 생성 완료. orderId={}", payload.orderId());
        } catch (DataIntegrityViolationException e) {
            log.info("이미 처리된 반품 완료 역배분 이벤트 (멱등 처리). eventId={}, orderId={}",
                    envelope.eventId(), payload.orderId());
        }

        acknowledgment.acknowledge();
    }

    private void createReturnAllocations(Long orderId) {
        List<PaymentAllocation> originalAllocations = findPaymentAllocationPort.findByOrderId(orderId);

        List<PaymentAllocation> returnAllocations = originalAllocations.stream()
                .filter(allocation -> allocation.getTransactionType().isPositiveContribution())
                .map(allocation -> PaymentAllocation.from(
                        PaymentAllocationCreateState.builder()
                                .orderId(orderId)
                                .sellerId(allocation.getSellerId())
                                .allocatedAmount(allocation.getAllocatedAmount())
                                .shippingFee(allocation.getShippingFee())
                                .transactionType(PaymentAllocationTransactionType.RETURN_REFUND)
                                .targetType(PaymentAllocationTargetType.ORDER)
                                .idempotencyKey(IDEMPOTENCY_KEY_PREFIX + orderId + ":" + allocation.getSellerId())
                                .build()
                ))
                .toList();

        if (!returnAllocations.isEmpty()) {
            savePaymentAllocationPort.saveAll(returnAllocations);
        }
    }
}
