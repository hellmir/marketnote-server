package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentCompletedProductPointConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "reward-product-point"
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

            log.info("주문 결제 완료 이벤트 수신 (상품 구매 포인트 적립). eventId={}, orderId={}, buyerId={}, totalAmount={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.totalAmount());

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.buyerId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId={}, buyerId={}",
                        envelope.eventId(), payload.orderId(), payload.buyerId());
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.totalAmount()) || payload.totalAmount() <= 0) {
                log.info("결제 금액이 없는 주문 (적립 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1131] HTTP 제거 후 ModifyPendingPointUseCase 활성화
            //  현재 듀얼 라이트 기간: ChangeOrderStatusService.addPendingProductAccumulationPoints()가 HTTP로 처리 중
            //  멱등성 보강 (#1213) 완료 후 아래 주석 해제:
            //  ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
            //          .userId(payload.buyerId())
            //          .changeType(UserPointChangeType.ACCRUAL)
            //          .amount(accumulatedPointAmount)  // 상품별 적립 포인트 합산 필요
            //          .sourceType(UserPointSourceType.ORDER)
            //          .sourceId(payload.orderId())
            //          .reason("상품 구매 적립 예정 포인트")
            //          .build();
            //  modifyPendingPointUseCase.modifyPending(command);

            log.info("상품 구매 포인트 적립 이벤트 검증 완료 (듀얼 라이트). orderId={}, buyerId={}, totalAmount={}",
                    payload.orderId(), payload.buyerId(), payload.totalAmount());
        } catch (Exception e) {
            log.error("상품 구매 포인트 적립 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
