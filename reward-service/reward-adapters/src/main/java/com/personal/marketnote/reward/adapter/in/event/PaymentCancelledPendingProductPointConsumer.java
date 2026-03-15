package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PaymentCancelledPendingProductPointConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "reward-pending-product-point"
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
            PaymentCancelledEvent payload = envelope.getPayloadAs(
                    PaymentCancelledEvent.class, objectMapper
            );

            log.info("결제 취소 이벤트 수신 (상품 적립 예정 포인트 회수). eventId={}, orderId={}, buyerId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.isFullCancel());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (!payload.isFullCancel()) {
                log.info("부분 취소 이벤트 -- 상품 적립 예정 포인트 회수 불필요. orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("buyerId", payload.buyerId()))) {
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1176] HTTP 제거 후 CancelPendingPointUseCase 활성화
            //  현재 듀얼 라이트 기간: CancelPaymentService.revokePendingProductAccumulationPoints()에서 HTTP로 처리 중
            //  아래 주석 해제 시 활성화:
            //  CancelPendingPointCommand command = CancelPendingPointCommand.builder()
            //          .userId(payload.buyerId())
            //          .sourceType(UserPointSourceType.ORDER)
            //          .sourceId(payload.orderId())
            //          .reason("결제 취소 적립 예정 포인트 회수")
            //          .build();
            //  cancelPendingPointUseCase.cancelPending(command);

            log.info("상품 적립 예정 포인트 회수 이벤트 검증 완료 (듀얼 라이트). orderId={}, buyerId={}",
                    payload.orderId(), payload.buyerId());
        } catch (Exception e) {
            log.error("상품 적립 예정 포인트 회수 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
