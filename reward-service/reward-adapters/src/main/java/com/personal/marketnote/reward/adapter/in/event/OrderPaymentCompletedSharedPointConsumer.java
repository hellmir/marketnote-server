package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingSharedPointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentCompletedSharedPointConsumer {
    private static final String SHARE_PURCHASE_REASON = "링크 공유 회원 상품 구매";

    private final ModifyPendingSharedPointUseCase modifyPendingSharedPointUseCase;
    private final ObjectMapper objectMapper;

    @Value("${reward.share-point-rate:0.1}")
    private float sharePointRate;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "reward-shared-point"
    )
    public void handleOrderPaymentCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_PAYMENT_COMPLETED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            OrderPaymentCompletedEvent payload = envelope.getPayloadAs(
                    OrderPaymentCompletedEvent.class, objectMapper
            );

            log.info("주문 결제 완료 이벤트 수신 (공유 포인트 적립). eventId={}, orderId={}, buyerId={}",
                    envelope.eventId(), payload.orderId(), payload.buyerId());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()),
                    EventPayloadValidator.id("buyerId", payload.buyerId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.totalAmount()) || payload.totalAmount() <= 0) {
                log.info("결제 금액이 없는 주문 (공유 포인트 적립 생략). orderId={}, totalAmount={}",
                        payload.orderId(), payload.totalAmount());
                acknowledgment.acknowledge();
                return;
            }

            List<UUID> sharerKeys = extractSharerKeys(payload.orderProducts());
            if (sharerKeys.isEmpty()) {
                log.info("공유자가 없는 주문 (공유 포인트 적립 생략). orderId={}", payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            long sharePointAmount = Math.round(payload.totalAmount() * sharePointRate);
            if (sharePointAmount <= 0) {
                log.info("공유 포인트 적립 금액이 0 이하 (적립 생략). orderId={}, totalAmount={}, sharePointRate={}",
                        payload.orderId(), payload.totalAmount(), sharePointRate);
                acknowledgment.acknowledge();
                return;
            }

            for (UUID sharerKey : sharerKeys) {
                modifyPendingPointIdempotent(envelope.eventId(), sharerKey, sharePointAmount, payload.orderId());
            }

            log.info("공유 포인트 적립 완료. orderId={}, sharerKeys={}, sharePointAmount={}",
                    payload.orderId(), sharerKeys, sharePointAmount);
        } catch (Exception e) {
            log.error("공유 포인트 적립 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void modifyPendingPointIdempotent(String eventId, UUID sharerKey, long sharePointAmount, Long orderId) {
        try {
            ModifyPendingSharedPointCommand command = ModifyPendingSharedPointCommand.builder()
                    .sharerKey(sharerKey)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(sharePointAmount)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(orderId)
                    .reason(SHARE_PURCHASE_REASON)
                    .build();
            modifyPendingSharedPointUseCase.modifyPending(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 공유 포인트 적립 이벤트 (멱등 처리). eventId={}, sharerKey={}, message={}",
                    eventId, sharerKey, e.getMessage());
        }
    }

    private List<UUID> extractSharerKeys(List<OrderProductItem> orderProducts) {
        if (FormatValidator.hasNoValue(orderProducts)) {
            return List.of();
        }

        return orderProducts.stream()
                .map(OrderProductItem::sharerKey)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
