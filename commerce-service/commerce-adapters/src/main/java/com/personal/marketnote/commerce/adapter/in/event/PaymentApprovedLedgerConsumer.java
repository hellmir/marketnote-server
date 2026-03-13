package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
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
public class PaymentApprovedLedgerConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_APPROVED,
            groupId = "commerce-ledger"
    )
    public void handlePaymentApprovedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            PaymentApprovedEvent payload = envelope.getPayloadAs(PaymentApprovedEvent.class, objectMapper);

            log.info("결제 승인 이벤트 수신 (회계 분개). eventId={}, orderId={}, orderKey={}, paymentAmount={}",
                    envelope.eventId(), payload.orderId(), payload.orderKey(), payload.paymentAmount());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#933] HTTP 제거 후 RecordLedgerEntryUseCase.recordPaymentApproval() 활성화
            //  현재 듀얼 라이트 기간: PaymentApprovalTransactionHelper.commitSuccess()에서 동기로 분개 처리 중
            //  활성화 시 PaymentApprovedEvent에 paymentId 필드 추가 필요
            //  recordLedgerEntryUseCase.recordPaymentApproval(paymentId, payload.paymentAmount());

            log.info("결제 승인 분개 이벤트 검증 완료 (듀얼 라이트). orderId={}, orderKey={}, paymentAmount={}",
                    payload.orderId(), payload.orderKey(), payload.paymentAmount());
        } catch (Exception e) {
            log.error("결제 승인 분개 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
