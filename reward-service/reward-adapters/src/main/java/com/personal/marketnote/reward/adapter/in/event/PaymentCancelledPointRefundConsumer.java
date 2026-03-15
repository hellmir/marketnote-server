package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
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
public class PaymentCancelledPointRefundConsumer {
    private static final String POINT_REFUND_REASON = "주문 취소 포인트 환불";

    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "reward-point-refund"
    )
    public void handlePaymentCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (FormatValidator.hasNoValue(envelope)) {
            log.error("이벤트 envelope이 null. topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            acknowledgment.acknowledge();
            return;
        }

        try {
            PaymentCancelledEvent payload = envelope.getPayloadAs(
                    PaymentCancelledEvent.class, objectMapper
            );

            log.info("결제 취소 이벤트 수신 (포인트 환불). eventId={}, orderId={}, buyerId={}, pointAmount={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.pointAmount(), payload.isFullCancel());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            if (!payload.isFullCancel()) {
                log.info("부분 취소 이벤트 -- 포인트 환불 불필요. orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.buyerId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId={}, buyerId=null",
                        envelope.eventId(), payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.pointAmount()) || payload.pointAmount() <= 0) {
                log.info("포인트 미사용 주문 (환불 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            modifyUserPointIdempotent(envelope.eventId(), payload.buyerId(), payload.pointAmount(), payload.orderId());

            log.info("포인트 환불 완료. orderId={}, buyerId={}, pointAmount={}",
                    payload.orderId(), payload.buyerId(), payload.pointAmount());
        } catch (Exception e) {
            log.error("포인트 환불 이벤트 처리 실패. eventId={}, key={}, error={}",
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
