package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReturnedPointRefundConsumer {
    private static final String POINT_REFUND_REASON = "반품 완료 포인트 환불";

    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_RETURNED,
            groupId = "reward-order-returned-point-refund"
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

        try {
            OrderReturnedEvent payload = envelope.getPayloadAs(
                    OrderReturnedEvent.class, objectMapper
            );

            log.info("반품 완료 이벤트 수신 (포인트 환불). eventId={}, orderId={}, buyerId={}, pointAmount={}, isFullReturn={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.pointAmount(), payload.isFullReturn());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (!payload.isFullReturn()) {
                log.info("부분 반품 이벤트 -- 포인트 환불 불필요. orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("buyerId", payload.buyerId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.pointAmount()) || payload.pointAmount() <= 0) {
                log.info("포인트 미사용 주문 (환불 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            modifyUserPointIdempotent(envelope.eventId(), payload.buyerId(), payload.pointAmount(), payload.orderId());

            log.info("반품 포인트 환불 완료. orderId={}, buyerId={}, pointAmount={}",
                    payload.orderId(), payload.buyerId(), payload.pointAmount());
        } catch (Exception e) {
            log.error("반품 포인트 환불 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void modifyUserPointIdempotent(String eventId, Long buyerId, Long pointAmount, Long orderId) {
        try {
            ModifyUserPointCommand command = ModifyUserPointCommand.builder()
                    .userId(buyerId)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(pointAmount)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(orderId)
                    .reason(POINT_REFUND_REASON)
                    .build();
            modifyUserPointUseCase.modify(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 포인트 환불 이벤트 (멱등 처리). eventId={}, buyerId={}, message={}",
                    eventId, buyerId, e.getMessage());
        }
    }
}
