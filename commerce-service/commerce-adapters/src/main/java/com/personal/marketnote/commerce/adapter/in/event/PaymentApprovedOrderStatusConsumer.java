package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApprovedOrderStatusConsumer {
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_APPROVED,
            groupId = "commerce-order-status"
    )
    public void handlePaymentApprovedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PAYMENT_APPROVED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            PaymentApprovedEvent payload = envelope.getPayloadAs(PaymentApprovedEvent.class, objectMapper);

            log.info("결제 승인 이벤트 수신. eventId={}, orderId={}, orderKey={}",
                    envelope.eventId(), payload.orderId(), payload.orderKey());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(payload.orderId())
                    .orderStatus(OrderStatus.PAID)
                    .build();
            changeOrderStatusUseCase.changeOrderStatus(command);

            log.info("Kafka 이벤트로 주문 상태 PAID 변경 완료. orderId={}, orderKey={}",
                    payload.orderId(), payload.orderKey());
        } catch (OrderStatusAlreadyChangedException e) {
            log.warn("듀얼 라이트: 이미 주문 상태가 변경됨. eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage());
        } catch (Exception e) {
            log.error("주문 상태 PAID 변경 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
