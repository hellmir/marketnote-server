package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
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
public class OrderPaymentCompletedProductPointConsumer {
    private static final String PRODUCT_PURCHASE_REASON = "상품 구매 적립";

    private final ModifyPendingPointUseCase modifyPendingPointUseCase;
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

            log.info("주문 결제 완료 이벤트 수신 (상품 구매 포인트 적립). eventId={}, orderId={}, buyerId={}, totalAccumulatedPoint={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId(), payload.totalAccumulatedPoint());

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

            if (FormatValidator.hasNoValue(payload.totalAccumulatedPoint()) || payload.totalAccumulatedPoint() <= 0) {
                log.info("상품 적립 포인트가 없는 주문 (적립 생략). orderId={}, totalAccumulatedPoint={}",
                        payload.orderId(), payload.totalAccumulatedPoint());
                acknowledgment.acknowledge();
                return;
            }

            modifyPendingPointIdempotent(envelope.eventId(), payload.buyerId(), payload.totalAccumulatedPoint(), payload.orderId());

            log.info("상품 구매 포인트 적립 완료. orderId={}, buyerId={}, totalAccumulatedPoint={}",
                    payload.orderId(), payload.buyerId(), payload.totalAccumulatedPoint());
        } catch (Exception e) {
            log.error("상품 구매 포인트 적립 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void modifyPendingPointIdempotent(String eventId, Long buyerId, Long totalAccumulatedPoint, Long orderId) {
        try {
            ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
                    .userId(buyerId)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(totalAccumulatedPoint)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(orderId)
                    .reason(PRODUCT_PURCHASE_REASON)
                    .build();
            modifyPendingPointUseCase.modifyPending(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 상품 구매 포인트 적립 이벤트 (멱등 처리). eventId={}, buyerId={}, message={}",
                    eventId, buyerId, e.getMessage());
        }
    }
}
