package com.personal.marketnote.commerce.adapter.in.event;

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
public class PaymentCancelledLedgerConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "commerce-ledger"
    )
    public void handlePaymentCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            PaymentCancelledEvent payload = envelope.getPayloadAs(PaymentCancelledEvent.class, objectMapper);

            log.info("결제 취소 이벤트 수신 (회계 역분개). eventId={}, orderId={}, isFullCancel={}, cancelAmount={}",
                    envelope.eventId(), payload.orderId(), payload.isFullCancel(), payload.cancelAmount());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1023] HTTP 제거 후 RecordLedgerEntryUseCase.recordPaymentCancellation() 활성화
            //  현재 듀얼 라이트 기간: CancelPaymentService.recordLedgerEntryForCancellation()에서 동기로 역분개 처리 중
            //  String idempotencyKey;
            //  if (payload.isFullCancel()) {
            //      idempotencyKey = "PAYMENT_CANCELLATION:" + payload.orderId();
            //  } else {
            //      idempotencyKey = "PAYMENT_PARTIAL_REFUND:" + payload.orderId()
            //              + ":" + payload.cancelAmount() + ":" + payload.alreadyRefunded();
            //  }
            //  recordLedgerEntryUseCase.recordPaymentCancellation(
            //          payload.orderId(), payload.cancelAmount(), idempotencyKey
            //  );

            log.info("결제 취소 역분개 이벤트 검증 완료 (듀얼 라이트). orderId={}, isFullCancel={}, cancelAmount={}",
                    payload.orderId(), payload.isFullCancel(), payload.cancelAmount());
        } catch (Exception e) {
            log.error("결제 취소 역분개 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
