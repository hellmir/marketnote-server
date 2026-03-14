package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
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
public class PaymentCancelledPartialProductPointConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "reward-partial-pending-product-point"
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

            log.info("결제 취소 이벤트 수신 (부분 상품 적립 예정 포인트 차감). eventId={}, orderId={}, buyerId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.isFullCancel());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            if (payload.isFullCancel()) {
                log.info("전체 취소 이벤트 -- 부분 상품 적립 예정 포인트 차감 불필요. orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.buyerId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId={}, buyerId=null",
                        envelope.eventId(), payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.partialProductPendingDeduction())
                    || payload.partialProductPendingDeduction() <= 0) {
                log.info("부분 상품 적립 예정 포인트 차감 금액 없음. orderId={}, deductionAmount={}",
                        payload.orderId(), payload.partialProductPendingDeduction());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1178] HTTP 제거 후 ModifyPendingPointUseCase 활성화
            //  현재 듀얼 라이트 기간: CancelPaymentService.reducePartialPendingProductAccumulationPoints()에서 HTTP로 처리 중
            //  아래 주석 해제 시 활성화:
            //  ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
            //          .userId(payload.buyerId())
            //          .changeType(UserPointChangeType.DEDUCTION)
            //          .amount(payload.partialProductPendingDeduction())
            //          .sourceType(UserPointSourceType.ORDER)
            //          .sourceId(payload.orderId())
            //          .reason("부분 결제 취소 상품 적립 예정 포인트 차감")
            //          .build();
            //  modifyPendingPointUseCase.modifyPending(command);

            log.info("부분 상품 적립 예정 포인트 차감 이벤트 검증 완료 (듀얼 라이트). orderId={}, buyerId={}, deductionAmount={}",
                    payload.orderId(), payload.buyerId(), payload.partialProductPendingDeduction());
        } catch (Exception e) {
            log.error("부분 상품 적립 예정 포인트 차감 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
