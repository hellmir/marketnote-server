package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
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
public class OrderPaymentCompletedOrderPointConsumer {
    private static final String ORDER_POINT_DEDUCTION_REASON = "주문 포인트 차감";

    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "reward-order-point"
    )
    public void handleOrderPaymentCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            OrderPaymentCompletedEvent payload = envelope.getPayloadAs(
                    OrderPaymentCompletedEvent.class, objectMapper
            );

            log.info("주문 결제 완료 이벤트 수신 (주문 포인트 차감). eventId={}, orderId={}, buyerId={}, pointAmount={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.pointAmount());

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.buyerId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId={}, buyerId={}",
                        envelope.eventId(), payload.orderId(), payload.buyerId());
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.pointAmount()) || payload.pointAmount() <= 0) {
                log.info("포인트 미사용 주문 (차감 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            modifyUserPointIdempotent(envelope.eventId(), payload.buyerId(), payload.pointAmount(), payload.orderId());

            log.info("주문 포인트 차감 완료. orderId={}, buyerId={}, pointAmount={}",
                    payload.orderId(), payload.buyerId(), payload.pointAmount());
        } catch (Exception e) {
            log.error("주문 포인트 차감 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void modifyUserPointIdempotent(String eventId, Long buyerId, Long pointAmount, Long orderId) {
        try {
            ModifyUserPointCommand command = ModifyUserPointCommand.builder()
                    .userId(buyerId)
                    .changeType(UserPointChangeType.DEDUCTION)
                    .amount(pointAmount)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(orderId)
                    .reason(ORDER_POINT_DEDUCTION_REASON)
                    .build();
            modifyUserPointUseCase.modify(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 주문 포인트 차감 이벤트 (멱등 처리). eventId={}, buyerId={}, message={}",
                    eventId, buyerId, e.getMessage());
        }
    }
}
