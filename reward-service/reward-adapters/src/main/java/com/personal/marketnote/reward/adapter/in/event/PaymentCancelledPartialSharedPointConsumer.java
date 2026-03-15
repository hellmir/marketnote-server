package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent.OrderProductItem;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelledPartialSharedPointConsumer {
    private static final String PARTIAL_SHARED_DEDUCTION_REASON = "부분 결제 취소 공유 적립 예정 포인트 차감";

    private final ModifyPendingPointUseCase modifyPendingPointUseCase;
    private final ObjectMapper objectMapper;

    @Value("${reward.share-point-rate:0.1}")
    private float sharePointRate;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "reward-partial-pending-shared-point"
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

            log.info("결제 취소 이벤트 수신 (부분 공유 적립 예정 포인트 차감). eventId={}, orderId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.isFullCancel());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            if (payload.isFullCancel()) {
                log.info("전체 취소 이벤트 -- 부분 공유 적립 예정 포인트 차감 불필요. orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            List<Long> sharerIds = extractSharerIds(payload.orderProducts());
            if (sharerIds.isEmpty()) {
                log.info("공유자가 없는 주문 (부분 공유 적립 예정 포인트 차감 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            Long proportionalPoint = calculateProportionalSharedPoint(
                    payload.paymentAmount(), payload.cancelAmount()
            );
            if (FormatValidator.hasNoValue(proportionalPoint) || proportionalPoint <= 0) {
                log.info("부분 공유 적립 예정 포인트 차감 금액 없음. orderId={}, paymentAmount={}, cancelAmount={}",
                        payload.orderId(), payload.paymentAmount(), payload.cancelAmount());
                acknowledgment.acknowledge();
                return;
            }

            for (Long sharerId : sharerIds) {
                modifyPendingPointIdempotent(envelope.eventId(), sharerId, proportionalPoint, payload.orderId());
            }

            log.info("부분 공유 적립 예정 포인트 차감 완료. orderId={}, sharerIds={}, proportionalPoint={}",
                    payload.orderId(), sharerIds, proportionalPoint);
        } catch (Exception e) {
            log.error("부분 공유 적립 예정 포인트 차감 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void modifyPendingPointIdempotent(String eventId, Long sharerId, Long proportionalPoint, Long orderId) {
        try {
            ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
                    .userId(sharerId)
                    .changeType(UserPointChangeType.DEDUCTION)
                    .amount(proportionalPoint)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(orderId)
                    .reason(PARTIAL_SHARED_DEDUCTION_REASON)
                    .build();
            modifyPendingPointUseCase.modifyPending(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 부분 공유 적립 예정 포인트 차감 이벤트 (멱등 처리). eventId={}, sharerId={}, message={}",
                    eventId, sharerId, e.getMessage());
        }
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

    private Long calculateProportionalSharedPoint(Long paymentAmount, Long cancelAmount) {
        if (FormatValidator.hasNoValue(paymentAmount) || paymentAmount <= 0
                || FormatValidator.hasNoValue(cancelAmount) || cancelAmount <= 0) {
            return null;
        }

        if (cancelAmount > paymentAmount) {
            log.error("취소 금액이 결제 금액을 초과. cancelAmount={}, paymentAmount={}", cancelAmount, paymentAmount);
            return null;
        }

        long originalSharePoint = Math.round(paymentAmount * sharePointRate);
        if (originalSharePoint <= 0) {
            return null;
        }

        long numerator = Math.multiplyExact(cancelAmount, originalSharePoint);
        return (numerator + paymentAmount / 2) / paymentAmount;
    }
}
