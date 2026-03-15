package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelledOrderStatusConsumer {
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "commerce-order-status"
    )
    public void handlePaymentCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PAYMENT_CANCELLED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            PaymentCancelledEvent payload = envelope.getPayloadAs(PaymentCancelledEvent.class, objectMapper);

            log.info("결제 취소 이벤트 수신 (주문 상태 변경). eventId={}, orderId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.isFullCancel());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (!payload.isFullCancel()) {
                log.info("부분 취소 이벤트 — 주문 상태 변경 불필요. eventId={}, orderId={}",
                        envelope.eventId(), payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1100] HTTP 제거 후 ChangeOrderStatusUseCase.changeOrderStatus(CANCEL_REQUESTED) 활성화
            //  현재 듀얼 라이트 기간: CancelPaymentService.cancel()에서 동기로 주문 상태 변경 처리 중
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(payload.orderId())
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .build();
            changeOrderStatusUseCase.changeOrderStatus(command);

            log.info("Kafka 이벤트로 주문 상태 CANCEL_REQUESTED 변경 완료. orderId={}",
                    payload.orderId());
        } catch (OrderStatusAlreadyChangedException e) {
            log.warn("듀얼 라이트: 이미 주문 상태가 변경됨. eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage());
        } catch (Exception e) {
            log.error("주문 상태 CANCEL_REQUESTED 변경 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
