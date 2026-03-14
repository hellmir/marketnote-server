package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
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

        if (FormatValidator.hasNoValue(envelope)) {
            log.warn("envelope이 null입니다. key={}", record.key());
            acknowledgment.acknowledge();
            return;
        }

        try {
            OrderPurchaseConfirmedEvent payload = envelope.getPayloadAs(
                    OrderPurchaseConfirmedEvent.class, objectMapper
            );

            log.info("구매 확정 이벤트 수신 (공유 적립 예정 포인트 확정). eventId={}, orderId={}, sharerIds={}",
                    envelope.eventId(), payload.orderId(), payload.sharerIds());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            List<Long> sharerIds = payload.sharerIds();
            if (FormatValidator.hasNoValue(sharerIds) || sharerIds.isEmpty()) {
                log.info("공유자가 없는 주문 (공유 적립 예정 포인트 확정 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1132] HTTP 제거 후 ConfirmPendingPointUseCase 활성화
            //  현재 듀얼 라이트 기간: ChangeOrderStatusService.confirmPendingPoints()가 HTTP로 처리 중
            //  멱등성 보강 (#1217) 완료 후 아래 주석 해제:
            //  for (Long sharerId : sharerIds) {
            //      ConfirmPendingPointCommand command = ConfirmPendingPointCommand.builder()
            //              .userId(sharerId)
            //              .sourceType(UserPointSourceType.ORDER)
            //              .sourceId(payload.orderId())
            //              .reason("구매 확정 공유 포인트 적립")
            //              .build();
            //      confirmPendingPointUseCase.confirmPending(command);
            //  }

            log.info("공유 적립 예정 포인트 확정 이벤트 검증 완료 (듀얼 라이트). orderId={}, sharerIds={}",
                    payload.orderId(), sharerIds);
        } catch (Exception e) {
            log.error("공유 적립 예정 포인트 확정 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
