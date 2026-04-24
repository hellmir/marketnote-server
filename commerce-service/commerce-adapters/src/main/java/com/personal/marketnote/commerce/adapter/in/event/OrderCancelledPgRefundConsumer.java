package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.RefundPaymentUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledPgRefundConsumer {
    private final ObjectMapper objectMapper;
    private final RefundPaymentUseCase refundPaymentUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_CANCELLED,
            groupId = "commerce-pg-refund"
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

        log.info("주문 취소 이벤트 수신 (PG 환불). eventId={}, orderId={}, isFullCancel={}, cancelAmount={}",
                envelope.eventId(), payload.orderId(), payload.isFullCancel(), payload.cancelAmount());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            RefundPaymentCommand command = RefundPaymentCommand.builder()
                    .orderKey(payload.orderKey())
                    .orderId(payload.orderId())
                    .cancelAmount(payload.cancelAmount())
                    .paymentAmount(payload.paymentAmount())
                    .isFullCancel(payload.isFullCancel())
                    .alreadyRefunded(payload.alreadyRefunded())
                    .build();

            refundPaymentUseCase.refund(command);

            log.info("PG 환불 완료. orderId={}, isFullCancel={}, cancelAmount={}",
                    payload.orderId(), payload.isFullCancel(), payload.cancelAmount());
        } catch (PaymentAlreadyRefundedException e) {
            log.info("이미 환불 처리된 결제 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        }

        acknowledgment.acknowledge();
    }
}
