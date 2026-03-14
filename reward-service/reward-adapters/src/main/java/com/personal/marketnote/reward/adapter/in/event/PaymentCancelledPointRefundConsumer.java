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
public class PaymentCancelledPointRefundConsumer {
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

            // FIXME: [#929][#1102] HTTP 제거 후 ModifyUserPointUseCase 활성화
            //  현재 듀얼 라이트 기간: CancelPaymentService.refundPoints()에서 HTTP로 처리 중
            //  멱등성 보강 (#1214) 완료 후 아래 주석 해제:
            //  ModifyUserPointCommand command = ModifyUserPointCommand.builder()
            //          .userId(payload.buyerId())
            //          .changeType(UserPointChangeType.ACCRUAL)
            //          .amount(payload.pointAmount())
            //          .sourceType(UserPointSourceType.ORDER)
            //          .sourceId(payload.orderId())
            //          .reason("주문 취소 포인트 환불")
            //          .build();
            //  modifyUserPointUseCase.modify(command);

            log.info("포인트 환불 이벤트 검증 완료 (듀얼 라이트). orderId={}, buyerId={}, pointAmount={}",
                    payload.orderId(), payload.buyerId(), payload.pointAmount());
        } catch (Exception e) {
            log.error("포인트 환불 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
