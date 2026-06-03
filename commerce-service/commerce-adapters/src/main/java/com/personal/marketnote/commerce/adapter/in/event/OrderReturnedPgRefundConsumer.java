package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnRefundUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReturnedPgRefundConsumer {
    private final ObjectMapper objectMapper;
    private final CompleteReturnRefundUseCase completeReturnRefundUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_RETURNED,
            groupId = "commerce-order-returned-pg-refund"
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

        log.info("반품 완료 이벤트 수신 (PG 환불). eventId={}, orderId={}, isFullReturn={}, returnAmount={}",
                envelope.eventId(), payload.orderId(), payload.isFullReturn(), payload.returnAmount());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            CompleteReturnRefundCommand command = CompleteReturnRefundCommand.builder()
                    .orderId(payload.orderId())
                    .orderKey(payload.orderKey())
                    .buyerId(payload.buyerId())
                    .returnAmount(payload.returnAmount())
                    .paymentAmount(payload.paymentAmount())
                    .pointAmount(payload.pointAmount())
                    .shippingFee(payload.shippingFee())
                    .isFullReturn(payload.isFullReturn())
                    .returnShippingFee(payload.returnShippingFee() != null ? payload.returnShippingFee() : 0L)
                    .build();

            completeReturnRefundUseCase.completeReturnRefund(command);

            log.info("반품 PG 환불 처리 완료. orderId={}", payload.orderId());
        } catch (Exception e) {
            log.error("반품 PG 환불 처리 실패. orderId={}, error={}",
                    payload.orderId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
