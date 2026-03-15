package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
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
    private static final String PARTIAL_PRODUCT_DEDUCTION_REASON = "부분 결제 취소 상품 적립 예정 포인트 차감";

    private final ModifyPendingPointUseCase modifyPendingPointUseCase;
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

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PAYMENT_CANCELLED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            PaymentCancelledEvent payload = envelope.getPayloadAs(
                    PaymentCancelledEvent.class, objectMapper
            );

            log.info("결제 취소 이벤트 수신 (부분 상품 적립 예정 포인트 차감). eventId={}, orderId={}, buyerId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.isFullCancel());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (payload.isFullCancel()) {
                log.info("전체 취소 이벤트 -- 부분 상품 적립 예정 포인트 차감 불필요. orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("buyerId", payload.buyerId()))) {
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

            modifyPendingPointIdempotent(envelope.eventId(), payload.buyerId(), payload.partialProductPendingDeduction(), payload.orderId());

            log.info("부분 상품 적립 예정 포인트 차감 완료. orderId={}, buyerId={}, deductionAmount={}",
                    payload.orderId(), payload.buyerId(), payload.partialProductPendingDeduction());
        } catch (Exception e) {
            log.error("부분 상품 적립 예정 포인트 차감 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void modifyPendingPointIdempotent(String eventId, Long buyerId, Long deductionAmount, Long orderId) {
        try {
            ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
                    .userId(buyerId)
                    .changeType(UserPointChangeType.DEDUCTION)
                    .amount(deductionAmount)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(orderId)
                    .reason(PARTIAL_PRODUCT_DEDUCTION_REASON)
                    .build();
            modifyPendingPointUseCase.modifyPending(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 부분 상품 적립 예정 포인트 차감 이벤트 (멱등 처리). eventId={}, buyerId={}, message={}",
                    eventId, buyerId, e.getMessage());
        }
    }
}
