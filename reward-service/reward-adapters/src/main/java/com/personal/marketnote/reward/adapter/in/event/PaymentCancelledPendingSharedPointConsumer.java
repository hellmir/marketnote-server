package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent.OrderProductItem;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelledPendingSharedPointConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "reward-pending-shared-point"
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

            log.info("결제 취소 이벤트 수신 (공유 적립 예정 포인트 회수). eventId={}, orderId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.isFullCancel());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            if (!payload.isFullCancel()) {
                log.info("부분 취소 이벤트 -- 공유 적립 예정 포인트 회수 불필요. orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            List<Long> sharerIds = extractSharerIds(payload.orderProducts());
            if (sharerIds.isEmpty()) {
                log.info("공유자가 없는 주문 (공유 적립 예정 포인트 회수 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1177] HTTP 제거 후 CancelPendingPointUseCase 활성화
            //  현재 듀얼 라이트 기간: CancelPaymentService.revokePendingSharedPurchasePoints()에서 HTTP로 처리 중
            //  아래 주석 해제 시 활성화:
            //  for (Long sharerId : sharerIds) {
            //      CancelPendingPointCommand command = CancelPendingPointCommand.builder()
            //              .userId(sharerId)
            //              .sourceType(UserPointSourceType.ORDER)
            //              .sourceId(payload.orderId())
            //              .reason("결제 취소 적립 예정 포인트 회수")
            //              .build();
            //      cancelPendingPointUseCase.cancelPending(command);
            //  }

            log.info("공유 적립 예정 포인트 회수 이벤트 검증 완료 (듀얼 라이트). orderId={}, sharerIds={}",
                    payload.orderId(), sharerIds);
        } catch (Exception e) {
            log.error("공유 적립 예정 포인트 회수 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private List<Long> extractSharerIds(List<OrderProductItem> orderProducts) {
        if (FormatValidator.hasNoValue(orderProducts)) {
            return List.of();
        }

        return orderProducts.stream()
                .map(OrderProductItem::sharerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
