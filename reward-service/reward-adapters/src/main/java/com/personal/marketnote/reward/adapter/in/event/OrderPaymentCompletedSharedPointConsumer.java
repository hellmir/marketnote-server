package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
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
public class OrderPaymentCompletedSharedPointConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "reward-shared-point"
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

            log.info("주문 결제 완료 이벤트 수신 (공유 포인트 적립). eventId={}, orderId={}, buyerId={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId());

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.buyerId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId={}, buyerId={}",
                        envelope.eventId(), payload.orderId(), payload.buyerId());
                acknowledgment.acknowledge();
                return;
            }

            List<Long> sharerIds = extractSharerIds(payload.orderProducts());
            if (sharerIds.isEmpty()) {
                log.info("공유자가 없는 주문 (공유 포인트 적립 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1019] HTTP 제거 후 ModifyPendingPointUseCase 활성화
            //  현재 듀얼 라이트 기간: ChangeOrderStatusService.addPendingSharedPurchasePoints()가 HTTP로 처리 중
            //  멱등성 보강 (#1211) 완료 후 아래 주석 해제:
            //  for (Long sharerId : sharerIds) {
            //      ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
            //              .userId(sharerId)
            //              .changeType(UserPointChangeType.ACCRUAL)
            //              .amount(payload.totalAmount())
            //              .sourceType(UserPointSourceType.ORDER)
            //              .sourceId(payload.orderId())
            //              .reason("공유 구매 적립 예정 포인트")
            //              .build();
            //      modifyPendingPointUseCase.modifyPending(command);
            //  }

            log.info("공유 포인트 적립 이벤트 검증 완료 (듀얼 라이트). orderId={}, sharerIds={}, totalAmount={}",
                    payload.orderId(), sharerIds, payload.totalAmount());
        } catch (Exception e) {
            log.error("공유 포인트 적립 이벤트 처리 실패. eventId={}, key={}, error={}",
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
