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
public class OrderPaymentCompletedOrderPointConsumer {
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

            // FIXME: [#929][#1020] HTTP 제거 후 ModifyUserPointUseCase 활성화
            //  현재 듀얼 라이트 기간: ChangeOrderStatusService.afterCommit()에서 HTTP로 처리 중
            //  멱등성 보강 (#1212) 완료 후 아래 주석 해제:
            //  ModifyUserPointCommand command = ModifyUserPointCommand.builder()
            //          .userId(payload.buyerId())
            //          .changeType(UserPointChangeType.DEDUCTION)
            //          .amount(payload.pointAmount())
            //          .sourceType(UserPointSourceType.ORDER)
            //          .sourceId(payload.orderId())
            //          .reason("주문 포인트 차감")
            //          .build();
            //  modifyUserPointUseCase.modify(command);

            log.info("주문 포인트 차감 이벤트 검증 완료 (듀얼 라이트). orderId={}, buyerId={}, pointAmount={}",
                    payload.orderId(), payload.buyerId(), payload.pointAmount());
        } catch (Exception e) {
            log.error("주문 포인트 차감 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
