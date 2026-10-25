package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderAutoConfirmFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoConfirmRetryConsumer {

    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED,
            groupId = "commerce-auto-confirm-retry"
    )
    public void handleOrderAutoConfirmFailedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            OrderAutoConfirmFailedEvent payload = envelope.getPayloadAs(
                    OrderAutoConfirmFailedEvent.class, objectMapper
            );

            log.info(
                    "자동 확정 실패 이벤트 수신 (재시도). eventId={}, orderId={}, buyerId={}, reason={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.reason()
            );

            if (EventPayloadValidator.hasInvalidIds(
                    envelope.eventId(), EventPayloadValidator.id("orderId", payload.orderId())
            )) {
                acknowledgment.acknowledge();
                return;
            }

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(payload.orderId())
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();
            changeOrderStatusUseCase.changeOrderStatus(command);

            log.info("자동 확정 재시도 성공. orderId={}", payload.orderId());
        } catch (OrderStatusAlreadyChangedException e) {
            log.warn(
                    "자동 확정 재시도 skip: 이미 주문 상태가 변경됨. eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage()
            );
        } catch (Exception e) {
            log.error(
                    "자동 확정 재시도 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e
            );
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
