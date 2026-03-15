package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ConfirmPendingPointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPurchaseConfirmedSharedPointConsumer {
    private static final String CONFIRM_REASON = "구매 확정 공유 포인트 적립";

    private final ConfirmPendingPointUseCase confirmPendingPointUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED,
            groupId = "reward-pending-shared-point"
    )
    public void handleOrderPurchaseConfirmedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            OrderPurchaseConfirmedEvent payload = envelope.getPayloadAs(
                    OrderPurchaseConfirmedEvent.class, objectMapper
            );

            log.info("구매 확정 이벤트 수신 (공유 적립 예정 포인트 확정). eventId={}, orderId={}, sharerIds={}",
                    envelope.eventId(), payload.orderId(), payload.sharerIds());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            List<Long> sharerIds = payload.sharerIds();
            if (FormatValidator.hasNoValue(sharerIds) || sharerIds.isEmpty()) {
                log.info("공유자가 없는 주문 (공유 적립 예정 포인트 확정 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            for (Long sharerId : sharerIds) {
                confirmPending(sharerId, payload.orderId());
            }

            log.info("공유 적립 예정 포인트 확정 완료. orderId={}, sharerIds={}",
                    payload.orderId(), sharerIds);
        } catch (Exception e) {
            log.error("공유 적립 예정 포인트 확정 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void confirmPending(Long sharerId, Long orderId) {
        ConfirmPendingPointCommand command = ConfirmPendingPointCommand.builder()
                .userId(sharerId)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(orderId)
                .reason(CONFIRM_REASON)
                .build();
        confirmPendingPointUseCase.confirmPending(command);
    }
}
